/*
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017/9/7.
 * Class is part of the NSense application.
 */

package pt.ulusofona.copelabs.ndn.android.wifi.p2p;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;

/**
 * This class is responsible for manage wifi p2p features according the wifi
 * interface status.
 * @author Miguel Tavares (COPELABS/ULHT)
 * @version 1.0, 2017
 */
public class WifiP2pReceiver extends BroadcastReceiver {

    /** This variable is used to debug WifiP2pReceiver class */
    private static final String TAG = "WifiP2pReceiver";

    /** This object is a reference to manage WifiP2pSearcher features */
    private WifiP2pSearcher mWifiP2pSearcher;

    /** This object is a reference to manage WifiP2pGo features */
    private WifiP2pGo mWifiP2pGo;

    public WifiP2pReceiver(WifiP2pSearcher wifiP2pSearcher, WifiP2pGo wifiP2pGo) {
        mWifiP2pSearcher = wifiP2pSearcher;
        mWifiP2pGo = wifiP2pGo;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                mWifiP2pGo.clearLocalServices();
                mWifiP2pGo.removeGroup();
            }
        } else if (WIFI_STATE_CHANGED_ACTION.equals(action)) {
            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if(wifi.isWifiEnabled()) {
                mWifiP2pSearcher.addServiceRequest();
            } else {
                mWifiP2pSearcher.removeServiceRequest();
                mWifiP2pGo.clearServiceRequests();
                mWifiP2pGo.clearLocalServices();
                mWifiP2pGo.removeGroup();
            }
        } else if (WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Log.i(TAG, "WIFI_P2P_PEERS_CHANGED_ACTION");
            mWifiP2pSearcher.requestPeers();
        } else if (WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            int discoveryState = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
            Log.i(TAG, "WiFi P2P Discovery Changed: " + discoveryState + " [STOPPED=1,STARTED=2,UNKNOWN=-1]");
        } else if (WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {
                Log.i(TAG, "We are connected, will check info now");
                WifiP2pListenerManager.notifyConnected(intent);
                mWifiP2pGo.requestConnectionInfo();
            } else {
                Log.i(TAG, "We are disconnected!");
                WifiP2pListenerManager.notifyDisconnected(intent);
            }
        }
    }
}
