/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class only provides a utility function to retrieve or generate a UUID.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.util.Base64;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.Random;
import java.util.UUID;

/** Utility class for methods used throughout the application. */
public abstract class Utilities {

    private static final String TAG = Utilities.class.getSimpleName();
    private static final String PROPERTY_UUID_KEY = "UMOBILE_UUID";

    public static String generateUuid(Context context) {
        String uuid;
        SharedPreferences storage = context.getSharedPreferences("Configuration", Context.MODE_PRIVATE);
        if(!storage.contains(PROPERTY_UUID_KEY)) {
            uuid = generateSmallUuid();
            SharedPreferences.Editor editor = storage.edit();
            editor.putString(PROPERTY_UUID_KEY, uuid);
            editor.apply();
        } else {
            uuid = storage.getString(PROPERTY_UUID_KEY, null);
        }
        return uuid;
    }

    public static void turnOnWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
    }

    private static String generateSmallUuid() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < 4; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    public static String extractIp(WifiP2pGroup group) {
        String ipAddress = null;
        String interfaceName = group.getInterface();
        Log.v(TAG, "Group Interface : " + interfaceName);
        if (interfaceName != null) {
            try {
                Enumeration<NetworkInterface> allIfaces = NetworkInterface.getNetworkInterfaces();
                Log.v(TAG, allIfaces.toString());
                while (allIfaces.hasMoreElements()) {
                    NetworkInterface iface = allIfaces.nextElement();
                    Log.v(TAG, iface.toString());
                    if (interfaceName.equals(iface.getName())) {
                        for (InterfaceAddress ifAddr : iface.getInterfaceAddresses()) {
                            InetAddress address = ifAddr.getAddress();
                            if (address instanceof Inet4Address && !address.isAnyLocalAddress())
                                ipAddress = address.getHostAddress();
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        return ipAddress;
    }

}
