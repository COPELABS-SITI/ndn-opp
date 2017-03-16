package pt.ulusofona.copelabs.ndn.android.umobile.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Observable;

public class WifiP2pStateTracker extends Observable {
    private static final String TAG = WifiP2pStateTracker.class.getSimpleName();

    private final IntentFilter mIntents = new IntentFilter();
    private ConnectionEventTracker mGroupEventTracker = new ConnectionEventTracker();

    private Context mContext;
    private boolean mEnabled = false;

    public WifiP2pStateTracker() {
        mIntents.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
    }

    public synchronized void enable(Context ctxt) {
        if(!mEnabled) {
            mContext = ctxt;
            mContext.registerReceiver(mGroupEventTracker, mIntents);
            mEnabled = true;
        }
    }

    public synchronized void disable() {
        if(mEnabled) {
            mContext.unregisterReceiver(mGroupEventTracker);
            mContext = null;
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
