package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pConnectivityTracker;
import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pDiscoveryTracker;
import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pStateTracker;

class WifiP2pDeviceDiscoverer extends Observable implements Observer {
    private static final String TAG = WifiP2pDeviceDiscoverer.class.getSimpleName();

    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    private final IntentFilter mPeerChangeIntent = new IntentFilter();
    private final PeerChangeReceiver mPeerChangeTracker = new PeerChangeReceiver();

    private final WifiP2pStateTracker mStateTracker = WifiP2pStateTracker.getInstance();
    private final WifiP2pConnectivityTracker mConnectivityTracker = WifiP2pConnectivityTracker.getInstance();

    private boolean mEnabled = false;
    private boolean mStarted = false;
    private boolean mWifiP2pEnabled = false;
    private boolean mWifiP2pConnected = false;
    private boolean mDiscovering = false;

    // Associates a MAC address to a WifiP2pDevice
    private Map<String, WifiP2pDevice> mDevices = new HashMap<>();

    WifiP2pDeviceDiscoverer() {
        mPeerChangeIntent.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
    }

    synchronized void enable(Context context, WifiP2pManager wifiP2pMgr, WifiP2pManager.Channel wifiP2pChn) {
        if(!mEnabled) {
            Log.d(TAG, "Enabling Peer Discoverer");
            mContext = context;
            mWifiP2pManager = wifiP2pMgr;
            mWifiP2pChannel = wifiP2pChn;

            mStateTracker.addObserver(this);
            mConnectivityTracker.addObserver(this);

            mEnabled = true;
        } else
            Log.w(TAG, "Attempt to register a second time.");
    }

    synchronized void disable() {
        if(mEnabled) {
            Log.d(TAG, "Disabling Peer Discoverer");
            stop();

            mStateTracker.deleteObserver(this);
            mConnectivityTracker.deleteObserver(this);

            mWifiP2pEnabled = false;
            mWifiP2pConnected = false;

            mEnabled = false;
        } else
            Log.w(TAG, "Attempt to unregister a second time.");
    }

    private synchronized void start() {
        if(mEnabled && !mStarted) {
            Log.d(TAG, "Starting Discovery");
            mStarted = true;
            mContext.registerReceiver(mPeerChangeTracker, mPeerChangeIntent);
            startDiscovery();
        } else
            Log.w(TAG, "Attempt to start a second time : mEnabled=" + mEnabled + ", mDiscovering=" + mDiscovering);
    }

    private synchronized void startDiscovery() {
        if(mStarted && !mDiscovering)
            mWifiP2pManager.discoverPeers(mWifiP2pChannel, afterDiscoverPeers);
    }

    private synchronized void stopDiscovery() {
        if(mDiscovering)
            mWifiP2pManager.stopPeerDiscovery(mWifiP2pChannel, afterStopDiscoverPeers);
    }

    private synchronized void stop() {
        if(mEnabled && mStarted) {
            Log.d(TAG, "Stopping Discovery");
            mStarted = false;

            mContext.unregisterReceiver(mPeerChangeTracker);

            clearDevices();
        } else
            Log.w(TAG, "Attempt to clearDevices a second time : mEnabled=" + mEnabled + ", mDiscovering=" + mDiscovering);
    }

    Map<String, WifiP2pDevice> getDevices() {return mDevices;}

    private void clearDevices() {
        for(WifiP2pDevice current : mDevices.values()) current.markAsLost();
        setChanged(); notifyObservers();
    }

    private void updatePeerList(WifiP2pDeviceList scanResult) {
        Map<String, WifiP2pDevice> newScanResult = new HashMap<>();

        Collection<android.net.wifi.p2p.WifiP2pDevice> availablePeers = scanResult.getDeviceList();

        StringBuilder strBld = new StringBuilder();
        for (android.net.wifi.p2p.WifiP2pDevice current : availablePeers) {
            newScanResult.put(current.deviceAddress, WifiP2pDevice.convert(current));
            strBld.append(current.deviceAddress).append("   ");
        }

        Log.d(TAG, "Scan Result : " + strBld.toString());

        // Mark disappeared devices as LOST.
        for(WifiP2pDevice current : mDevices.values())
            if(!newScanResult.containsValue(current))
                current.markAsLost();

        mDevices.putAll(newScanResult);

        setChanged(); notifyObservers();
    }

    private WifiP2pManager.ActionListener afterDiscoverPeers = new WifiP2pManager.ActionListener() {
        @Override public void onSuccess() {Log.v(TAG, "discoverPeers() succeeded");}
        @Override public void onFailure(int i) {Log.v(TAG, "discoverPeers() failed");}
    };

    private WifiP2pManager.ActionListener afterStopDiscoverPeers = new WifiP2pManager.ActionListener() {
        @Override public void onSuccess() {Log.v(TAG, "stopPeerDiscovery() succeeded");}
        @Override public void onFailure(int i) {Log.v(TAG, "stopPeerDiscovery() failed");}
    };

    @Override
    public void update(Observable observable, Object obj) {
        if(observable instanceof WifiP2pStateTracker) {
            mWifiP2pEnabled = (boolean) obj;
            if (mWifiP2pEnabled) start();
            else stop();


        } else if (observable instanceof WifiP2pConnectivityTracker) {
            mWifiP2pConnected = (boolean) obj;
            Log.d(TAG, "Connection change : " + (mWifiP2pConnected ? "CONNECTED" : "DISCONNECTED"));
            if(!mWifiP2pConnected) startDiscovery();
        }
    }

    private class PeerChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                WifiP2pDeviceList devList = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
                if(devList != null)
                    updatePeerList(devList);
                else
                    Log.w(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION did not contain a PeerList");
            }
        }
    }
}