/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * The ServiceTracker encapsulates the logic of discovering UMobile peers on the same network. It is
 * mostly intended for demo and testing purposes.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.tracking;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import pt.ulusofona.copelabs.ndn.android.UmobileService;
import pt.ulusofona.copelabs.ndn.android.UmobileService.Status;
import pt.ulusofona.copelabs.ndn.android.umobile.Routing;

public class ServiceTracker extends Observable {
    // @TODO: figure out if the observed micro-cuts [LOST =(1-2seconds)= FOUND] can be safely concealed.
    // @TODO: Services are sometimes lost for longer period of time ...
    // @TODO: assign port dynamically
    private static final String TAG = ServiceTracker.class.getSimpleName();

    static final String SERVICE_TYPE = "_ndn._tcp";

    static final String UNKNOWN_HOST = "0.0.0.0";
    static final int UNKNOWN_PORT = 0;

    private Context mContext;

    final String assignedUuid;

    private boolean mEnabled = false;

    private Map<String, UmobileService> mServices = new HashMap<>();

    private WifiConnectionTracker mWifiTracker;

    public ServiceTracker(Context ctxt, Routing rt, String uuid) {
        mContext = ctxt;

        assignedUuid = uuid;

        mWifiTracker = new WifiConnectionTracker(this, rt);
    }

    public void enable() {
        mWifiTracker.enable(mContext);
    }

    public void disable() {
        mWifiTracker.disable(mContext);
        for (UmobileService svc : mServices.values()) svc.currently = UmobileService.Status.UNAVAILABLE;
        setChanged(); notifyObservers(); clearChanged();
    }

    public Map<String,UmobileService> getServices() {
        return mServices;
    }

    void updateService(String uuid, Status currently, String host, int port) {
        Log.d(TAG, "Updating <" + uuid + ">" + " status=" + currently + ", host=" + host + ", port=" + port + " known=" + mServices.containsKey(uuid));

        UmobileService svc =
            mServices.containsKey(uuid) ? mServices.get(uuid) : new UmobileService();

        svc.uuid = uuid;
        svc.currently = currently;
        svc.host = host;
        svc.port = port;

        mServices.put(uuid, svc);

        setChanged(); notifyObservers(svc); clearChanged();
    }
}
