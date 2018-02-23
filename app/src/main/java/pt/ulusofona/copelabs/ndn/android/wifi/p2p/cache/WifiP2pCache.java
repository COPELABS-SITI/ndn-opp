/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class acts as a cache using shared preferences.
 * The stored data is related with Wi-Fi P2P service discovery which is slow.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.wifi.p2p.cache;


import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static android.content.Context.MODE_PRIVATE;

public abstract class WifiP2pCache {

    /** This variable is used to debug WifiP2pCache class */
    private static final String TAG = WifiP2pCache.class.getSimpleName();

    /** This variable is used as a key to store detected devices */
    private static final String WIFI_P2P_CACHE_DEVICES = "wifi_p2p_cache_devices";

    /** This variable is used as a key to store detected devices and his last seen timestamps */
    private static final String WIFI_P2P_CACHE = "wifi_p2p_cache";

    /** This variable is returned when a device is not available in cache */
    private static final int INVALID_CACHE = -1;

    /**
     * This method inserts a device in cache
     * @param context application context
     * @param uuid device uuid
     * @param mac mac uuid
     * @param lastSeen last seen timestamp
     */
    public static void addDevice(Context context, String uuid, String mac, long lastSeen) {
        String id = buildId(uuid, mac);
        addToIndex(context, id);
        SharedPreferences sharedPreferences = context.getSharedPreferences(WIFI_P2P_CACHE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(id, lastSeen);
        editor.apply();
    }

    /**
     * This method returns all cached data
     * @param context application context
     * @return all cached data
     */
    public static HashMap<String, String> getData(Context context) {
        HashMap<String, String> data = new HashMap<>();
        Set<String> devices = getDevicesIndex(context);
        for(String device : devices) {
            String [] reversedId = reverseId(device);
            data.put(reversedId[0], reversedId[1]);
        }
        return data;
    }

    /**
     * This method wipes this cache
     * @param context application context
     */
    public static void wipeCache(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(WIFI_P2P_CACHE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * This method removes a device from cache
     * @param context application context
     * @param id device id to remove
     */
    static void removeDevice(Context context, String id) {
        removeFromIndex(context, id);
        SharedPreferences sharedPreferences = context.getSharedPreferences(WIFI_P2P_CACHE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(id);
        editor.apply();
    }

    /**
     * This method returns the last seen timestamp
     * @param context application context
     * @param id device id in order to get last seen
     * @return last seen timestamp
     */
    static long getLastSeen(Context context, String id) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(WIFI_P2P_CACHE, MODE_PRIVATE);
        return sharedPreferences.getLong(id, INVALID_CACHE);
    }

    /**
     * This method returns the list of detected devices
     * @param context application context
     * @return list of detected devices
     */
    static Set<String> getDevicesIndex(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(WIFI_P2P_CACHE, MODE_PRIVATE);
        return sharedPreferences.getStringSet(WIFI_P2P_CACHE_DEVICES, new HashSet<String>());
    }

    /**
     * This method builds an id based on uuid and mac. Concatenates both variables like "mac$uuid".
     * @param uuid device uuid
     * @param mac device mac
     * @return device id
     */
    private static String buildId(String uuid, String mac) {
        return mac + "$" + uuid;
    }

    /**
     * This method returns the uuid and the mac based on its id.
     * @param id device id
     * @return [0] mac, [1] uuid
     */
    private static String[] reverseId(String id) {
        return id.split("\\$");
    }

    /**
     * This method inserts a detected device in cache
     * @param context application context
     * @param id device id to be inserted
     */
    private static void addToIndex(Context context, String id) {
        Set<String> devices = getDevicesIndex(context);
        devices.add(id);
        storeDevicesIndex(context, devices);
    }

    /**
     * This method removes a device from cache
     * @param context application context
     * @param id device id to be removed
     */
    private static void removeFromIndex(Context context, String id) {
        Set<String> devices = getDevicesIndex(context);
        devices.remove(id);
        storeDevicesIndex(context, devices);
    }

    /**
     * This method stores in cache all detected devices
     * @param context application context
     * @param devices Set containing all devices ids
     */
    private static void storeDevicesIndex(Context context, Set<String> devices) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(WIFI_P2P_CACHE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(WIFI_P2P_CACHE_DEVICES, devices);
        editor.apply();
    }

}
