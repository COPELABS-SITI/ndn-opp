/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-08-31
 * This class is used to receive NDN packets from other devices
 * @author Miguel Tavares (COPELABS/ULHT)
 */

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

    /** This variable is used to debug OpportunisticChannelIn class */
    private static final String TAG = OpportunisticChannelIn.class.getSimpleName();

    /** This object is used to implement multithreading receiving */
    private OpportunisticChannelInTask mTask;

    /** This interface is used to notify the packet reception */
    private PacketObserver mObservingContext;

    /** This object is used to deliver the messages to NDN-OPP */
    private BufferOut mBufferOut;

    /** This method holds the status of this class*/
    private boolean mEnabled;

    /**
     * This method enables the features on this class
     * @param context application context
     */
    public synchronized void enable(Context context) {
        mObservingContext = (PacketObserver) context;
        WifiP2pListenerManager.registerListener(this);
    }

    /**
     * This method stops all the mechanisms on this class
     */
    public synchronized void disable() {
        WifiP2pListenerManager.unregisterListener(this);
        disableService();
    }

    /**
     * This method enables a socket and a task to receive NDN packets
     * @param assignedIp device's IP Address
     */
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

    /**
     * Disable the packet transfer service
     */
    private synchronized void disableService() {
        if (mEnabled) {
            mTask.close();
            mBufferOut.close();
            mEnabled = false;
        }
    }

    /**
     * This method is invoked when the device establishes a Wi-Fi P2P connection
     * @param intent
     */
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

    /**
     * This method is invoked when the Wi-Fi P2P connection goes down
     * @param intent
     */
    @Override
    public void onDisconnected(Intent intent) {
        disableService();
    }

}

