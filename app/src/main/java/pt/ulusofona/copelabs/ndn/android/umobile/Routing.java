/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * The Router class acts as the pivot between the ContextualManager and the ForwardingDaemon.
 * It fullfills two functions within the App
 * (1) Routing: recomputing the new routes to be installed into the ForwardingDaemon's RIB
 * (2) Face management: translating the changes in neighborhood (i.e. other UMobile nodes availability)
 * into bringing the corresponding Faces AVAILABLE and UNAVAILABLE and establishing the connection that those Faces
 * ought to use for communication.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile;

import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import pt.ulusofona.copelabs.ndn.android.models.Face;
import pt.ulusofona.copelabs.ndn.android.models.NsdService;
import pt.ulusofona.copelabs.ndn.android.umobile.nsd.NsdServiceRegistrar;
import pt.ulusofona.copelabs.ndn.android.umobile.nsd.NsdServiceTracker;
import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pConnectivityTracker;

// @TODO: if phone goes to sleep, all the open connections will close.
public class Routing implements Observer {
    private static final String TAG = Routing.class.getSimpleName();
    private static final int DEFAULT_PORT = 16363;

    private ForwardingDaemon mDaemon;
    private Map<String, NsdService> mUmobileServices = new HashMap<>();
    private Map<String, Long> mOppFaceIds = new HashMap<>();
    private Map<String, OpportunisticChannel> mOppChannels = new HashMap<>();

    private boolean mEnabled = false;
    private ConnectionHandler mConnector;

    private NsdServiceRegistrar mRegistrar = new NsdServiceRegistrar();

	Routing(ForwardingDaemon daemon) { mDaemon = daemon; }

    void afterFaceAdd(Face f) {
        // If this is an opportunistic face, it must be introduced into the RIB for /ndn/multicast.
        long faceId = f.getFaceId();
        if(f.getRemoteUri().startsWith("opp://")) {
            String peerUuid = f.getRemoteUri().substring(6);
            mOppFaceIds.put(peerUuid, faceId);

            Log.d(TAG, "Registering Opportunistic Face " + faceId + " in RIB for prefix /ndn/multicast.");
            mDaemon.addRoute("/ndn", faceId, 0L, 0L, 1L);
        }
    }

    void bringUpFace(String uuid) {
        Log.d(TAG, "Bringing UP face for " + uuid + " " + mOppFaceIds.containsKey(uuid));
        if(mOppFaceIds.containsKey(uuid)) {
            long faceId = mOppFaceIds.get(uuid);
            NsdService svc = mUmobileServices.get(uuid);
            if(!mOppChannels.containsKey(uuid)) {
                OpportunisticChannel chan = new OpportunisticChannel(mDaemon, this, uuid, faceId, svc.getHost(), svc.getPort());
                mOppChannels.put(uuid, chan);
                mDaemon.bringUpFace(faceId, chan);
            }
        }
    }

    void bringDownFace(String uuid) {
        Log.d(TAG, "Bringing DOWN face for " + uuid);
        if (mOppFaceIds.containsKey(uuid)) {
            if(mOppChannels.containsKey(uuid)) {
                mOppChannels.remove(uuid);
                mDaemon.bringDownFace(mOppFaceIds.get(uuid));
            }
        }
    }

    private void updateService(NsdService current) {
        if(current.getStatus() == NsdService.Status.AVAILABLE) {
            /* If the peer is unknown (i.e. its UUID is not in the list of UMobile peers,
             * we request the creation of a Face for it. Then, if the Face has an ID referenced
             * in the existing Opportunistic faces, bring it up. */
            if(!mUmobileServices.containsKey(current.getUuid())) {
                Log.d(TAG, "Requesting Face creation");
                mDaemon.createFace("opp://" + current.getUuid(), 0, false);
            }
            mUmobileServices.put(current.getUuid(), current);
            bringUpFace(current.getUuid());
        } else if(current.getStatus() == NsdService.Status.UNAVAILABLE) {
            mUmobileServices.put(current.getUuid(), current);
            bringDownFace(current.getUuid());
        }
    }

