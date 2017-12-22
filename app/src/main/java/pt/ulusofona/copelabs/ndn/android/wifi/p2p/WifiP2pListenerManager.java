/*
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017/9/8.
 * Class is part of the NSense application.
 */

package pt.ulusofona.copelabs.ndn.android.wifi.p2p;


import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible to manage the wifi p2p listeners.
 * Register, unregister and notify them.
 * @author Miguel Tavares (COPELABS/ULHT)
 * @version 1.0, 2017
 */
public abstract class WifiP2pListenerManager {

    /** This variable is used to debug WifiP2pListenerManager class */
    private static final String TAG = "WifiP2pListenerManager";

    /** This list contains all registered listeners */
    private static List<WifiP2pListener> listeners = new ArrayList<>();

    /**
     * This method registers a listener
     * @param wifiP2pListener listener to be registered
     */
    public static void registerListener(WifiP2pListener wifiP2pListener) {
        Log.i(TAG, "Registering a listener");
        listeners.add(wifiP2pListener);
    }

    /**
     * This method unregisters a listener
     * @param wifiP2pListener listener to be unregistered
     */
    public static void unregisterListener(WifiP2pListener wifiP2pListener) {
        Log.i(TAG, "Unregistering a listener");
        listeners.remove(wifiP2pListener);
    }

    /**
     * This method notifies all ServiceAvailable listeners
     */
    static void notifyServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
        Log.i(TAG, "notifyServiceAvailable");
        for(WifiP2pListener listener : listeners) {
            if(listener instanceof WifiP2pListener.ServiceAvailable)
                ((WifiP2pListener.ServiceAvailable)listener).onServiceAvailable(instanceName, registrationType, srcDevice);
        }
    }

    /**
     * This method notifies all TxtRecordAvailable listeners
     */
    static void notifyTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
        Log.i(TAG, "notifyTxtRecordAvailable");
        for(WifiP2pListener listener : listeners) {
            if(listener instanceof WifiP2pListener.TxtRecordAvailable)
                ((WifiP2pListener.TxtRecordAvailable)listener).onTxtRecordAvailable(fullDomainName, txtRecordMap, srcDevice);
        }
    }

    /**
     * This method notifies all PeersAvailable listeners
     */
    static void notifyPeersAvailable(WifiP2pDeviceList peers) {
        Log.i(TAG, "notifyPeersAvailable");
        for(WifiP2pListener listener : listeners) {
            if(listener instanceof  WifiP2pListener.PeersAvailable) {
                ((WifiP2pListener.PeersAvailable)listener).onPeersAvailable(peers);
            }
        }
    }

    static void notifyConnected(Intent intent) {
        Log.i(TAG, "Wi-Fi connection established");
        for(WifiP2pListener listener : listeners) {
            if(listener instanceof WifiP2pListener.WifiP2pConnectionStatus) {
                ((WifiP2pListener.WifiP2pConnectionStatus)listener).onConnected(intent);
            }
        }
    }

    static void notifyDisconnected(Intent intent) {
        Log.i(TAG, "Wi-Fi connection dropped down");
        for(WifiP2pListener listener : listeners) {
            if(listener instanceof WifiP2pListener.WifiP2pConnectionStatus) {
                ((WifiP2pListener.WifiP2pConnectionStatus)listener).onDisconnected(intent);
            }
        }
    }

}
