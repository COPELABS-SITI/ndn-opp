package pt.ulusofona.copelabs.ndn.android.service;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.Peer;
import pt.ulusofona.copelabs.ndn.android.Peer.Status;

public class Routing {
    private List<Peer> mPeers;

	public Routing() {
        mPeers = new ArrayList<>();
	}

    // Callback for the ContextualManager.
    public void add(List<Peer> peers) {
        for(Peer current : peers) {
            int idx = mPeers.indexOf(current);
            if(idx == -1)
                mPeers.add(current);
            else
                mPeers.get(idx).setStatus(Status.AVAILABLE);
        }
    }

    public void remove(List<Peer> peers) {
        for(Peer current : peers) {
            int idx = mPeers.indexOf(current);
            if(idx != -1)
                mPeers.get(idx).setStatus(Status.UNAVAILABLE);
        }
    }

    public List<Peer> getPeers() { return new ArrayList<>(mPeers); }
}