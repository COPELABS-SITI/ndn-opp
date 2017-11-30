package pt.ulusofona.copelabs.ndn.android.wifi.p2p;

import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;

public abstract class RequestManager {

    private static WifiP2pDnsSdServiceInfo sDescriptor;

    public static void setDescriptor(WifiP2pDnsSdServiceInfo descriptor) {
        sDescriptor = descriptor;
    }

    public static WifiP2pDnsSdServiceInfo getDescriptor() {
        return sDescriptor;
    }

}
