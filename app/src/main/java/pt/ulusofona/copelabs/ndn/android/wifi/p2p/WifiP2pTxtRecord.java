package pt.ulusofona.copelabs.ndn.android.wifi.p2p;

import java.util.HashMap;
import java.util.Map;

public abstract class WifiP2pTxtRecord {

    private static Map<String, String> sData = new HashMap<>();

    public static void setEntry(String key, String value) {
        sData.put(key, value);
    }

    public static Map<String, String> getEntries() {
        return sData;
    }

}
