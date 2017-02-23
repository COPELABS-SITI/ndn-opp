/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-21
 * The ResolveListener used by the ServiceTracker.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.tracking;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.UmobileService.Status;

class ServiceResolver implements NsdManager.ResolveListener {
    private static final String TAG = ServiceRegistrar.class.getSimpleName();

    private ServiceTracker mTracker;

    ServiceResolver(ServiceTracker t) {
        mTracker = t;
    }

    @Override
    public void onServiceResolved(NsdServiceInfo descriptor) {
        mTracker.updateService(descriptor.getServiceName(), Status.AVAILABLE, descriptor.getHost().getHostAddress(), descriptor.getPort());
    }

    @Override
    public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int error) {
        Log.d(TAG, "Resolution err" + error + " : " + nsdServiceInfo.getServiceName());
        // TODO: retry logic ?
    }

}
