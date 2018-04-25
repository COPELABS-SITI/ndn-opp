/** @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of a Routing engine that serves as the interface between the NOD
 * and the Contextual Manager.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.common;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.util.LongSparseArray;

import com.intel.jndn.management.ManagementException;

import net.named_data.jndn.ControlParameters;
import net.named_data.jndn.ForwardingFlags;
import net.named_data.jndn.Name;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import pt.ulusofona.copelabs.ndn.android.utilities.Nfdc;
import pt.ulusofona.copelabs.ndn.android.models.Face;
import pt.ulusofona.copelabs.ndn.android.models.NsdService;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.OpportunisticPeer;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.channels.OpportunisticChannelOut;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services.ServiceDiscoverer;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.RoutingEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.utilities.Utilities;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;

// @TODO: if phone goes to sleep, all the open connections will close.

/**
 * The Opportunistic Face PacketManagerImpl acts as the pivot between the ContextualManager and the ForwardingDaemon.
 * It fullfills two functions within the App
 * (1) Routing: recomputing the new routes to be installed into the ForwardingDaemon's RIB
 * (2) Face management: translating the changes in neighborhood (i.e. other UMobile nodes availability)
 * into bringing the corresponding Faces AVAILABLE and UNAVAILABLE and establishing the connection that those Faces
 * ought to use for communication.
 */
