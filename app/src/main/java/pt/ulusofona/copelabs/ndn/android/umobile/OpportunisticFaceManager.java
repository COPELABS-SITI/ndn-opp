/* @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of a Routing engine that serves as the interface between the NOD
 * and the Contextual Manager.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile;

import android.os.Handler;
import android.util.Log;
import android.util.LongSparseArray;

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

    // Associates a UUID to an OpportunisticPeer
    private Map<String, OpportunisticPeer> mUmobilePeers = new HashMap<>();

    // Associates a FaceId to a UUID
    private Map<String, Long> mUuidToFaceId = new HashMap<>();
    // Associates a UUID to a FaceId
    private LongSparseArray<String> mFaceIdToUuid = new LongSparseArray<>();

    /** Enable the OppFaceManager. When enabled, it reacts to changes in the connection status to a Wi-Fi Direct Group.
     * As the Group Formation results in the device being assigned an IP address, a listening socket will be opened and
     * used for the reception of Interest/Data packets from other devices connected to the same Group. Furthermore,
     * @param binder Binder to the ForwardingDaemon
     */
	void enable(OpportunisticDaemon.Binder binder) {
        mDaemonBinder = binder;
    }

    /** Disable the Routing engine. Changes in the connection status of Wi-Fi Direct Groups will be ignored. */
    void disable() {}

    /** Callback method invoked by the ForwardingDaemon when the creation of a Face has been successful.
     * @param face a representation of the Face that was created */
    void afterFaceAdded(Face face) {
        /* If the created face is an opportunistic one, it must be configured at the RIB/FIB level */
        if(face.getRemoteUri().startsWith("opp://")) {
            final long faceId = face.getFaceId();
            String peerUuid = face.getRemoteUri().substring(6);
            mUuidToFaceId.put(peerUuid, faceId);
            mFaceIdToUuid.put(faceId, peerUuid);

            Log.d(TAG, "Registering Opportunistic Face " + faceId + " in RIB for prefix /ndn/multicast and /ndn/opp/emergency.");
            mDaemonBinder.addRoute("/ndn/multicast", faceId, 0L, 0L, 1L);
            mDaemonBinder.addRoute("/ndn/opp/emergency", faceId, 0L, 0L, 1L);
            mDaemonBinder.addRoute("/ndn", faceId, 0L, 0L, 1L);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDaemonBinder.passInterests(faceId, "/ndn/multicast/opp");
                }
            }, 500);

            mDaemonBinder.bringUpFace(faceId);
        }
    }

    /** Used to handle when a UMobile peer is detected to bring up its corresponding Face.
     * @param uuid the UUID of the device that was detected.
     */
    void bringUpFace(String uuid) {
        Log.d(TAG, "Bringing UP face for " + uuid + " " + mUuidToFaceId.containsKey(uuid));
        if(mUuidToFaceId.containsKey(uuid))
            mDaemonBinder.bringUpFace(mUuidToFaceId.get(uuid));
    }

    /** Used to handle the departure of an NDN-Opp peer from the current Wi-Fi Direct Group
     * @param uuid the UUID of the NDN-Opp peer which left
     */
    void bringDownFace(String uuid) {
        Log.d(TAG, "Bringing DOWN face for " + uuid);
        if (mUuidToFaceId.containsKey(uuid))
            mDaemonBinder.bringDownFace(mUuidToFaceId.get(uuid));
    }

    /** Update the state of the Routing engine based on what the NSD Service Discoverer reports
     * @param observable observable notifying of changes
     * @param obj optional parameter passed by the observable
     */
    @Override
    public void update(Observable observable, Object obj) {
        if (observable instanceof OpportunisticPeerTracker) {
            Map<String, OpportunisticPeer> peers = (Map<String, OpportunisticPeer>) obj;
            /* If the peer is unknown (i.e. its UUID is not in the list of UMobile peers,
             * we request the creation of a Face for it. Then, if the Face has an ID referenced
             * in the existing Opportunistic faces, bring it up. */
            if (peers != null) {
                for (String uuid : peers.keySet()) {
                    OpportunisticPeer peer = peers.get(uuid);
                    if (!mUmobilePeers.containsKey(uuid)) {
                        Log.d(TAG, "Requesting Face creation");
                        mDaemonBinder.createFace("opp://" + uuid, 0, false);
                    } else {
                        Long faceId = mUuidToFaceId.get(uuid);
                        if(faceId != null) {
                            if (peer.getStatus().equals(Status.AVAILABLE))
                                mDaemonBinder.bringUpFace(mUuidToFaceId.get(uuid));
                            else
                                mDaemonBinder.bringDownFace(mUuidToFaceId.get(uuid));
                        }
                    }
                    mUmobilePeers.put(uuid, peer);
                }
            }
        }
    }

    public String getUuid(long faceId) {
        return mFaceIdToUuid.get(faceId);
    }

    public Long getFaceId(String uuid) {
        return mUuidToFaceId.get(uuid);
    }
}