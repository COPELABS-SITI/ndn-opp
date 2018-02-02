package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services;


import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public interface ServiceFailureDetectorListener {

    void onPeerLost(NsdInfo nsdInfo);
}
