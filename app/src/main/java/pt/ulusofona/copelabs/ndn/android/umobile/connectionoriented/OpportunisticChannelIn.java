package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented;


import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketObserver;
import pt.ulusofona.copelabs.ndn.android.umobile.nsd.NsdServiceRegistrar;
import pt.ulusofona.copelabs.ndn.android.utilities.Utilities;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;

public class OpportunisticChannelIn implements WifiP2pListener.WifiP2pConnectionStatus {
    private static final String TAG = OpportunisticChannelIn.class.getSimpleName();
    private static final int DEFAULT_PORT = 16363;

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
        WifiP2pListenerManager.registerListener(this);
    }

    /** Disable the Routing engine. Changes in the connection status of Wi-Fi Direct Groups will be ignored. */
    public void disable() {
        WifiP2pListenerManager.unregisterListener(this);
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

    @Override
    public void onConnected(Intent intent) {
        String mAssignedIpv4 = null;
        NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        WifiP2pGroup wifip2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

        Log.v(TAG, "NetworkInfo : " + netInfo);
        Log.v(TAG, "WifiP2pGroup : " + wifip2pGroup);

        if (netInfo.isConnected()) {
                /* When the current device connects to a Group;
                   1) retrieve the IP it has been assigned and
                   2) enable the opportunistic service on that IP
                */
            String newIpv4 = Utilities.extractIp(wifip2pGroup);
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

        }
    }

    @Override
    public void onDisconnected(Intent intent) {
        disableService();
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
                    Packet packet = (Packet) in.readObject();
                    // Cleanup
                    in.close();
                    // Pass on the buffer to the Face for which it is intended. The identification is done by
                    // matching the remote IP with the UUID.
                    /** Use the IP to identify which service (UUID) is on the other end
                     pass the received packet to the corresponding Opportunistic Face. */
                    if (packet != null) {
                        Log.i(TAG, "Packet received from " + packet.getSender());
                        mObservingContext.onPacketReceived(packet.getSender(), packet.getPayload());
                    }
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

}

