/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class encapsulates Status of WiFi P2P Devices
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.net.wifi.p2p.WifiP2pDevice;

public enum Status {
    AVAILABLE("Av"),
    CONNECTED("Co"),
    FAILED("Fa"),
    INVITED("In"),
    UNAVAILABLE("Un"),
    LOST("--");
    private String symbol;

    Status(String s) {
        symbol = s;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Status convert(int st) {
        Status converted;
        if(st == WifiP2pDevice.CONNECTED)
            converted = CONNECTED;
        else if(st == WifiP2pDevice.INVITED)
            converted = INVITED;
        else if(st == WifiP2pDevice.FAILED)
            converted = FAILED;
        else if(st == WifiP2pDevice.AVAILABLE)
            converted = AVAILABLE;
        else if(st == WifiP2pDevice.UNAVAILABLE)
            converted = UNAVAILABLE;
        else
            converted = LOST;

        return converted;
    }
}
