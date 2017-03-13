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

import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.android.UmobileService;
import pt.ulusofona.copelabs.ndn.android.UmobileService.Status;

class ServiceDiscoverer extends Observable {
    private static final String TAG = ServiceDiscoverer.class.getSimpleName();

    private final ServiceTracker mTracker;
    private NsdManager mNsdManager;

    private boolean mDiscovering = false;
    private DiscoveryListener mListener = new DiscoveryListener();
    private ServiceResolver mResolver;

    ServiceDiscoverer(ServiceTracker tracker) {
        mTracker = tracker;
        mResolver = new ServiceResolver(tracker);
    }

    public void enable(Context ctxt) {
        if (!mDiscovering) {
            mNsdManager = (NsdManager) ctxt.getSystemService(Context.NSD_SERVICE);
            mResolver.enable(mNsdManager);
            mNsdManager.discoverServices(ServiceTracker.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mListener);
        }
    }

    public void disable() {
        if (mDiscovering) {
            mNsdManager.stopServiceDiscovery(mListener);
            mResolver.disable();
        }
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
            String svcName = descriptor.getServiceName();
            if( ! mTracker.assignedUuid.equals(svcName)) {
                Log.d(TAG, "ServiceFound " + svcName);
                mResolver.resolve(descriptor);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo descriptor) {
            String svcName = descriptor.getServiceName();
            if( ! mTracker.assignedUuid.equals(svcName)) {
                Log.d(TAG, "ServiceLost " + svcName);
                mTracker.updateService(svcName, Status.UNAVAILABLE, ServiceTracker.UNKNOWN_HOST, ServiceTracker.UNKNOWN_PORT);
                UmobileService svc = new UmobileService(Status.UNAVAILABLE, svcName, ServiceTracker.UNKNOWN_HOST, ServiceTracker.UNKNOWN_PORT);
                setChanged(); notifyObservers(svc);
            }
        }
    }

}
