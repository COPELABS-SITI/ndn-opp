package pt.ulusofona.copelabs.ndn.android.umobile.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Observable;

public class WifiP2pConnectivityTracker extends Observable {
    private static final String TAG = WifiP2pConnectivityTracker.class.getSimpleName();

    private final IntentFilter mIntents = new IntentFilter();
    private final ConnectionIntentReceiver mIntentReceiver = new ConnectionIntentReceiver();

    private Context mContext = null;
    private boolean mEnabled = false;

    private boolean mConnected = false;
    private String mLastAccessPoint = null;

    public WifiP2pConnectivityTracker() {
        mIntents.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    public synchronized void enable(Context ctxt) {
        if(!mEnabled) {
            mContext = ctxt;
            mContext.registerReceiver(mIntentReceiver, mIntents);
            mEnabled = true;
        }
    }

    public synchronized void disable() {
        if(mEnabled) {
            mContext.unregisterReceiver(mIntentReceiver);
            mContext = null;
            mConnected = false;
            mLastAccessPoint = null;
            mEnabled = false;
        }
    }

    private class ConnectionIntentReceiver extends BroadcastReceiver {
        private boolean changeDetected(String lastNetworkName, String currentNetworkName) {
            boolean changeDetected;

            if(lastNetworkName != null)
                changeDetected = !lastNetworkName.equals(currentNetworkName);
            else
                changeDetected = (currentNetworkName != null);

            Log.d(TAG, "Change detected : " + changeDetected);

            return changeDetected;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received Intent : " + action);

            if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                WifiP2pGroup wifip2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

                String currentGroupName = wifip2pGroup.getNetworkName();

                Log.v(TAG, "NetworkInfo : " + netInfo);
                Log.v(TAG, "WifiP2pInfo : " + wifiP2pInfo);
                Log.v(TAG, "WifiP2pGroup : " + wifip2pGroup);

                if(changeDetected(mLastAccessPoint, currentGroupName)) {
                    mConnected = currentGroupName != null;
                    setChanged(); notifyObservers(mConnected);
                }

                mLastAccessPoint = currentGroupName;
            }
        }
    }
}