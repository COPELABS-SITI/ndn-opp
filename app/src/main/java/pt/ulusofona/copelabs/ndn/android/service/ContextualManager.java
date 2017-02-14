/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * A dummy implementation of the ContextualManager that will be provided by Senseption, Lda.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.service;

import android.content.Context;
import android.content.IntentFilter;

import android.net.wifi.p2p.WifiP2pManager;

import java.util.List;

import pt.ulusofona.copelabs.ndn.android.Peer;

class ContextualManager {
    private Routing mRouting;
    private WifiP2pManager mWfdMgr;
    private WifiP2pManager.Channel mWfdChannel;

    private UmobileDeviceTracker mTracker;
    private IntentFilter mIntents;

    ContextualManager(Context ctxt, Routing rt) {
        mRouting = rt;

        mWfdMgr = (WifiP2pManager) ctxt.getSystemService(Context.WIFI_P2P_SERVICE);
        mWfdChannel = mWfdMgr.initialize(ctxt, ctxt.getMainLooper(), null);

        mTracker = new UmobileDeviceTracker(this, mWfdMgr, mWfdChannel);

        mIntents = new IntentFilter();
        mIntents.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntents.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntents.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
    }

    List<Peer> getUmobilePeers() {
        return mTracker.getUmobilePeers();
    }

    void notifyUmobilePeerChange() {
        mRouting.notifyUMobilePeersChange(mTracker.getUmobilePeers());
    }

    public void register(Context ctxt) {
        ctxt.registerReceiver(mTracker, mIntents);
    }

    public void unregister(Context ctxt) {
        ctxt.unregisterReceiver(mTracker);
    }
}