public class OpportunisticFaceManager implements Observer, ServiceDiscoverer.PeerListDiscoverer,
        WifiP2pListener.WifiP2pConnectionStatus {

    private static final String TAG = OpportunisticFaceManager.class.getSimpleName();

    private OpportunisticDaemon.Binder mDaemonBinder;

    private net.named_data.jndn.Face mControlFace = new net.named_data.jndn.Face();

    private boolean mControlFaceRegistered = false;

    private Context mContext;
    // Associates a UUID to an OpportunisticPeer
    private Map<String, OpportunisticPeer> mUmobilePeers = new HashMap<>();
    // Associates a FaceId to a UUID
    private Map<String, Long> mUuidToFaceId = new HashMap<>();
    // Associates a UUID to a FaceId
    private LongSparseArray<String> mFaceIdToUuid = new LongSparseArray<>();
    // Associates a OpportunisticChannel to a UUID
    private ConcurrentHashMap<String, OpportunisticChannelOut> mOppOutChannels = new ConcurrentHashMap<>();

    /** Enable the OppFaceManager. When enabled, it reacts to changes in the connection status to a Wi-Fi Direct Group.
     * As the Group Formation results in the device being assigned an IP address, a listening socket will be opened and
     * used for the reception of Interest/WifiP2pCache packets from other devices connected to the same Group. Furthermore,
     * @param binder Binder to the ForwardingDaemon
     */
	public void enable(OpportunisticDaemon.Binder binder, Context context) {
        mControlFaceRegistered = false;
        setupControlFace();
        mDaemonBinder = binder;
        mContext = context;
        WifiP2pListenerManager.registerListener(this);
    }

    private void setupControlFace() {
        new Thread() {
            public void run() {
                try {
                    KeyChain keyChain = Utilities.buildTestKeyChain();
                    keyChain.setFace(mControlFace);
                    mControlFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());
                    mControlFace.registerPrefix(new Name("/localhost/controlface"), null, null);
                    mControlFaceRegistered = true;
                    Log.i(TAG, "Control face registered");
                } catch (SecurityException | IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    /** Disable the Routing engine. Changes in the connection status of Wi-Fi Direct Groups will be ignored. */
    public void disable() {
        mControlFaceRegistered = false;
        WifiP2pListenerManager.unregisterListener(this);
    }

    /** Callback method invoked by the ForwardingDaemon when the creation of a Face has been successful.
     * @param face a representation of the Face that was created */
    public void afterFaceAdded(Face face) {
        /* If the created face is an opportunistic one, it must be configured at the RIB/FIB level */
        if(face.getRemoteUri().startsWith("opp://")) {
            final Long faceId = face.getFaceId();
            final String peerUuid = face.getRemoteUri().substring(6);

            mUuidToFaceId.put(peerUuid, faceId);
            mFaceIdToUuid.put(faceId, peerUuid);

            addRoute(new RoutingEntry("/", faceId, 0));

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDaemonBinder.passInterests(faceId, "/ndn/oi");
                }
            }, 500);

            bringUpFace(peerUuid);
        }
    }

    public void addRoute(final RoutingEntry routingEntry) {
        //if(!mControlFaceRegistered) {
            new Thread() {
                public void run() {
                    try {
                        ControlParameters controlParameters = new ControlParameters();
                        controlParameters.setFaceId((int) routingEntry.getFace());
                        controlParameters.setName(new Name(routingEntry.getPrefix()));
                        Nfdc.register(mControlFace, controlParameters);
                    } catch (ManagementException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
       // }
    }

    /** Used to handle when a UMobile peer is detected to bring up its corresponding Face.
     * @param uuid the UUID of the device that was detected.
     */
    public void bringUpFace(String uuid) {
        Log.d(TAG, "Bringing UP face for " + uuid + " " + mUuidToFaceId.containsKey(uuid));
        if(mUuidToFaceId.containsKey(uuid)) {
            mDaemonBinder.bringUpFace(mUuidToFaceId.get(uuid));
        }
    }

    /** Used to handle the departure of an NDN-Opp peer from the current Wi-Fi Direct Group
     * @param uuid the UUID of the NDN-Opp peer which left
     */
    public void bringDownFace(String uuid) {
        Log.d(TAG, "Bringing DOWN face for " + uuid);
        if (mUuidToFaceId.containsKey(uuid)) {
            mDaemonBinder.bringDownFace(mUuidToFaceId.get(uuid));
            if(mOppOutChannels.containsKey(uuid)) {
                Log.i(TAG, "Destroying socket of " + uuid);
                mOppOutChannels.remove(uuid);
            }
        }
    }

    @Override
    public void onConnected(Intent intent) {
        Log.i(TAG, "Wi-Fi or Wi-Fi P2P connection detected");
        ServiceDiscoverer.registerListener(this);
    }

    @Override
    public void onDisconnected(Intent intent) {
        Log.i(TAG, "Wi-Fi or Wi-Fi P2P connection dropped");
        Log.i(TAG, "Deleting opportunistic channels");
        ServiceDiscoverer.unregisterListener(this);
        mOppOutChannels.clear();
    }

    /** Update the state of the Routing engine based on what the NSD Service DiscovererResult reports
     * @param observable observable notifying of changes
     * @param obj optional parameter passed by the observable
     */
    @Override
    public void update(Observable observable, Object obj) {
        if(mControlFaceRegistered) {
            if (observable instanceof OpportunisticPeerTracker) {
                Map<String, OpportunisticPeer> peers = (Map<String, OpportunisticPeer>) obj;
                /* If the peer is unknown (i.e. its UUID is not in the list of UMobile peers,
                 * we request the creation of a Face for it. Then, if the Face has an ID referenced
                 * in the existing Opportunistic faces, bring it up. */
                if (peers != null) {
                    for (final String uuid : peers.keySet()) {
                        OpportunisticPeer peer = peers.get(uuid);
                        if (!mUmobilePeers.containsKey(uuid)) {
                            Log.d(TAG, "Requesting Face creation");
                            mDaemonBinder.createFace("opp://" + uuid, 0, false);
                        } else {
                            Long faceId = mUuidToFaceId.get(uuid);
                            if (faceId != null) {
                                if (peer.isAvailable()) {
                                    if (!mDaemonBinder.isFaceUp(faceId))
                                        bringUpFace(uuid);
                                } else {
                                    if (mDaemonBinder.isFaceUp(faceId))
                                        bringDownFace(uuid);
                                }
                            }
                        }
                        mUmobilePeers.put(uuid, peer);
                    }
                }
            }
        }
    }

    /**
     * Updated list of connected peers
     * @param nsdInfoList updated list
     */
    @Override
    public synchronized void onReceivePeerList(ArrayList<NsdInfo> nsdInfoList) {
        Log.i(TAG, "Received NSD List");
        checkChannelsToCreate(nsdInfoList);
        checkChannelsToDelete(nsdInfoList);
    }

    /**
     * This method checks if there is a new peer available. If there is, creates a channel for it
     * @param nsdInfoList updated peer list
     */
    private synchronized void checkChannelsToCreate(ArrayList<NsdInfo> nsdInfoList) {
        for(NsdInfo nsdInfo : nsdInfoList) {
            NsdService svc = new NsdService(nsdInfo.getUuid(), nsdInfo.getIpAddress());
            if (!mOppOutChannels.containsKey(svc.getUuid()) && svc.isHostValid()) {
                Log.i(TAG, "Creating opportunistic channel to " + svc.getUuid());
                createChannelOut(svc);
            }
        }
    }

    /**
     * This method checks if some peer is missing. If there is, it deletes the channel
     * @param nsdInfoList updated peer list
     */
    private synchronized void checkChannelsToDelete(ArrayList<NsdInfo> nsdInfoList) {
        Map channels = new HashMap<>(mOppOutChannels);
        Iterator it = channels.entrySet().iterator();
        while (it.hasNext()) {
            boolean found = false;
            Map.Entry pair = (Map.Entry)it.next();
            for(NsdInfo nsdInfo : nsdInfoList) {
                if(pair.getKey().equals(nsdInfo.getUuid())) {
                    found = true;
                    break;
                }
            }
            if(!found) {
                deleteChannelOut(pair.getKey().toString());
            }
        }
    }

    /**
     * This method creates a channel and store it in an HashMap
     * @param svc peer info
     */
    private void createChannelOut(NsdService svc) {
        Log.i(TAG, "Creating socket for: " + svc.getUuid() + " with " + svc.getHost() + ":" + svc.getPort());
        mOppOutChannels.put(svc.getUuid(), new OpportunisticChannelOut(mContext, svc.getHost(), svc.getPort()));
    }

    /**
     * This method deletes the channel related with uuid passed as a parameter
     * @param uuid uuid related with the channel to delete
     */
    private void deleteChannelOut(String uuid) {
        Log.i(TAG, "Deleting socket : " + uuid);
        mOppOutChannels.remove(uuid);
    }

    /**
     * This method returns true if there is a connection for this uuid. Returns false if not
     * @param uuid uuid to check if the connection is available
     * @return
     */
    public boolean isSocketAvailable(String uuid) {
        return mOppOutChannels.containsKey(uuid);
    }

    /**
     * This method sends data through a socket
     * @param packet data to be sent
     */
    public void sendPacket(Packet packet) {
        if(packet != null) {
            mOppOutChannels.get(packet.getRecipient()).sendPacket(packet);
        }
    }

    /**
     * Returns the uuid associated with the face id in parameter
     * @param faceId face id in order to get uuid
     * @return
     */
    public String getUuid(long faceId) {
        return mFaceIdToUuid.get(faceId);
    }

    /**
     * Returns the face id associated with a uuid in parameter
     * @param uuid uuid in order to get face id
     * @return
     */
    public Long getFaceId(String uuid) {
        return mUuidToFaceId.get(uuid);
    }

}