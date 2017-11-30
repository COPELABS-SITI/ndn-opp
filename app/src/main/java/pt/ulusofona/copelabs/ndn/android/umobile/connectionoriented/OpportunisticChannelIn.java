package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketObserver;
import pt.ulusofona.copelabs.ndn.android.umobile.nsd.NsdServiceRegistrar;

public class OpportunisticChannelIn {
    private static final String TAG = OpportunisticChannelIn.class.getSimpleName();
    private static final int DEFAULT_PORT = 16363;

    private ConnectionEventDetector mConnectionDetector = new ConnectionEventDetector();
    private NsdServiceRegistrar mRegistrar = new NsdServiceRegistrar();
    private OpportunisticDaemon.Binder mDaemonBinder;
    private PacketObserver mObservingContext;
    private ConnectionHandler mConnector;
    private boolean mEnabled;
    private Context mContext;


    public void enable(OpportunisticDaemon.Binder binder, Context context) {
        mContext = context;
        mDaemonBinder = binder;
        mObservingContext = (PacketObserver) context;
        mContext.registerReceiver(mConnectionDetector, new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION));
    }

    /** Disable the Routing engine. Changes in the connection status of Wi-Fi Direct Groups will be ignored. */
    void disable() {
        mContext.unregisterReceiver(mConnectionDetector);
        disableService();
    }

    private void enableService(String assignedIp) {
        if(!mEnabled) {
            try {
                Log.v(TAG, "Enabling ServerSocket on " + assignedIp + ":" + DEFAULT_PORT);
                ServerSocket socket = new ServerSocket();
                socket.bind(new InetSocketAddress(assignedIp, DEFAULT_PORT));
                mConnector = new ConnectionHandler(socket);
                mConnector.start();
                mRegistrar.register(mContext, mDaemonBinder.getUmobileUuid(), assignedIp, DEFAULT_PORT);
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
            while (mEnabled) {
                try {
                    // @TODO: multi-threaded receiver.
                    // Accept the next connection.
                    Socket connection = mAcceptingSocket.accept();
                    Log.d(TAG, "Connection from " + connection.toString());
                    ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                    //int availableBytes = in.available();
                    // Receive the bytes into the local buffer.
                    //int received = in.read(mBuffer, 0, availableBytes);
                    //Log.d(TAG, "Received " + received + " bytes of " + availableBytes + " expected.");
                    Packet packet = (Packet) in.readObject();

                    // Cleanup
                    in.close();
                    // Pass on the buffer to the Face for which it is intended. The identification is done by
                    // matching the remote IP with the UUID.
                    /** Use the IP to identify which service (UUID) is on the other end
                     pass the received packet to the corresponding Opportunistic Face. */
                    if (/*received > 0*/ packet != null)
                        mObservingContext.onPacketReceived(packet.getSender(), packet.getPayLoad());
                        //mDaemon.receiveOnFace(identifyFaceIdFromHostAddress(connection.getInetAddress().getHostAddress()), received, mBuffer);
                    // Close connection
                    connection.close();
                } catch (IOException e) {
                    Log.e(TAG, "Connection went WRONG.");
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ConnectionEventDetector extends BroadcastReceiver {
        private String mAssignedIpv4 = null;

        private String extractIp(WifiP2pGroup group) {
            String ipAddress = null;
            String interfaceName = group.getInterface();
            Log.v(TAG, "Group Interface : " + interfaceName);
            if (interfaceName != null) {
                try {
                    Enumeration<NetworkInterface> allIfaces = NetworkInterface.getNetworkInterfaces();
                    Log.v(TAG, allIfaces.toString());
                    while (allIfaces.hasMoreElements()) {
                        NetworkInterface iface = allIfaces.nextElement();
                        Log.v(TAG, iface.toString());
                        if (interfaceName.equals(iface.getName())) {
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

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                WifiP2pGroup wifip2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

                Log.v(TAG, "NetworkInfo : " + netInfo);
                Log.v(TAG, "WifiP2pGroup : " + wifip2pGroup);

                if (netInfo.isConnected()) {
                    /* When the current device connects to a Group;
                       1) retrieve the IP it has been assigned and
                       2) enable the opportunistic service on that IP
                    */
                    String newIpv4 = extractIp(wifip2pGroup);
                    if (mAssignedIpv4 != null) {
                        // If it was previously connected, and the IP has changed enable the service
                        if (!mAssignedIpv4.equals(newIpv4)) {
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

