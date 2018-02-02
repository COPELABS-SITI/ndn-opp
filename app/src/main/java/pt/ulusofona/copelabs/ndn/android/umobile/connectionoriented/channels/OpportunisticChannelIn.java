package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.channels;


import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketObserver;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.buffering.BufferOut;
import pt.ulusofona.copelabs.ndn.android.utilities.Utilities;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;

import static pt.ulusofona.copelabs.ndn.android.models.NsdService.DEFAULT_PORT;

public class OpportunisticChannelIn extends Thread implements WifiP2pListener.WifiP2pConnectionStatus {

    private static final String TAG = OpportunisticChannelIn.class.getSimpleName();
    private OpportunisticChannelInTask mTask;
    private PacketObserver mObservingContext;
    private BufferOut mBufferOut;
    private boolean mEnabled;


    public synchronized void enable(Context context) {
        mObservingContext = (PacketObserver) context;
        WifiP2pListenerManager.registerListener(this);
    }

    /** Disable the Routing engine. Changes in the connection status of Wi-Fi Direct Groups will be ignored. */
    public synchronized void disable() {
        WifiP2pListenerManager.unregisterListener(this);
        disableService();
    }

    private synchronized void enableService(String assignedIp) {
        if(!mEnabled) {
            try {
                Log.v(TAG, "Enabling ServerSocket on " + assignedIp + ":" + DEFAULT_PORT);
                mBufferOut = new BufferOut(mObservingContext);
                ServerSocket socket = new ServerSocket();
                socket.bind(new InetSocketAddress(assignedIp, DEFAULT_PORT));
                mTask = new OpportunisticChannelInTask(socket);
                mTask.start();
                mEnabled = true;
            } catch (IOException e) {
                Log.e(TAG, "Failed to open listening socket");
                e.printStackTrace();
            }
        }
    }

    /** Disable the packet transfer service. */
    private synchronized void disableService() {
        if (mEnabled) {
            mTask.close();
            mBufferOut.close();
            mEnabled = false;
        }
    }

    @Override
    public void onConnected(Intent intent) {
        Log.i(TAG, "Connected");
        NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        WifiP2pGroup wifip2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
        if (netInfo.isConnected()) {
            String myIpAddress = Utilities.extractIp(wifip2pGroup);
            Log.i(TAG, "Received my own ip address " + myIpAddress);
            enableService(myIpAddress);
        }
    }

    @Override
    public void onDisconnected(Intent intent) {
        disableService();
    }

}

