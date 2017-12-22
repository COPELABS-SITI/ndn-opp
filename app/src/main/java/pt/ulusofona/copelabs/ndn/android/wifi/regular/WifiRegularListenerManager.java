package pt.ulusofona.copelabs.ndn.android.wifi.regular;


import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WifiRegularListenerManager {

    private static final String TAG = WifiRegularListenerManager.class.getSimpleName();

    /** This list contains all registered listeners */
    private static List<WifiRegularListener> listeners = new ArrayList<>();

    /**
     * This method registers a listener
     * @param wifiRegularListener listener to be registered
     */
    public static void registerListener(WifiRegularListener wifiRegularListener) {
        Log.i(TAG, "Registering a listener");
        listeners.add(wifiRegularListener);
    }

    /**
     * This method unregisters a listener
     * @param wifiRegularListener listener to be unregistered
     */
    public static void unregisterListener(WifiRegularListener wifiRegularListener) {
        Log.i(TAG, "Unregistering a listener");
        listeners.remove(wifiRegularListener);
    }

    static void notifyConnected() {
        Log.i(TAG, "notifyConnected");
        for(WifiRegularListener listener : listeners) {
            listener.onConnected();
        }
    }

    static void notifyDisconnected() {
        Log.i(TAG, "notifyDisconnected");
        for(WifiRegularListener listener : listeners) {
            listener.onDisconnected();
        }
    }
}
