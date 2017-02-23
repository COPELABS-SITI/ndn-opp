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

import pt.ulusofona.copelabs.ndn.android.Face;
import pt.ulusofona.copelabs.ndn.android.UmobileService;
import pt.ulusofona.copelabs.ndn.android.UmobileService.Status;

class Routing {
    private static final String TAG = Routing.class.getSimpleName();

    private ForwardingDaemon mDaemon;
    private Map<String, UmobileService> mUmobilePeers = new HashMap<>();
    private Map<String, Long> mOppFaceIds = new HashMap<>();

	Routing(ForwardingDaemon daemon) {
        mDaemon = daemon;
	}

    private void bringDown(String name) {
        /* @TODO: Bringing DOWN logic */
        Log.d(TAG, "Bringing DOWN : " + name);
        if(mOppFaceIds.containsKey(name))
            mDaemon.bringDownFace(mOppFaceIds.get(name));
    }

    void afterFaceAdd(Face f) {
        // If this is an opportunistic face, it must be introduced into the RIB for /ndn/multicast.
        long faceId = f.getId();
        if(f.getRemoteURI().startsWith("opp://")) {
            mOppFaceIds.put(f.getRemoteURI().substring(6), faceId);
            Log.d(TAG, "Registering Opportunistic Face " + faceId + " in RIB for prefix /ndn/multicast.");
            mDaemon.addRoute("/ndn/multicast", faceId, 0L, 0L, 1L);
            mDaemon.bringUpFace(faceId);
        }
    }

    public void update(Set<UmobileService> changes) {
        Log.d(TAG, "Received an UPDATE " + changes);
        for(UmobileService current : changes) {
            if(current.getStatus() == Status.AVAILABLE) {
                /* @TODO: Make sure the Faces of the native library are synchronized */
                if(!mUmobilePeers.containsKey(current.name))
                    mDaemon.createFace("opp://" + current.name, 0, false);
                else
                    mDaemon.bringUpFace(mOppFaceIds.get(current.name));
            } else if(current.getStatus() == Status.UNAVAILABLE)
                bringDown(current.name);

            mUmobilePeers.put(current.name, current);
        }
    }
}