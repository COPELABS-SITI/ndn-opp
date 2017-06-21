/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class is in charge of WiFi P2P device discovery
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/** The WifiP2pDeviceDiscoverer wraps up the peer discovery process of Android and includes additional
 * logic around it. Specifically, once it is enabled, it reacts to changes in the WifiP2pState (ON/OFF) and
 * the WifiP2pConnectivity (CONNECTED/DISCONNECTED).
 *
 * WifiP2pState is indirectly toggled by enabling/disabling Wi-Fi on the Android device.
 * When WifiP2pState becomes ON, we start peer discovery
 * When WifiP2pState becomes OFF, we stop peer discovery and mark all devices as UNAVAILABLE
 *
 * WifiP2pConnectivity reflects whether the current device is connected or not to a Wi-Fi Direct Group
 * When WifiP2pConnectivity becomes CONNECTED, Android stops peer discovery automatically
 *   (cfr. android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION)
 * When WifiP2pConnectivity becomes DISCONNECTED, we start peer discovery
 *
 * When a new Peer List is available from the framework, the WifiP2pDeviceDiscoverer performs an update to
 * its list of known devices according to the following rules;
 *
 * - A device seen for the first time is added to the list with the information reported by the framework
 * - A device which is not in the new Peer List is marked as LOST.
 */
class WifiP2pDeviceDiscoverer extends Observable {
    private static final String TAG = WifiP2pDeviceDiscoverer.class.getSimpleName();

    // Android provided
    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    private final DeviceDiscovererEventDetector mEventDetector = new DeviceDiscovererEventDetector();

    private boolean mEnabled = false;
    private boolean mDiscovering = false;

    // Associates a MAC address to a WifiP2pDevice
    private Map<String, WifiP2pDevice> mDevices = new HashMap<>();

    /** Enable the Device Discoverer so that it reacts to changes in the WifiP2pState, WifiP2pConnectivity
     *  and the Peer List reported by Android. The parameters are the ones obtained from the Android API
     * @param context context within which the Device Discoverer is enabled
     * @param wifiP2pMgr WifiP2pManager to which the Device Discoverer is attached
     * @param wifiP2pChn WifiP2pManager.Channel in which the Device Discoverer is listening
     */
    synchronized void enable(Context context, WifiP2pManager wifiP2pMgr, WifiP2pManager.Channel wifiP2pChn) {
        if(!mEnabled) {
            Log.d(TAG, "Enabling Peer Discoverer");
            mContext = context;
            mWifiP2pManager = wifiP2pMgr;
            mWifiP2pChannel = wifiP2pChn;

            IntentFilter intents = new IntentFilter();
            intents.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
            intents.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
            intents.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
            intents.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            mContext.registerReceiver(mEventDetector, intents);

            mEnabled = true;
        } else
            Log.w(TAG, "Attempt to register a second time.");
    }

    /** Disable the Device Discoverer. All changes to WifiP2pState, WifiP2pConnectivity and the Peer List
     *  will be ignored in this state. */
    synchronized void disable() {
        if(mEnabled) {
            Log.d(TAG, "Disabling Peer Discoverer");
            stop();

            mContext.unregisterReceiver(mEventDetector);

            mEnabled = false;
        } else
            Log.w(TAG, "Attempt to unregister a second time.");
    }

    /** Start the device discoverer. */
    private synchronized void start() {
        if(mEnabled && !mDiscovering) {
            Log.d(TAG, "Initiating Peer Discovery");
            mWifiP2pManager.discoverPeers(mWifiP2pChannel, null);
        } else
            Log.w(TAG, "Attempt to initiating a second time : mEnabled=" + mEnabled + ", mDiscovering=" + mDiscovering);
    }

    private synchronized void stop() {
        if(mEnabled && mDiscovering) {
            Log.d(TAG, "Stopping Discovery");
            mWifiP2pManager.stopPeerDiscovery(mWifiP2pChannel, null);
        } else
            Log.w(TAG, "Attempt to stop a second time : mEnabled=" + mEnabled + ", mDiscovering=" + mDiscovering);
    }

    Map<String, WifiP2pDevice> getDevices() {return mDevices;}

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

    private class DeviceDiscovererEventDetector extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();


            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int wifiP2pState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                if(wifiP2pState == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.v(TAG, "WiFi P2P State : ON");
                    start();
                }
                else if (wifiP2pState == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                    Log.v(TAG, "WiFi P2P State : OFF");
                    stop();
                }


            } else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                int wifiP2pDiscoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
                mDiscovering = (wifiP2pDiscoveryState == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED);
                Log.v(TAG, "Wi-Fi Discovery : " + (mDiscovering ? "STARTED" : "STOPPED"));
                if(!mDiscovering)
                    start();


            } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                WifiP2pDeviceList devList = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
                if(devList != null)
                    updatePeerList(devList);
                else
                    Log.w(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION did not contain a PeerList");


            } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                boolean wifiP2pConnected = netInfo.isConnected();
                Log.v(TAG, "Connection changed : " + (wifiP2pConnected ? "CONNECTED" : "DISCONNECTED"));

                if(! wifiP2pConnected) start();
            }
        }
    }
}