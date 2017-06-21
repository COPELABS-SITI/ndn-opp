/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class keeps track of WiFi P2P status advertised by Android.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Observable;

/** Implementation of a state tracker for the Wi-Fi Direct functionality of Android.
 *  Implemented as an Observable.
 */
public class WifiP2pStateTracker extends Observable {
    private static final String TAG = WifiP2pStateTracker.class.getSimpleName();
    private static WifiP2pStateTracker INSTANCE = null;

    private final IntentFilter mIntents = new IntentFilter(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
    private ConnectionEventTracker mGroupEventTracker = new ConnectionEventTracker();

    private boolean mEnabled = false;

    /** Retrieves the instance of the state tracker.
     * @return singleton instance of state tracker
     */
    public static WifiP2pStateTracker getInstance() {
        if(INSTANCE == null)
            INSTANCE = new WifiP2pStateTracker();
        return INSTANCE;
    }

    /**
     * Enable the state tracker; events related to WifiP2p being toggled ON/OFF by Android will be tracked
     * @param ctxt context within which the events should be collected
     */
    public synchronized void enable(Context ctxt) {
        if(!mEnabled) {
            ctxt.registerReceiver(mGroupEventTracker, mIntents);
            mEnabled = true;
        }
    }

    /** Disable the state tracker; events are no longer tracked
     * @param ctxt context within which the events should be collected
     */
    public synchronized void disable(Context ctxt) {
        if(mEnabled) {
            ctxt.unregisterReceiver(mGroupEventTracker);
            mEnabled = false;
        }
    }

    private class ConnectionEventTracker extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int wifiP2pState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                boolean enabled = (wifiP2pState == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
                Log.v(TAG, "WiFi P2P State : " + (enabled ? "ENABLED" : "DISABLED"));

                setChanged(); notifyObservers(enabled);
            }
        }
    }
}
