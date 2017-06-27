/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-21
 * The NsdServiceTracker centralizes the functionality of maintaining up to date the list of
 * Services that are known in the network we're part of.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.nsd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.android.models.NsdService;

// @TODO: figure out if the observed micro-cuts [LOST =(1-2seconds)= FOUND] can be safely concealed.
// @TODO: Services are sometimes lost for longer period of time ...

/** Implementation of a tracker which maintains a list of NSD services detected in the same
 * Wi-Fi Direct Group along with their status. This list is maintained even after the current
 * device leaves the Wi-Fi Direct Group.
 */
public class NsdServiceDiscoverer extends Observable implements Observer {
    private static final String TAG = NsdServiceDiscoverer.class.getSimpleName();

    private static NsdServiceDiscoverer INSTANCE = null;

    private Context mContext;

    private NsdManager mNsdManager;
    private String mAssignedUuid;

    private boolean mEnabled = false;
    private boolean mStarted = false;

    private final DiscoveryListener mListener = new DiscoveryListener();
    private final NsdServiceResolver mResolver = new NsdServiceResolver();

    private final ConnectivityEventDetector mConnectivityDetector = new ConnectivityEventDetector();

    // Associates a UUID to a NsdService.
    private Map<String, NsdService> mServices = new HashMap<>();

    private NsdServiceDiscoverer() {}

    public static NsdServiceDiscoverer getInstance() {
        if(INSTANCE == null)
            INSTANCE = new NsdServiceDiscoverer();
        return INSTANCE;
    }

    /** Enable this NSD Service Tracker. When enabled, the tracker will react to Wi-Fi Direct Group
     * connections by activating service tracking within that Group and update its list of NSD Services
     * accordingly.
     * @param context the Android context within which the tracker should be activated.
     * @param uuid the UUID of the current device. Used for filtering out from the detected services.
     */
    public synchronized void enable(Context context, String uuid) {
        if (!mEnabled) {
            Log.i(TAG, "Enabling");
            mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
            mAssignedUuid = uuid;

            mResolver.addObserver(this);
            mResolver.enable(mNsdManager);

            mContext = context;
            mContext.registerReceiver(mConnectivityDetector, new IntentFilter(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION));

            mEnabled = true;
        } else
            Log.i(TAG, "Enabling TWICE");
    }

    /** Disable the tracker. Future Group connections will be ignored as well as notifigations from the Service Tracker. */
    public synchronized void disable() {
        if (mEnabled) {
            Log.i(TAG, "Disabling");
            stop();

            mResolver.disable();
            mResolver.deleteObserver(this);

            mContext.unregisterReceiver(mConnectivityDetector);

            mEnabled = false;
        } else
            Log.i(TAG, "Disabling TWICE");
    }

    /** Starts the tracker; services will be discovered on the current Wi-Fi Direct Group. */
    private synchronized void start() {
        if(mEnabled && !mStarted) {
            Log.i(TAG, "Starting");
            mNsdManager.discoverServices(NsdService.SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mListener);
            mStarted = true;
        } else
            Log.i(TAG, "Starting TWICE");
    }

    /** Stops the tracker; services detected on the current Wi-Fi Direct Group will no longer be updated. */
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

    /** Retrieve the list of all NSD services ever detected across all Wi-Fi Direct Groups.
     * @return list of NSD services
     */
    public Map<String, NsdService> getServices() {
        return mServices;
    }

    /** Invoked upon resolution completion to update the NSD service details
     * with the new information available about it.
     * @param descriptor relevant informations describing the service details that have changed
     */
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

    /** Observer method to process notifications from Observables
     * @param observable observable which notified of a change
     * @param obj optional parameter passed by the observable as part of its notification
     */
    @Override
    public void update(Observable observable, Object obj) {
        if(observable instanceof NsdServiceResolver)
            // Update the NSD Service upon resolution completion
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

    private class ConnectivityEventDetector extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                boolean wifiP2pConnected = netInfo.isConnected();
                Log.v(TAG, "Connection changed : " + (wifiP2pConnected ? "CONNECTED" : "DISCONNECTED"));

                // When the current device joins a Wi-Fi Direct Group, start detecting services,
                if(wifiP2pConnected) start();
                // When the current device leaves the Wi-Fi Direct Group, stop detecting services.
                else stop();
            }
        }
    }
}
