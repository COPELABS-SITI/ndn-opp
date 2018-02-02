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

    private static final String TAG = NsdManager.class.getSimpleName();
    private static final int PORT = 26363;
    private boolean mEnabled, mRunning, mGroupOwning, mDiscovering;
    private OpportunisticDaemon.Binder mDaemonBinder;
    private ServiceDiscoverer mServiceDiscoverer;
    private ServiceRegister mServiceRegister;
    private ServiceLeader mServiceLeader;
    private HostInfo mLeaderInfo;
    private NsdInfo mMeInfo;

    public synchronized void enable(OpportunisticDaemon.Binder binder) {
        if(!mEnabled) {
            Log.i(TAG, "Enabling NsdManager");
            mDaemonBinder = binder;
            WifiP2pListenerManager.registerListener(this);
            mEnabled = true;
            Log.i(TAG, "NsdManager enabled");
        }
    }

    public synchronized void disable() {
        if(mEnabled) {
            Log.i(TAG, "Disabling NsdManager");
            WifiP2pListenerManager.unregisterListener(this);
            disableService();
            mEnabled = false;
            Log.i(TAG, "NsdManager disabled");
        }
    }

    private synchronized void enableService() {
        if(!mRunning) {
            Log.i(TAG, "Enabling services of NsdManager");
            mServiceDiscoverer = ServiceDiscoverer.getInstance();
            mServiceRegister = ServiceRegister.getInstance();
            mRunning = true;
            Log.i(TAG, "Services of NsdManager enabled");
        }
    }

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

    @Override
    public void onDisconnected(Intent intent) {
        Log.i(TAG, "Disconnected");
        disableService();
    }

    @Override
    public void onIamGo() {
        if(!mGroupOwning) {
            Log.i(TAG, "I'm GO");
            mServiceLeader = new ServiceLeader();
            mServiceLeader.start();
            mGroupOwning = true;
        }
    }

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
