package pt.ulusofona.copelabs.ndn.android.umobile.nsd;


import pt.ulusofona.copelabs.ndn.android.models.NsdService;

public interface NsdServiceDiscovererListener {

    void refresh(NsdService nsdService);
}
