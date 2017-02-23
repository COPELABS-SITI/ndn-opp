/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * The ServiceTracker encapsulates the logic of discovering UMobile peers on the same network. It is
 * mostly intended for demo and testing purposes.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.tracking;

import android.content.Context;
import android.content.IntentFilter;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import pt.ulusofona.copelabs.ndn.android.UmobileService;
import pt.ulusofona.copelabs.ndn.android.UmobileService.Status;

public class ServiceTracker extends Observable {
    // @TODO: figure out if the observed micro-cuts [LOST =(1-2seconds)= FOUND] can be safely concealed.
    // @TODO: Services are sometimes lost for longer period of time ...
    // @TODO: assign port dynamically
    private static final String TAG = ServiceTracker.class.getSimpleName();

    static final String SVC_INSTANCE_TYPE = "_ndn._tcp";
    private static final int SVC_INSTANCE_PORT = 6364;

    static final String UNKNOWN_HOST = "0.0.0.0";
    static final int UNKNOWN_PORT = 0;

    private Context mContext;

    NsdServiceInfo mDescriptor = new NsdServiceInfo();
    final String mAssignedUuid;

    private boolean mEnabled = false;

    private Map<String, UmobileService> mServices = new HashMap<>();

    private IntentFilter mWifiIntents = new IntentFilter();
    private WifiStateTracker mWifiTracker;

    public ServiceTracker(Context context, String uuid) {
        mContext = context;

        mAssignedUuid = uuid;

        mDescriptor.setServiceName(mAssignedUuid);
        mDescriptor.setServiceType(SVC_INSTANCE_TYPE);
        mDescriptor.setPort(SVC_INSTANCE_PORT);

        NsdManager mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        mWifiTracker = new WifiStateTracker(this, mNsdManager);

        mWifiIntents.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    public void enable() {
        if(!mEnabled) {
            mContext.registerReceiver(mWifiTracker, mWifiIntents);
            mEnabled = true;
        }
    }

    public void disable() {
        if(mEnabled) {
            mContext.unregisterReceiver(mWifiTracker);
            mWifiTracker.disable();

            for (UmobileService svc : mServices.values()) svc.currently = UmobileService.Status.UNAVAILABLE;
            setChanged(); notifyObservers();

            mEnabled = false;
        }
    }

    public Map<String,UmobileService> getServices() {
        return mServices;
    }

    void addUnresolvedService(String svcName) {
        if(!svcName.equals(mAssignedUuid)) {
            Log.d(TAG, "Adding Unresolved <" + svcName + ">");
            mServices.put(svcName, new UmobileService(Status.UNAVAILABLE, svcName, UNKNOWN_HOST, UNKNOWN_PORT));
        }
    }

    void updateService(String svcName, Status newStatus, String svcHost, int svcPort) {
        Log.d(TAG, "Updating <" + svcName + ">" + " status=" + newStatus + ", host=" + svcHost + ", port=" + svcPort + " known=" + mServices.containsKey(svcName));
        if(mServices.containsKey(svcName)) {
            UmobileService svc = mServices.get(svcName);

            svc.currently = newStatus;
            svc.host = svcHost;
            svc.port = svcPort;

            setChanged(); notifyObservers();
        }
    }

}
