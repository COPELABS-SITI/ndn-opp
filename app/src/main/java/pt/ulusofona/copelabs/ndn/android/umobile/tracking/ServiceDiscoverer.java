/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-21
 * The DiscoveryListener used by the ServiceTracker.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.tracking;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.UmobileService.Status;

class ServiceDiscoverer implements NsdManager.DiscoveryListener {
    private static final String TAG = ServiceDiscoverer.class.getSimpleName();

    private NsdManager mNsdManager;
    private ServiceTracker mTracker;
    private boolean mDiscovering = false;

    ServiceDiscoverer(ServiceTracker o, NsdManager n) {
        mTracker = o;
        mNsdManager = n;
    }

    boolean isDiscovering() {
        return mDiscovering;
    }

    @Override
    public void onStartDiscoveryFailed(String s, int error) {Log.d(TAG, "Start err" + error);}

    @Override
    public void onStopDiscoveryFailed(String s, int error) {Log.d(TAG, "Stop err" + error);}

    @Override
    public void onDiscoveryStarted(String regType) {
        Log.d(TAG, "Started : " + regType);
        mDiscovering = true;
    }

    @Override
    public void onDiscoveryStopped(String regType) {
        Log.d(TAG, "Stopped : " + regType);
        mDiscovering = false;
    }

    @Override
    public void onServiceFound(NsdServiceInfo descriptor) {
        mTracker.addUnresolvedService(descriptor.getServiceName());
        mNsdManager.resolveService(descriptor, new ServiceResolver(mTracker));
    }

    @Override
    public void onServiceLost(NsdServiceInfo descriptor) {
        Log.d(TAG, "ServiceLost " + descriptor.getServiceName());
        mTracker.updateService(descriptor.getServiceName(), Status.UNAVAILABLE, ServiceTracker.UNKNOWN_HOST, ServiceTracker.UNKNOWN_PORT);
    }
}
