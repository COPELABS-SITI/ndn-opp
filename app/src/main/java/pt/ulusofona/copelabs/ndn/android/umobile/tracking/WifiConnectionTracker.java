/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * //TODO: Description.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.Observable;

import pt.ulusofona.copelabs.ndn.android.umobile.Routing;

class WifiConnectionTracker {
    private static final String TAG = WifiConnectionTracker.class.getSimpleName();

    private ConnectionEventReceiver mConnectionTracker = new ConnectionEventReceiver();
    private IntentFilter mWifiEvents = new IntentFilter();
    private boolean mEnabled = false;

    private ServiceRegistrar mRegistrar;
    private ServiceDiscoverer mDiscoverer;

    WifiConnectionTracker(ServiceTracker tracker, Routing rt) {
        mWifiEvents.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        mRegistrar = new ServiceRegistrar(tracker.assignedUuid, rt);
        mDiscoverer = new ServiceDiscoverer(tracker);
    }

    public synchronized void enable(Context ctxt) {
        if(!mEnabled) {
            ctxt.registerReceiver(mConnectionTracker, mWifiEvents);
            mEnabled = true;
        }
    }

    public synchronized void disable(Context ctxt) {
        ctxt.unregisterReceiver(mConnectionTracker);
        mRegistrar.disable();
        mDiscoverer.disable();
    }

    private class ConnectionEventReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                Log.d(TAG, "Network state changed : isConnected() = " + netInfo.isConnected());
                if (netInfo.isConnected()) {
                    mRegistrar.enable(context);
                    mDiscoverer.enable(context);
                } else {
                    mRegistrar.disable();
                    mDiscoverer.disable();
                }
            }
        }
    }
}
