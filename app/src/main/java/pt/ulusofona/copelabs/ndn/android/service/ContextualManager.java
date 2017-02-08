package pt.ulusofona.copelabs.ndn.android.service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.wifi.WifiManager;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.Peer;
import pt.ulusofona.copelabs.ndn.android.Peer.Status;

public class ContextualManager {
    private static final String TAG = ContextualManager.class.getSimpleName();

    private Routing mRouting;
    private WifiP2pManager mWfdMgr;
    private WifiP2pManager.Channel mWfdChannel;

    private WifiP2pBroadcastReceiver mReceiver;
    private IntentFilter mIntents;

    public ContextualManager(Context ctxt, Routing rt) {
        mRouting = rt;

        mWfdMgr = (WifiP2pManager) ctxt.getSystemService(Context.WIFI_P2P_SERVICE);
        mWfdChannel = mWfdMgr.initialize(ctxt, ctxt.getMainLooper(), null);

        mReceiver = new WifiP2pBroadcastReceiver(mRouting, mWfdMgr, mWfdChannel);

        mIntents = new IntentFilter();
        mIntents.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mIntents.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntents.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntents.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
    }

    public void register(Context ctxt) {
        ctxt.registerReceiver(mReceiver, mIntents);
    }
    public void unregister(Context ctxt) {
        ctxt.unregisterReceiver(mReceiver);
    }

    private static class WifiP2pBroadcastReceiver extends android.content.BroadcastReceiver {
        private Routing mRouting;
        private List<Peer> mLastPeerList;
        private WifiP2pManager mWfdMgr;
        private WifiP2pManager.Channel mWfdChannel;

        WifiP2pBroadcastReceiver(Routing rt, WifiP2pManager wfdMgr, WifiP2pManager.Channel wfdChan) {
            mRouting = rt;
            mLastPeerList = new ArrayList<>();
            mWfdMgr = wfdMgr;
            mWfdChannel = wfdChan;
        }

        @Override
        public void onReceive(Context ctxt, Intent in) {
            String action = in.getAction();
            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int extra = in.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, WifiP2pManager.WIFI_P2P_STATE_DISABLED);
                switch (extra) {
                    case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                        Log.d(TAG, "Wifi P2P : enabled");
                        mWfdMgr.discoverPeers(mWfdChannel, new WifiP2pManager.ActionListener() {
                            @Override public void onSuccess() {}
                            @Override public void onFailure(int i) {}
                        });
                        break;
                    case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                        Log.d(TAG, "Wifi P2P : disabled");
                        mRouting.remove(mLastPeerList);
                        break;
                }
            } else if(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
                mWfdMgr.requestPeers(mWfdChannel, new WifiP2pManager.PeerListListener() {
                    @Override
                    public void onPeersAvailable(WifiP2pDeviceList availablePeers) {
                        List<Peer> newPeerList = new ArrayList<>();
                        for(WifiP2pDevice current : availablePeers.getDeviceList())
                            newPeerList.add(new Peer(Status.AVAILABLE, current.deviceName, current.deviceAddress));
                        Log.d(TAG, "New peer list : " + newPeerList);

                        //TODO: if device is already known and its name change we do not update it.
                        List<Peer> added = new ArrayList<>(newPeerList);
                        added.removeAll(mLastPeerList);

                        List<Peer> removed = new ArrayList<>(mLastPeerList);
                        removed.removeAll(newPeerList);

                        mLastPeerList = newPeerList;

                        mRouting.add(added);
                        mRouting.remove(removed);
                    }
                });
            } else if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                int extra = in.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);
                switch(extra) {
                    case WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED:
                        Log.d(TAG, "Wifi P2P discovery started.");
                        break;
                    case WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED:
                        Log.d(TAG, "Wifi P2P discovery stopped.");
                        break;
                }
            }
        }
    }
}
