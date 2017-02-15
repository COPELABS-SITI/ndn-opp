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

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pt.ulusofona.copelabs.ndn.android.Peer;

public class Routing {
    private static final String TAG = Routing.class.getSimpleName();

    private ForwardingDaemon mDaemon;
    private Map<String, Peer> mUmobilePeers;

	public Routing(ForwardingDaemon fd) {
        mDaemon = fd;
        mUmobilePeers = new HashMap<>();
	}

    // Callback for the ContextualManager.
    void notifyUMobilePeersChange(Set<Peer> changes) {
        for(Peer current : changes) {
            String currentAddress = current.getAddr();
            switch(current.getStatus()) {
                case AVAILABLE:
                    if(!mUmobilePeers.containsKey(currentAddress))
                        mDaemon.createFace("opp://[" + current.getAddr() + "]", 0, false);
                    /* @TODO: Bringing UP logic */
                    break;
                case UNAVAILABLE:
                    /* @TODO: Bringing DOWN logic */
                    break;
                default:
                    Log.w(TAG, "Unhandled status : " + current.getStatus().getSymbol());
                    break;
            }
            mUmobilePeers.put(current.getAddr(), current);
        }
    }
}