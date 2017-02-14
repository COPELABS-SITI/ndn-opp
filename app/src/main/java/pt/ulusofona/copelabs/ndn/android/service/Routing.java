/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * The Router class acts as the pivot between the ContextualManager and the ForwardingDaemon.
 * It fullfills two functions within the App
 * (1) Routing: recomputing the new routes to be installed into the ForwardingDaemon's RIB
 * (2) Face management: translating the changes in neighborhood (i.e. other UMobile nodes availability)
 * into bringing the corresponding Faces UP and DOWN and establishing the connection that those Faces
 * ought to use for communication.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.service;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.Peer;
import pt.ulusofona.copelabs.ndn.android.Peer.Status;

public class Routing {
    private ForwardingDaemon mDaemon;
    private List<Peer> mUmobilePeers;

	public Routing(ForwardingDaemon fd) {
        mDaemon = fd;
        mUmobilePeers = new ArrayList<>();
	}

    // Callback for the ContextualManager.
    public void notifyAddition(List<Peer> peers) {
        for(Peer current : peers) notifyAddition(current);
    }

    public void notifyRemoval(List<Peer> peers) {
        for(Peer current : peers) notifyRemoval(current);
    }

    void notifyUMobilePeersChange(List<Peer> uPeers) {
        mUmobilePeers.clear(); mUmobilePeers.addAll(uPeers);
    }

    private void notifyAddition(Peer current) {
        int idx = mUmobilePeers.indexOf(current);
        if(idx == -1) {
            mUmobilePeers.add(current);
            mDaemon.createFace("opp://[" + current.getAddr() + "]", 0, false);
        } else
            mUmobilePeers.get(idx).setStatus(Status.AVAILABLE);
        /* @TODO: Logic of Bringing Up */
    }

    private void notifyRemoval(Peer current) {
        int idx = mUmobilePeers.indexOf(current);
        if(idx != -1)
            mUmobilePeers.get(idx).setStatus(Status.UNAVAILABLE);
    }
}