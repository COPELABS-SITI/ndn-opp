/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * //TODO: Description.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.tracking;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.LinkedList;
import java.util.Queue;

import pt.ulusofona.copelabs.ndn.android.UmobileService;

class ServiceResolver {
    private static final String TAG = ServiceResolver.class.getSimpleName();

    private ServiceTracker mTracker;
    private NsdManager mNsdManager;

    private boolean mResolving = false;
    private ResolutionListener mListener = new ResolutionListener();

    private Queue<NsdServiceInfo> mQueue = new LinkedList<>();

    ServiceResolver(ServiceTracker tracker) {
        mTracker = tracker;
    }

    public void enable(NsdManager nsdMgr) {
        mNsdManager = nsdMgr;
    }

    public void disable() {
        mQueue.clear();
        mResolving = false;
    }

    private synchronized void resolveNext() {
        if(mQueue.isEmpty())
            mResolving = false;
        else
            mNsdManager.resolveService(mQueue.remove(), mListener);
    }

    public synchronized void resolve(NsdServiceInfo descriptor) {
        if(mResolving)
            mQueue.add(descriptor);
        else {
            mResolving = true;
            mNsdManager.resolveService(descriptor, mListener);
        }
    }

    private class ResolutionListener implements NsdManager.ResolveListener {
        @Override
        public void onServiceResolved(NsdServiceInfo descriptor) {
            mTracker.updateService(descriptor.getServiceName(), UmobileService.Status.AVAILABLE, descriptor.getHost().getHostAddress(), descriptor.getPort());
            resolveNext();
        }

        @Override
        public void onResolveFailed(NsdServiceInfo descriptor, int error) {
            Log.d(TAG, "Resolution err" + error + " : " + descriptor.getServiceName());
            // TODO: retry logic ?
            resolveNext();
        }
    }
}
