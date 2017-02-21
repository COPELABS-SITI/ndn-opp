/*
 * *
 *  *  @version 1.0
 *  * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 *  * //TODO: Description.
 *  * @author Seweryn Dynerowicz (COPELABS/ULHT)
 *
 */

package pt.ulusofona.copelabs.ndn.android.umobile.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;

class WifiStateTracker extends BroadcastReceiver {
    private ServiceTracker mTracker;

    WifiStateTracker(ServiceTracker serviceTracker) {
        mTracker = serviceTracker;
    }

    private void enable() {
        if (!mTracker.mRegistered)
            mTracker.mNsdManager.registerService(mTracker.mDescriptor, NsdManager.PROTOCOL_DNS_SD, mTracker.mRegistrationListener);

        if (!mTracker.mDiscovering)
            mTracker.mNsdManager.discoverServices(ServiceTracker.SVC_INSTANCE_TYPE, NsdManager.PROTOCOL_DNS_SD, mTracker.mDiscoveryListener);
    }

    public void disable() {
        if (mTracker.mRegistered)
            mTracker.mNsdManager.unregisterService(mTracker.mRegistrationListener);

        if (mTracker.mDiscovering)
            mTracker.mNsdManager.stopServiceDiscovery(mTracker.mDiscoveryListener);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
            NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (netInfo.isConnected()) enable();
            else disable();
        }
    }
}
