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
 * services in the same Wi-Fi Direct Group. The resolution operation takes a simple name of a service
 * instance and performs the necessary operations to obtain the IP address and port number at which
 * the service is effectively reachable.
 *
 * This serialization is done due to the fact that Android may report multiple newly discovered services
 * that trigger concurrent resolutions, resulting in failure. */
class NsdServiceResolver extends Observable {
    private static final String TAG = NsdServiceResolver.class.getSimpleName();

    private boolean mEnabled = false;
    private NsdManager mNsdManager;

    private boolean mResolutionPending = false;
    private ResolutionListener mListener = new ResolutionListener();

    private Queue<NsdServiceInfo> mQueue = new LinkedList<>();

    /** Enable the resolver; services that are subsequently detected will be resolved serially.
     * @param nsdMgr Android-provided NsdManager
     */
    synchronized void enable(NsdManager nsdMgr) {
        if(!mEnabled) {
            mNsdManager = nsdMgr;
            mEnabled = true;
        }
    }

    /** Disable the resolver. All pending resolution are dropped. */
    synchronized void disable() {
        if(mEnabled) {
            mQueue.clear();
            mEnabled = false;
            mResolutionPending = false;
        }
    }

    /** Effectively perform the resolution of the next service in the queue. If the queue of pending
     * resolutions is empty, the resolvers stops. Otherwise, it requests the next resolution from the NsdManager
     */
    private synchronized void resolveNext() {
        if(mQueue.isEmpty()) {
            mResolutionPending = false;
        } else
            mNsdManager.resolveService(mQueue.remove(), mListener);
    }

    /** Used to request resolution of a service. If a resolution is underway, the resolution is placed in a queue.
     * Otherwise, the NsdManager is requested to perform its resolution immediately.
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

    /** Listener used to handle the resolution completion. */
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
