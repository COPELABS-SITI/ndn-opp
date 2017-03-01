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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.ulusofona.copelabs.ndn.android.Face;
import pt.ulusofona.copelabs.ndn.android.UmobileService;
import pt.ulusofona.copelabs.ndn.android.UmobileService.Status;

// @TODO: if phone goes to sleep, all the open connections will close.
public class Routing {
    private static final String TAG = Routing.class.getSimpleName();

    private ForwardingDaemon mDaemon;
    private Map<String, UmobileService> mUmobilePeers = new HashMap<>();
    private Map<String, Long> mOppFaceIds = new HashMap<>();
    private Map<String, OpportunisticChannel> mOppChannels = new HashMap<>();

    private boolean mConnectorStarted = false;
    private ConnectionHandler mConnector = new ConnectionHandler();

	Routing(ForwardingDaemon daemon) { mDaemon = daemon; }

    void afterFaceAdd(Face f) {
        // If this is an opportunistic face, it must be introduced into the RIB for /ndn/multicast.
        long faceId = f.getId();
        if(f.getRemoteURI().startsWith("opp://")) {
            String peerUuid = f.getRemoteURI().substring(6);
            mOppFaceIds.put(peerUuid, faceId);

            Log.d(TAG, "Registering Opportunistic Face " + faceId + " in RIB for prefix /ndn/multicast.");
            mDaemon.addRoute("/ndn", faceId, 0L, 0L, 1L);
        }
    }

    private void bringUpFace(String uuid) {
        Log.d(TAG, "Bringing UP face for " + uuid + " " + mOppFaceIds.containsKey(uuid));
        if(mOppFaceIds.containsKey(uuid)) {
            long faceId = mOppFaceIds.get(uuid);
            UmobileService svc = mUmobilePeers.get(uuid);
            OpportunisticChannel oc = new OpportunisticChannel(mDaemon, faceId, svc.host, svc.port);
            mOppChannels.put(uuid, oc);
            mDaemon.bringUpFace(faceId, oc);
        }
    }

    private void bringDownFace(String uuid) {
        Log.d(TAG, "Bringing DOWN face for " + uuid);
        if (mOppFaceIds.containsKey(uuid)) {
            mOppChannels.remove(uuid);
            mDaemon.bringDownFace(mOppFaceIds.get(uuid));
        }
    }

    public void update(UmobileService current) {
        if(current.getStatus() == Status.AVAILABLE) {
            /* If the peer is unknown (i.e. its UUID is not in the list of UMobile peers,
             * we request the creation of a Face for it. Then, if the Face has an ID referenced
             * in the existing Opportunistic faces, bring it up. */
            if(!mUmobilePeers.containsKey(current.uuid)) {
                Log.d(TAG, "Requesting Face creation");
                mDaemon.createFace("opp://" + current.uuid, 0, false);
            }
            mUmobilePeers.put(current.uuid, current);
            bringUpFace(current.uuid);
        } else if(current.getStatus() == Status.UNAVAILABLE) {
            mUmobilePeers.put(current.uuid, current);
            bringDownFace(current.uuid);
        }
    }

    public void update(Set<UmobileService> serviceChanges) {
        Log.d(TAG, "Received a COMPLETE update : " + serviceChanges);
        for(UmobileService svc : serviceChanges) update(svc);
    }

    public void enable(ServerSocket mSocket) {
        if( !mConnectorStarted ) {
            mConnector.runOn(mSocket);
            mConnectorStarted = true;
        }
    }

    public void disable() {
        if( mConnectorStarted ) {
            mConnector.stop();
            mConnectorStarted = false;
        }
    }

    private class ConnectionHandler extends Thread {
        // Matches the constant from ndn-cxx/encoding/tlv.hpp
        private static final int MAX_NDN_PACKET_SIZE = 8800;
        private byte[] mBuffer;
        private ServerSocket mAcceptingSocket;
        private boolean mEnabled;

        void runOn(ServerSocket sock) {
            mBuffer = new byte[MAX_NDN_PACKET_SIZE];
            mAcceptingSocket = sock;
            if(!mEnabled)
                start();
        }

        private String hex(byte[] buf, int limit) {
            StringBuilder hexStr = new StringBuilder();
            for(int i = 0; i < limit; i++)
                hexStr.append(Integer.toHexString(buf[i] & 0xFF)).append(" ");
            return hexStr.toString();
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
                    Log.d(TAG, "Payload : " + hex(mBuffer, received));
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
            Log.d(TAG, "Shutting down acceptor.");
            try {
                mAcceptingSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private long identifyUuid(String hostAddress) {
            for(UmobileService svc : mUmobilePeers.values())
                if(svc.host.equals(hostAddress))
                    return mOppFaceIds.get(svc.uuid);

            Log.e(TAG, "No service matching " + hostAddress + " found.");
            return -1L;
        }
    }
}