    private void enable(String assignedIp) {
        if(!mEnabled) {
            try {
                Log.v(TAG, "Enabling ServerSocket");
                ServerSocket socket = new ServerSocket();
                socket.bind(new InetSocketAddress(assignedIp, DEFAULT_PORT));
                mConnector = new ConnectionHandler(socket);
                mConnector.start();
                mRegistrar.register(mDaemon, mDaemon.getUmobileUuid(), DEFAULT_PORT);
                mEnabled = true;
            } catch (IOException e) {
                Log.e(TAG, "Failed to open listening socket");
                e.printStackTrace();
            }
        }
    }

    private void disable() {
        if(mEnabled) {
            mConnector.terminate();
            mRegistrar.unregister();
            mEnabled = false;
        }
    }

    @Override
    public void update(Observable observable, Object obj) {
        if(observable instanceof NsdServiceTracker) {
            if (obj != null) {
                if (obj instanceof NsdService) {
                    NsdService svc = (NsdService) obj;
                    Log.v(TAG, "Received a PUNCTUAL NsdService UPDATE : " + svc);
                    updateService((NsdService) obj);
                } else if (obj instanceof Set) {
                    Set<NsdService> changes = (Set<NsdService>) obj;
                    Log.v(TAG, "Received a COMPLETE NsdService UPDATE : " + changes);
                    for (NsdService current : changes)
                        updateService(current);
                }
            } else
                Log.w(TAG, "Received NULL object from NsdServiceTracker");
        } else if (observable instanceof WifiP2pConnectivityTracker) {
            WifiP2pConnectivityTracker wifiConnTracker = (WifiP2pConnectivityTracker) observable;
            boolean connected = (boolean) obj;
            if(connected) enable(wifiConnTracker.getAssignedIp());
            else disable();
        }
    }

    private class ConnectionHandler extends Thread {
        // Matches the constant from ndn-cxx/encoding/tlv.hpp
        private static final int MAX_NDN_PACKET_SIZE = 8800;
        private byte[] mBuffer;
        private ServerSocket mAcceptingSocket;
        private boolean mEnabled;

        ConnectionHandler(ServerSocket sock) {
            mBuffer = new byte[MAX_NDN_PACKET_SIZE];
            mAcceptingSocket = sock;
            mEnabled = true;
        }

        void terminate() {
            mEnabled = false;
            try {
                mAcceptingSocket.close();
            } catch (IOException e) {
                Log.w(TAG, "Failure to close the ServerSocket " + e.getMessage());
            }
        }

        @Override
        public void run() {
            mEnabled = true;
            Log.d(TAG, "Accepting on " + mAcceptingSocket.toString());
            while(mEnabled) {
                try {
                    // @TODO: multi-threaded receiver.
                    Socket connection = mAcceptingSocket.accept();
                    Log.d(TAG, "Connection from " + connection.toString());
                    DataInputStream in = new DataInputStream(connection.getInputStream());
                    int availableBytes = in.available();
                    int received = in.read(mBuffer, 0, availableBytes);
                    Log.d(TAG, "Received " + received + " bytes of " + availableBytes + " expected.");
                    in.close();
                    if(received > 0)
                        mDaemon.receiveOnFace(identifyUuid(connection.getInetAddress().getHostAddress()), received, mBuffer);
                    connection.close();
                    /** Use the IP to identify which service (UUID) is on the other end
                        pass the received packet to the corresponding transport. */
                } catch (IOException e) {
                    Log.e(TAG, "Connection went WRONG.");
                }
            }
        }

        private long identifyUuid(String hostAddress) {
            for(NsdService svc : mUmobileServices.values())
                if(svc.getHost().equals(hostAddress))
                    return mOppFaceIds.get(svc.getUuid());

            Log.e(TAG, "No service matching " + hostAddress + " found.");
            return -1L;
        }
    }
}