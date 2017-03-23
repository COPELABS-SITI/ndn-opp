/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-21
 * The DiscoveryListener used by the ServiceTracker.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.nsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.android.NsdService;
import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pConnectivityTracker;

// @TODO: figure out if the observed micro-cuts [LOST =(1-2seconds)= FOUND] can be safely concealed.
// @TODO: Services are sometimes lost for longer period of time ...
public class NsdServiceTracker extends Observable implements Observer {
    private static final String TAG = NsdServiceTracker.class.getSimpleName();

    private static NsdServiceTracker INSTANCE = null;

    private NsdManager mNsdManager;
    private String mAssignedUuid;

    private boolean mEnabled = false;
    private boolean mStarted = false;

    private final DiscoveryListener mListener = new DiscoveryListener();
    private final NsdServiceResolver mResolver = new NsdServiceResolver();

    private final WifiP2pConnectivityTracker mConnectivityTracker = WifiP2pConnectivityTracker.getInstance();

    // Associates a UUID to a NsdService.
    private Map<String, NsdService> mServices = new HashMap<>();

    private NsdServiceTracker() {}

    public static NsdServiceTracker getInstance() {
        if(INSTANCE == null)
            INSTANCE = new NsdServiceTracker();
        return INSTANCE;
    }

    public synchronized void enable(Context context, String uuid) {
        if (!mEnabled) {
            Log.i(TAG, "Enabling");
            mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
            mAssignedUuid = uuid;

            mResolver.addObserver(this);
            mResolver.enable(mNsdManager);

            mConnectivityTracker.addObserver(this);

            mEnabled = true;
        } else
            Log.i(TAG, "Enabling TWICE");
    }

    public synchronized void disable() {
        if (mEnabled) {
            Log.i(TAG, "Disabling");
            stop();

            mResolver.disable();
            mResolver.deleteObserver(this);

            mConnectivityTracker.deleteObserver(this);

            mEnabled = false;
        } else
            Log.i(TAG, "Disabling TWICE");
    }

    private synchronized void start() {
        if(mEnabled && !mStarted) {
            Log.i(TAG, "Starting");
            mNsdManager.discoverServices(NsdService.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mListener);
            mStarted = true;
        } else
            Log.i(TAG, "Starting TWICE");
    }

    private synchronized void stop() {
        if(mEnabled && mStarted) {
            Log.i(TAG, "Stopping");

            mNsdManager.stopServiceDiscovery(mListener);

            for (NsdService svc : mServices.values())
                svc.markAsUnavailable();

            setChanged(); notifyObservers();
            mStarted = false;
        } else
            Log.i(TAG, "Stopping TWICE");
    }

    public Map<String, NsdService> getServices() {
        return mServices;
    }

    private void resolutionCompleted(NsdServiceInfo descriptor) {
        String svcUuid = descriptor.getServiceName();

        NsdService svc;
        if(mServices.containsKey(svcUuid))
            svc = mServices.get(descriptor.getServiceName());
        else {
            Log.w(TAG, "Resolution of a Service not found in mServices.");
            svc = new NsdService(svcUuid);
            mServices.put(svcUuid, svc);
        }

        svc.resolved(descriptor);

        setChanged(); notifyObservers(svc);
    }

    /** D3.1 & D3.3 & D5.3 */

    @Override
    public void update(Observable observable, Object obj) {
        if (observable instanceof WifiP2pConnectivityTracker) {
            boolean isConnected = (boolean) obj;
            Log.d(TAG, "Connection change : " + (isConnected ? "CONNECTED" : "DISCONNECTED"));

            if(isConnected) start();
            else stop();


        } else if(observable instanceof NsdServiceResolver)
            resolutionCompleted((NsdServiceInfo) obj);
        else
            Log.w(TAG, "Update from unknown Observable " + observable.getClass());
    }

    private class DiscoveryListener implements NsdManager.DiscoveryListener {
        @Override
        public void onStartDiscoveryFailed(String s, int error) {
            Log.e(TAG, "Start err" + error);
        }

        @Override
        public void onStopDiscoveryFailed(String s, int error) {
            Log.e(TAG, "Stop err" + error);
        }

        @Override public void onDiscoveryStarted(String regType) {
            Log.d(TAG, "Started : " + regType);
        }

        @Override public void onDiscoveryStopped(String regType) {
            Log.d(TAG, "Stopped : " + regType);
        }

        @Override
        public void onServiceFound(NsdServiceInfo descriptor) {
            String svcUuid = descriptor.getServiceName();

            Log.d(TAG, "ServiceFound " + svcUuid + " @ " + descriptor.getHost() + ":" + descriptor.getPort());

            if( !mAssignedUuid.equals(svcUuid)) {
                if(!mServices.containsKey(svcUuid)) {
                    NsdService svc = new NsdService(svcUuid);
                    mServices.put(svcUuid, svc);
                }

                mResolver.resolve(descriptor);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo descriptor) {
            String svcUuid = descriptor.getServiceName();
            if( !mAssignedUuid.equals(svcUuid)) {
                Log.d(TAG, "ServiceLost " + svcUuid);

                if(mServices.containsKey(svcUuid)) {
                    NsdService svc = mServices.get(svcUuid);
                    svc.markAsUnavailable();
                    setChanged(); notifyObservers(svc);
                }
            }
        }
    }
}
