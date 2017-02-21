/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * The Router class acts as the pivot between the ContextualManager and the ForwardingDaemon.
 * It fullfills two functions within the App
 * (1) Routing: recomputing the new routes to be installed into the ForwardingDaemon's RIB
 * (2) Face management: translating the changes in neighborhood (i.e. other UMobile nodes availability)
 * into bringing the corresponding Faces AVAILABLE and UNAVAILABLE and establishing the connection that those Faces
 * ought to use for communication.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.ulusofona.copelabs.ndn.android.UmobileService;
import pt.ulusofona.copelabs.ndn.android.UmobileService.Status;

class Routing {
    private static final String TAG = Routing.class.getSimpleName();

    private ForwardingDaemon mDaemon;
    private Map<String, UmobileService> mUmobilePeers;

	Routing(ForwardingDaemon daemon) {
        mDaemon = daemon;
        mUmobilePeers = new HashMap<>();
	}

    private void bringUp(String name) {
        /* @TODO: Bringing UP logic */
        Log.d(TAG, "Bringing UP : " + name);
    }

    private void bringDown(String name) {
        /* @TODO: Bringing DOWN logic */
        Log.d(TAG, "Bringing DOWN : " + name);
    }

    public void update(Set<UmobileService> changes) {
        for(UmobileService current : changes) {
            if(current.status == Status.AVAILABLE) {
                /* @TODO: Make sure the Faces of the native library are synchronized */
                if(!mUmobilePeers.containsKey(current.name))
                    mDaemon.createFace("opp://" + current.name, 0, false);
                bringUp(current.name);
            } else if(current.status == Status.UNAVAILABLE)
                bringDown(current.name);

            mUmobilePeers.put(current.name, current);
        }
    }
}