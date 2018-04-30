/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class is manages the NSD implementation.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd;


import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.HostInfo;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services.ServiceDiscoverer;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services.ServiceLeader;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services.ServiceRegister;
import pt.ulusofona.copelabs.ndn.android.utilities.Utilities;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;

public class NsdManager implements WifiP2pListener.WifiP2pConnectionStatus, WifiP2pListener.GoIpAvailable {

    /** This variable is used to debug NsdManager class */
    private static final String TAG = NsdManager.class.getSimpleName();

    /** This port is used to establish a communication between devices that uses this NSD implementation */
    private static final int PORT = 26363;

    /** These variables are used to hold the status of some NSD features */
    private boolean mEnabled, mRunning, mGroupOwning, mDiscovering;

    /** This interface is used to communicate with the daemon */
    private OpportunisticDaemon.Binder mDaemonBinder;

    /** This object does the peer discovery */
    private ServiceDiscoverer mServiceDiscoverer;

    /** This object does the NSD registration */
    private ServiceRegister mServiceRegister;

    /** This object manages all peer's status */
    private ServiceLeader mServiceLeader;

    /** This object holds the leader's host info */
    private HostInfo mLeaderInfo;

    /** This object holds the this device host info */
    private NsdInfo mMeInfo;


    /**
     * This method enables the NSD Manager
     * @param binder
     */
    public synchronized void enable(OpportunisticDaemon.Binder binder) {
        if(!mEnabled) {
            Log.i(TAG, "Enabling NsdManager");
            mDaemonBinder = binder;
            WifiP2pListenerManager.registerListener(this);
            mEnabled = true;
            Log.i(TAG, "NsdManager enabled");
        }
    }

    /**
     * This method disables the NSD Manager
     */
    public synchronized void disable() {
        if(mEnabled) {
            Log.i(TAG, "Disabling NsdManager");
            WifiP2pListenerManager.unregisterListener(this);
            disableService();
            mEnabled = false;
            Log.i(TAG, "NsdManager disabled");
        }
    }


    /**
     * This method startCheckFib some features related with NSD service
     */
    private synchronized void enableService() {
        if(!mRunning) {
            Log.i(TAG, "Enabling services of NsdManager");
            mServiceDiscoverer = ServiceDiscoverer.getInstance();
            mServiceRegister = ServiceRegister.getInstance();
            mRunning = true;
            Log.i(TAG, "Services of NsdManager enabled");
        }
    }

    /**
     * This method disables all the features related with NSD service
     */
    private synchronized void disableService() {
        if(mRunning) {
            Log.i(TAG, "Disabling services of NsdManager");
            mServiceRegister.close();
            mServiceDiscoverer.close();
            if (mServiceLeader != null)
                mServiceLeader.close();
            mLeaderInfo = null;
            mMeInfo = null;
            mRunning = false;
            mGroupOwning = false;
            mDiscovering = false;
            Log.i(TAG, "Services of NsdManager disabled");
        }
    }

    /**
     * This method is invoked when is detected a Wi-FI P2P connection
     * It starts automatically the discovery process and also detects this device info.
     * @param intent
     */
    @Override
    public void onConnected(Intent intent) {
        Log.i(TAG, "Connected");
        enableService();
        NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
        WifiP2pGroup wifip2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
        if (netInfo.isConnected()) {
            String myIpAddress = Utilities.extractIp(wifip2pGroup);
            Log.i(TAG, "Received my own ip address " + myIpAddress);
            mMeInfo = new NsdInfo(mDaemonBinder.getUmobileUuid(), myIpAddress, PORT);
            mServiceDiscoverer.start(mMeInfo);
        }
    }

    /**
     * This method is invoked when a Wi-Fi P2P connection goes down
     * @param intent
     */
    @Override
    public void onDisconnected(Intent intent) {
        Log.i(TAG, "Disconnected");
        disableService();
    }

    /**
     * This method is invoked when this device is elected as a GO
     */
    @Override
    public void onIamGo() {
        if(!mGroupOwning) {
            Log.i(TAG, "I'm GO");
            mServiceLeader = new ServiceLeader();
            mServiceLeader.start();
            mGroupOwning = true;
        }
    }

    /**
     * This method is invoked when GO's IP address is discovered
     * @param ipAddress GO's IP address
     */
    @Override
    public void onGoIpAddressAvailable(String ipAddress) {
        if(!mDiscovering) {
            Log.i(TAG, "Received GO ip address " + ipAddress);
            mLeaderInfo = new HostInfo(ipAddress, PORT);
            mServiceRegister.start(mLeaderInfo, mMeInfo);
            mDiscovering = true;
        }
    }
}
