/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * The NsdServiceResolver is in charge of actualling calling the resolution function of Android.
 * The main reason for this class is that it seems to be necessary to perform resolution of one service
 * at a time even when multiple services are discovered at the same time. (i.e. performing two resolutions
 * at the same time makes the second one fail)
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.nsd;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.LinkedList;
import java.util.Observable;
import java.util.Queue;

/** Implementation of a resolver which serializes resolution operations upon detection of new
 * services in the same Wi-Fi Direct Group.
 */
class NsdServiceResolver extends Observable {
    private static final String TAG = NsdServiceResolver.class.getSimpleName();

    private boolean mEnabled = false;
    private NsdManager mNsdManager;

    private boolean mResolutionPending = false;
    private ResolutionListener mListener = new ResolutionListener();

    private Queue<NsdServiceInfo> mQueue = new LinkedList<>();

    /** Enable the resolver; services that are detected will be resolved serially.
     * @param nsdMgr
     */
    synchronized void enable(NsdManager nsdMgr) {
        if(!mEnabled) {
            mNsdManager = nsdMgr;
            mEnabled = true;
        }
    }

    /** Disable the resolver. All pending resolution are dropped.
     */
    synchronized void disable() {
        if(mEnabled) {
            mQueue.clear();
            mEnabled = false;
            mResolutionPending = false;
        }
    }

    /** Effectively perform the resolution of the next service in the queue.
     */
    private synchronized void resolveNext() {
        if(mQueue.isEmpty()) {
            mResolutionPending = false;
        } else
            mNsdManager.resolveService(mQueue.remove(), mListener);
    }

    /** Used to request resolution of a service. If a resolution is underway, the resolution is placed in a queue.
     * @param descriptor partial details of the newly discovered service
     */
    synchronized void resolve(NsdServiceInfo descriptor) {
        if(mResolutionPending) {
            Log.d(TAG, "Queueing descriptor.");
            mQueue.add(descriptor);
        } else {
            Log.d(TAG, "Resolving descriptor.");
            mResolutionPending = true;
            mNsdManager.resolveService(descriptor, mListener);
        }
    }

    private class ResolutionListener implements NsdManager.ResolveListener {
        @Override
        public void onServiceResolved(NsdServiceInfo descriptor) {
            Log.d(TAG, "ServiceResolved : " + descriptor.getServiceName() + " @ " + descriptor.getHost() + ":" + descriptor.getPort());

            setChanged(); notifyObservers(descriptor);

            resolveNext();
        }

        @Override
        public void onResolveFailed(NsdServiceInfo descriptor, int error) {
            Log.d(TAG, "Resolution err" + error + " : " + descriptor.getServiceName());
            resolveNext();
        }
    }
}
