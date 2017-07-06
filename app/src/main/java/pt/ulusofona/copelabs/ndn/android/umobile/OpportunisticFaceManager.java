/* @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of a Routing engine that serves as the interface between the NOD
 * and the Contextual Manager.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import pt.ulusofona.copelabs.ndn.android.models.Face;
import pt.ulusofona.copelabs.ndn.android.models.NsdService;
import pt.ulusofona.copelabs.ndn.android.umobile.nsd.NsdServiceRegistrar;
import pt.ulusofona.copelabs.ndn.android.umobile.nsd.NsdServiceDiscoverer;

// @TODO: if phone goes to sleep, all the open connections will close.

/**
 * The Opportunistic Face Manager acts as the pivot between the ContextualManager and the ForwardingDaemon.
 * It fullfills two functions within the App
 * (1) Routing: recomputing the new routes to be installed into the ForwardingDaemon's RIB
 * (2) Face management: translating the changes in neighborhood (i.e. other UMobile nodes availability)
 * into bringing the corresponding Faces AVAILABLE and UNAVAILABLE and establishing the connection that those Faces
 * ought to use for communication.
 */
public class OpportunisticFaceManager implements Observer {
    private static final String TAG = OpportunisticFaceManager.class.getSimpleName();
    private static final int DEFAULT_PORT = 16363;

    private Context mContext;
    private OpportunisticDaemon.NodBinder mDaemon;

    // Associates a NsdService to a UUID
    private Map<String, NsdService> mUmobileServices = new HashMap<>();
    // Associates a FaceId to a UUID
    private Map<String, Long> mOppFaceIds = new HashMap<>();
    // Associates a OpportunisticChannel to a UUID
    private Map<String, OpportunisticChannel> mOppChannels = new HashMap<>();

    private boolean mEnabled = false;
    private ConnectionHandler mConnector;

    private NsdServiceRegistrar mRegistrar = new NsdServiceRegistrar();
    private ConnectionEventDetector mConnectionDetector = new ConnectionEventDetector();

