/*
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017/9/7.
 * Class is part of the NSense application.
 */

package pt.ulusofona.copelabs.ndn.android.wifi.p2p;


import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import static android.net.wifi.WifiManager.WIFI_STATE_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;

/**
 * This class is used to instantiate wifi p2p features
 * @author Miguel Tavares (COPELABS/ULHT)
 * @version 1.0, 2017
 */
public class WifiP2p {

    /** This variable is used to debug WifiP2p class */
    private static final String TAG = "WifiP2p";

    /** This object schedules the different stages of wifi and wifi p2p components */
    private WifiP2pScheduler mWifiP2pScheduler;

    /** This object manage the stages of wifi and wifi p2p */
    private WifiP2pReceiver mWifiP2pReceiver;

    /** This object does a service and peer discoveries */
    private WifiP2pSearcher mWifiP2pSearcher;

    /** This object enables the wifi p2p GO features */
    private WifiP2pGo mWifiP2pGo;

    /** This object stores the application context */
    private Context mContext;

    public WifiP2p(Context context) {
        mContext = context;
        mWifiP2pGo = new WifiP2pGo(context);
        mWifiP2pSearcher = new WifiP2pSearcher(context);
        mWifiP2pReceiver = new WifiP2pReceiver(mWifiP2pSearcher, mWifiP2pGo);
        mWifiP2pScheduler = new WifiP2pScheduler(mWifiP2pGo, mWifiP2pSearcher);
    }

    /**
     * This method starts this component
     */
    public void start() {
        Log.i(TAG, "Start");
        mWifiP2pScheduler.start();
        mContext.registerReceiver(mWifiP2pReceiver, buildIntentFilter());
    }

    /**
     * This method stops this component
     */
    public void stop() {
        Log.i(TAG, "Stop");
        mWifiP2pScheduler.stop();
        mContext.unregisterReceiver(mWifiP2pReceiver);
    }

    /**
     * This method builds the needed intent for the receiver
     * @return filtered intent
     */
    private IntentFilter buildIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        intentFilter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
        return intentFilter;
    }

}
