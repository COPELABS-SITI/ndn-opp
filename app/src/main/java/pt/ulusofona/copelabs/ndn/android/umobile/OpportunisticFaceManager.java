/* @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of a Routing engine that serves as the interface between the NOD
 * and the Contextual Manager.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.android.models.Face;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.OpportunisticPeer;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.OpportunisticPeerTracker;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.Status;

// @TODO: if phone goes to sleep, all the open connections will close.

/**
 * The Opportunistic Face Manager acts as the pivot between the ContextualManager and the ForwardingDaemon.
 * It fullfills two functions within the App
 * (1) Routing: recomputing the new routes to be installed into the ForwardingDaemon's RIB
 * (2) Face management: translating the changes in neighborhood (i.e. other UMobile nodes availability)
 * into bringing the corresponding Faces AVAILABLE and UNAVAILABLE and establishing the connection that those Faces
 * ought to use for communication.
 */
public class OpportunisticFaceManager implements Observer {
    private static final String TAG = OpportunisticFaceManager.class.getSimpleName();

    private OpportunisticDaemon.Binder mDaemonBinder;

    // Associates a UMOBILE peer to a UUID
    private Map<String, OpportunisticPeer> mUmobilePeers = new HashMap<>();

    // Associates a FaceId to a UUID
    private Map<String, Long> mOppFaceIds = new HashMap<>();

    /** Enable the OppFaceManager. When enabled, it reacts to changes in the connection status to a Wi-Fi Direct Group.
     * As the Group Formation results in the device being assigned an IP address, a listening socket will be opened and
     * used for the reception of Interest/Data packets from other devices connected to the same Group. Furthermore,
     * @param binder Binder to the ForwardingDaemon
     */
	void enable(OpportunisticDaemon.Binder binder) {
        mDaemonBinder = binder;
    }

    /** Disable the Routing engine. Changes in the connection status of Wi-Fi Direct Groups will be ignored. */
    void disable() {

    }

    /** Callback method invoked by the ForwardingDaemon when the creation of a Face has been successful.
     * @param face a representation of the Face that was created */
    void afterFaceAdded(Face face) {
        /* If the created face is an opportunistic one, it must be configured at the RIB/FIB level */
        if(face.getRemoteUri().startsWith("opp://")) {
            long faceId = face.getFaceId();
            String peerUuid = face.getRemoteUri().substring(6);
            mOppFaceIds.put(peerUuid, faceId);

            Log.d(TAG, "Registering Opportunistic Face " + faceId + " in RIB for prefix /ndn/multicast and /emergency.");
            mDaemonBinder.addRoute("/ndn/multicast", faceId, 0L, 0L, 1L);
            mDaemonBinder.addRoute("/emergency", faceId, 0L, 0L, 1L);

            mDaemonBinder.bringUpFace(faceId);
        }
    }

    /** Used to handle when a UMobile peer is detected to bring up its corresponding Face.
     * @param uuid the UUID of the device that was detected.
     */
    void bringUpFace(String uuid) {
        Log.d(TAG, "Bringing UP face for " + uuid + " " + mOppFaceIds.containsKey(uuid));
        if(mOppFaceIds.containsKey(uuid))
            mDaemonBinder.bringUpFace(mOppFaceIds.get(uuid));
    }

    /** Used to handle the departure of an NDN-Opp peer from the current Wi-Fi Direct Group
     * @param uuid the UUID of the NDN-Opp peer which left
     */
    void bringDownFace(String uuid) {
        Log.d(TAG, "Bringing DOWN face for " + uuid);
        if (mOppFaceIds.containsKey(uuid))
            mDaemonBinder.bringDownFace(mOppFaceIds.get(uuid));
    }

    /** Update the state of the Routing engine based on what the NSD Service Discoverer reports
     * @param observable observable notifying of changes
     * @param obj optional parameter passed by the observable
     */
    @Override
    public void update(Observable observable, Object obj) {
        if (observable instanceof OpportunisticPeerTracker) {
            OpportunisticPeer peer = (OpportunisticPeer) obj;
            /* If the peer is unknown (i.e. its UUID is not in the list of UMobile peers,
             * we request the creation of a Face for it. Then, if the Face has an ID referenced
             * in the existing Opportunistic faces, bring it up. */
            if(!mUmobilePeers.containsKey(peer.getUuid())) {
                Log.d(TAG, "Requesting Face creation");
                mDaemonBinder.createFace("opp://" + peer.getUuid(), 0, false);
            } else {
                if(peer.getStatus().equals(Status.AVAILABLE))
                    mDaemonBinder.bringUpFace(mOppFaceIds.get(peer.getUuid()));
                else
                    mDaemonBinder.bringDownFace(mOppFaceIds.get(peer.getUuid()));
            }
            mUmobilePeers.put(peer.getUuid(), peer);
        }
    }
}