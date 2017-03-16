/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * A dummy implementation of the ContextualManager that will be provided by Senseption, Lda.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.UUID;

import pt.ulusofona.copelabs.ndn.android.NsdService;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.PeerTracking;
import pt.ulusofona.copelabs.ndn.android.umobile.nsd.NsdServiceTracker;

// @TODO: weigh the trade-off between swiftness of ServiceTracking vs. cost of the ServiceDiscovery on which it relies.
class ContextualManager extends Observable implements Observer {
    private static final String TAG = "PeerTracker";

    private static final String PROPERTY_UUID_KEY = "UMOBILE_UUID";

    private String mAssignedUuid;

    private final NsdServiceTracker mNsdTracker = new NsdServiceTracker();

    private Map<String, NsdService> mUmobileServices = new HashMap<>();

    String getUmobileUuid() { return mAssignedUuid; }

    Collection<NsdService> getUmobileServices() {
        return new HashSet<>(mUmobileServices.values());
    }

    private static String obtainUuid(Context context) {
        String uuid;
        SharedPreferences storage = context.getSharedPreferences(PeerTracking.class.getSimpleName(), Context.MODE_PRIVATE);
        if(!storage.contains(PROPERTY_UUID_KEY)) {
            uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = storage.edit();
            editor.putString(PROPERTY_UUID_KEY, uuid);
            editor.apply();
        } else
            uuid = storage.getString(PROPERTY_UUID_KEY, null);

        return uuid;
    }

    public void enable(Context ctxt) {
        mAssignedUuid = obtainUuid(ctxt);

        mNsdTracker.addObserver(this);
        mNsdTracker.enable(ctxt, mAssignedUuid);
    }

    public void disable() {
        mNsdTracker.disable();
        mNsdTracker.deleteObserver(this);
    }

    @Override
    public void update(Observable observable, Object obj) {
        // Construct delta between previous UmobileService list and the new one.
        if(obj != null) {
            Log.d(TAG, "Received PUNCTUAL updateService.");
            NsdService svc = (NsdService) obj;
            mUmobileServices.put(svc.getUuid(), svc);
            setChanged(); notifyObservers(svc);
            //mRouting.updateService(svc);
        } else {
            Log.d(TAG, "Received COMPLETE updateService.");

            Set<NsdService> changes = new HashSet<>();

            Map<String, NsdService> newServiceList = mNsdTracker.getServices();
            for (String svcUuid : newServiceList.keySet()) {
                NsdService newEntry = newServiceList.get(svcUuid);
                NsdService oldEntry = mUmobileServices.get(svcUuid);

                Log.d(TAG, oldEntry + " -> " + newEntry + " equal? " + newEntry.equals(oldEntry));

                if (!newEntry.equals(oldEntry)) {
                    mUmobileServices.put(svcUuid, newEntry);
                    changes.add(newEntry);
                }
            }

            // Push changes to Observers.
            setChanged(); notifyObservers(changes);
            //mRouting.updateService(changes);
        }
    }
}
