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
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;

class WifiStateTracker extends BroadcastReceiver {
    private ServiceTracker mTracker;
    private NsdManager mNsdManager;
    private ServiceRegistrar mRegistrar;
    private ServiceDiscoverer mDiscoverer;

    WifiStateTracker(ServiceTracker serviceTracker, NsdManager nsdManager) {
        mRegistrar = new ServiceRegistrar(serviceTracker);
        mDiscoverer = new ServiceDiscoverer(serviceTracker, nsdManager);
        mNsdManager = nsdManager;
        mTracker = serviceTracker;
    }

    private void enable() {
        if (!mRegistrar.isRegistered())
            mNsdManager.registerService(mTracker.mDescriptor, NsdManager.PROTOCOL_DNS_SD, mRegistrar);

        if (!mDiscoverer.isDiscovering())
            mNsdManager.discoverServices(ServiceTracker.SVC_INSTANCE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoverer);
    }

    public void disable() {
        if (mRegistrar.isRegistered())
            mNsdManager.unregisterService(mRegistrar);

        if (mDiscoverer.isDiscovering())
            mNsdManager.stopServiceDiscovery(mDiscoverer);
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
