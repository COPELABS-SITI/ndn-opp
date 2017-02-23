/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-21
 * The DiscoveryListener used by the ServiceTracker.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.tracking;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;

import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.UmobileService.Status;

class ServiceDiscoverer {
    private static final String TAG = ServiceDiscoverer.class.getSimpleName();

    private ServiceTracker mTracker;
    private NsdManager mNsdManager;

    private boolean mDiscovering = false;
    private DiscoveryListener mListener = new DiscoveryListener();

    ServiceDiscoverer(ServiceTracker o) {
        mTracker = o;
    }

    public void enable(Context ctxt) {
        if (!mDiscovering) {
            mNsdManager = (NsdManager) ctxt.getSystemService(Context.NSD_SERVICE);
            mNsdManager.discoverServices(ServiceTracker.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mListener);
        }
    }

    public void disable() {
        if (mDiscovering)
            mNsdManager.stopServiceDiscovery(mListener);
    }

    private class DiscoveryListener implements NsdManager.DiscoveryListener {
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
            if( ! mTracker.assignedUuid.equals(descriptor.getServiceName())) {
                Log.d(TAG, "ServiceFound " + descriptor.getServiceName());
                mNsdManager.resolveService(descriptor, new ResolveListener());
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo descriptor) {
            Log.d(TAG, "ServiceLost " + descriptor.getServiceName());
            mTracker.updateService(descriptor.getServiceName(), Status.UNAVAILABLE, ServiceTracker.UNKNOWN_HOST, ServiceTracker.UNKNOWN_PORT);
        }
    }

    private class ResolveListener implements NsdManager.ResolveListener {
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
}
