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

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.ulusofona.copelabs.ndn.android.Face;
import pt.ulusofona.copelabs.ndn.android.UmobileService;
import pt.ulusofona.copelabs.ndn.android.UmobileService.Status;

public class Routing {
    private static final String TAG = Routing.class.getSimpleName();

    private ServerSocket mServerSocket;
    private ForwardingDaemon mDaemon;
    private Map<String, UmobileService> mUmobilePeers = new HashMap<>();
    private Map<String, Long> mOppFaceIds = new HashMap<>();

	Routing(ForwardingDaemon daemon) { mDaemon = daemon; }

    void afterFaceAdd(Face f) {
        // If this is an opportunistic face, it must be introduced into the RIB for /ndn/multicast.
        long faceId = f.getId();
        if(f.getRemoteURI().startsWith("opp://")) {
            String peerUuid = f.getRemoteURI().substring(6);
            mOppFaceIds.put(peerUuid, faceId);
            Log.d(TAG, "Registering Opportunistic Face " + faceId + " in RIB for prefix /ndn/multicast.");
            mDaemon.addRoute("/ndn/multicast", faceId, 0L, 0L, 1L);
            //if(mUmobilePeers.get(peerUuid).currently == Status.AVAILABLE)
            //    mDaemon.bringUpFace(faceId);
        }
    }

    public void update(UmobileService current) {
        if(current.getStatus() == Status.AVAILABLE) {
            /* If the peer is unknown (i.e. its UUID is not in the list of UMobile peers,
             * we request the creation of a Face for it. Then, if the Face has an ID referenced
             * in the existing Opportunistic faces, bring it up. */
            if(!mUmobilePeers.containsKey(current.uuid)) {
                Log.d(TAG, "Requesting Face creation");
                mDaemon.createFace("opp://" + current.uuid, 0, false);
            }

            Log.d(TAG, "Bringing UP face for " + current.uuid);
            if(mOppFaceIds.containsKey(current.uuid))
                mDaemon.bringUpFace(mOppFaceIds.get(current.uuid));
        } else if(current.getStatus() == Status.UNAVAILABLE) {
            Log.d(TAG, "Bringing DOWN face for " + current.uuid);
            if (mOppFaceIds.containsKey(current.uuid))
                mDaemon.bringDownFace(mOppFaceIds.get(current.uuid));
        }

        mUmobilePeers.put(current.uuid, current);
    }

    public void update(Set<UmobileService> serviceChanges) {
        Log.d(TAG, "Received a COMPLETE update : " + serviceChanges);
        for(UmobileService svc : serviceChanges) update(svc);
    }

    public void enable(ServerSocket mSocket) {
        mServerSocket = mSocket;
    }

    public void disable() {

    }
}