    /** Enable the OppFaceManager. When enabled, it reacts to changes in the connection status to a Wi-Fi Direct Group.
     * As the Group Formation results in the device being assigned an IP address, a listening socket will be opened and
     * used for the reception of Interest/Data packets from other devices connected to the same Group. Furthermore,
     * @param context Android-provided context
     * @param binder Binder to the ForwardingDaemon
     */
	void enable(Context context, OpportunisticDaemon.NodBinder binder) {
        mDaemon = binder;

        mContext = context;
        mContext.registerReceiver(mConnectionDetector, new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION));
    }

    /** Disable the Routing engine. Changes in the connection status of Wi-Fi Direct Groups will be ignored. */
    void disable() {
        mContext.unregisterReceiver(mConnectionDetector);
        disableService();
    }

    /** Callback method invoked by the ForwardingDaemon when the creation of a Face has been successful.
     * @param face a representation of the Face that was created
     */
    void afterFaceAdded(Face face) {
        /* If the created face is an opportunistic one, it must be registered into the data structures used
           to make the link with the socket on the other side. */
        if(face.getRemoteUri().startsWith("opp://")) {
            long faceId = face.getFaceId();
            String peerUuid = face.getRemoteUri().substring(6);
            mOppFaceIds.put(peerUuid, faceId);

            Log.d(TAG, "Registering Opportunistic Face " + faceId + " in RIB for prefix /ndn/multicast.");
            mDaemon.addRoute("/ndn", faceId, 0L, 0L, 1L);
        }
    }

    /** Used to handle when a UMobile peer is detected to bring up its corresponding Face.
     * @param uuid the UUID of the device that was detected.
     */
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

    /** Used to handle the departure of an NDN-Opp peer from the current Wi-Fi Direct Group
     * @param uuid the UUID of the NDN-Opp peer which left
     */
    void bringDownFace(String uuid) {
        Log.d(TAG, "Bringing DOWN face for " + uuid);
        if (mOppFaceIds.containsKey(uuid)) {
            if(mOppChannels.containsKey(uuid)) {
                mOppChannels.remove(uuid);
                mDaemon.bringDownFace(mOppFaceIds.get(uuid));
            }
        }
    }

    /** Update service information regarding a NDN-Opp peer
     * @param current new information to be used for the update
     */
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

    /** Enable the service which enables packets to be transferred. This includes opening a listening socket
     * on an IP:port combination which can be used to effectively transfer Interest/Data packets among the NDN-Opp peers.
     * @param assignedIp the IP that has been assigned to the current device within the Wi-Fi Direct Group it is connected to
     */
    private void enableService(String assignedIp) {
        if(!mEnabled) {
            try {
                Log.v(TAG, "Enabling ServerSocket on " + assignedIp + ":" + DEFAULT_PORT);
                ServerSocket socket = new ServerSocket();
                socket.bind(new InetSocketAddress(assignedIp, DEFAULT_PORT));
                mConnector = new ConnectionHandler(socket);
                mConnector.start();
                mRegistrar.register(mContext, mDaemon.getUmobileUuid(), assignedIp, DEFAULT_PORT);
                mEnabled = true;
            } catch (IOException e) {
                Log.e(TAG, "Failed to open listening socket");
                e.printStackTrace();
            }
        }
    }

    /** Disable the packet transfer service. */
    private void disableService() {
        if(mEnabled) {
            mConnector.terminate();
            mRegistrar.unregister();
            mEnabled = false;
        }
    }

    /** Update the state of the Routing engine based on what the NSD Service Discoverer reports
     * @param observable observable notifying of changes
     * @param obj optional parameter passed by the observable
     */
    @Override
    public void update(Observable observable, Object obj) {
        if(observable instanceof NsdServiceDiscoverer) {
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
                Log.w(TAG, "Received NULL object from NsdServiceDiscoverer");
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

        /** Main thread encapsulating the logic of packet reception
         */
        @Override
        public void run() {
            mEnabled = true;
            Log.d(TAG, "Accepting on " + mAcceptingSocket.toString());
            while(mEnabled) {
                try {
                    // @TODO: multi-threaded receiver.
                    // Accept the next connection.
                    Socket connection = mAcceptingSocket.accept();
                    Log.d(TAG, "Connection from " + connection.toString());
                    DataInputStream in = new DataInputStream(connection.getInputStream());
                    int availableBytes = in.available();
                    // Receive the bytes into the local buffer.
                    int received = in.read(mBuffer, 0, availableBytes);
                    Log.d(TAG, "Received " + received + " bytes of " + availableBytes + " expected.");
                    // Cleanup
                    in.close();
                    // Pass on the buffer to the Face for which it is intended. The identification is done by
                    // matching the remote IP with the UUID.
                    /** Use the IP to identify which service (UUID) is on the other end
                     pass the received packet to the corresponding Opportunistic Face. */
                    if(received > 0)
                        mDaemon.receiveOnFace(identifyFaceIdFromHostAddress(connection.getInetAddress().getHostAddress()), received, mBuffer);
                    // Close connection
                    connection.close();
                } catch (IOException e) {
                    Log.e(TAG, "Connection went WRONG.");
                }
            }
        }

        /** Returns the FaceId of the Face corresponding to the remote host.
         * @param hostAddress the host address of the remote point of the connection
         * @return the FaceId of the Face that points to the remote host
         */
        private long identifyFaceIdFromHostAddress(String hostAddress) {
            for(NsdService svc : mUmobileServices.values())
                if(svc.getHost().equals(hostAddress))
                    return mOppFaceIds.get(svc.getUuid());

            Log.e(TAG, "No service matching " + hostAddress + " found.");
            return -1L;
        }
    }

    /** ConnectionEventDetector keeps track of connection events relating to the Wi-Fi Direct Group */
    private class ConnectionEventDetector extends BroadcastReceiver {
        private String mAssignedIpv4 = null;

        private String extractIp(WifiP2pGroup group) {
            String ipAddress = null;
            String interfaceName = group.getInterface();
            Log.v(TAG, "Group Interface : " + interfaceName);
            if(interfaceName != null) {
                try {
                    Enumeration<NetworkInterface> allIfaces = NetworkInterface.getNetworkInterfaces();
                    Log.v(TAG, allIfaces.toString());
                    while (allIfaces.hasMoreElements()) {
                        NetworkInterface iface = allIfaces.nextElement();
                        Log.v(TAG, iface.toString());
                        if(interfaceName.equals(iface.getName())) {
                            for (InterfaceAddress ifAddr : iface.getInterfaceAddresses()) {
                                InetAddress address = ifAddr.getAddress();
                                if (address instanceof Inet4Address && !address.isAnyLocalAddress())
                                    ipAddress = address.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
            return ipAddress;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received Intent : " + action);

            if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                WifiP2pGroup wifip2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

                Log.v(TAG, "NetworkInfo : " + netInfo);
                Log.v(TAG, "WifiP2pGroup : " + wifip2pGroup);

                if(netInfo.isConnected()) {
                    /* When the current device connects to a Group;
                       1) retrieve the IP it has been assigned and
                       2) enable the opportunistic service on that IP
                    */
                    String newIpv4 = extractIp(wifip2pGroup);
                    if (mAssignedIpv4 != null) {
                        // If it was previously connected, and the IP has changed enable the service
                        if(!mAssignedIpv4.equals(newIpv4)) {
                            disableService();
                            mAssignedIpv4 = newIpv4;
                            enableService(mAssignedIpv4);
                        }
                    } else {
                        mAssignedIpv4 = newIpv4;
                        enableService(mAssignedIpv4);
                    }

                } else {
                    mAssignedIpv4 = null;
                    disableService();
                }
            }
        }
    }
}