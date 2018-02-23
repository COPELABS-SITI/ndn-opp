/**
 * @version 1.1
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class only provides utilities functions
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 * @author Miguel Tavares (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pGroup;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Random;

/** Utility class for methods used throughout the application. */
public abstract class Utilities {

    /** This variable is used to debug Utilities class */
    private static final String TAG = Utilities.class.getSimpleName();

    /** This variable is the key of UUID store */
    private static final String PROPERTY_UUID_KEY = "UMOBILE_UUID";

    /**
     * This method generates a new uuid in case of it does not exists. The generated
     * UUID is also stored in shared preferences. In case of already exists an UUID,
     * it returns it.
     * @param context application context
     * @return UUID
     */
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

    /**
     * This method turns on the Wi-Fi if it is turned off.
     * @param context
     */
    public static void turnOnWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(!wifiManager.isWifiEnabled())
            wifiManager.setWifiEnabled(true);
    }

    /**
     * This method generates a random small UUID with 4 chars
     * @return UUID
     */
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

    /**
     * This method returns a timestamp
     * @return timestamp
     */
    public static long getTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * This method generates a SHA hash based on the payload parameter
     * @param payload data to be hashed
     * @return SHA generated
     */
    public static String digest(byte[] payload) {
        String sha1 = "FAIL";
        try {
            byte[] sha1sum = MessageDigest.getInstance("SHA-1").digest(payload);
            Formatter fmt = new Formatter();
            for(byte b : sha1sum)
                fmt.format("%02x", b);
            sha1 = fmt.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha1;
    }

}
