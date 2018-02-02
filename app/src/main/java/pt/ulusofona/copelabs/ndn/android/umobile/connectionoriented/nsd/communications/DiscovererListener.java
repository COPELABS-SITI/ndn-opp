package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications;


import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public interface DiscovererListener {

    interface Discoverer extends DiscovererListener {
        void onPeerDetected(NsdInfo nsdInfo);
        void onReceivePeerList(ArrayList<NsdInfo> nsdInfo);
        void onStartDiscoveringSuccess();
        void onStartDiscoveringFailed();
    }

    interface PeerDiscoverer extends DiscovererListener {
        void onPeerDetected(NsdInfo nsdInfo);
    }

    interface PeerListDiscoverer extends DiscovererListener {
        void onReceivePeerList(ArrayList<NsdInfo> nsdInfo);
    }

}
