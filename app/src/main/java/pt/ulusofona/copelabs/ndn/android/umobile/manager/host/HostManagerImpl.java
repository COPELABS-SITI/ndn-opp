package pt.ulusofona.copelabs.ndn.android.umobile.manager.host;


import android.content.Intent;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.utilities.Utilities;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;

public class HostManagerImpl implements HostManager, WifiP2pListener.WifiP2pConnectionStatus {

    private static final String TAG = HostManagerImpl.class.getSimpleName();

    /** This list contains all registered sListeners */
    private static List<HostManagerListener> sListeners = new ArrayList<>();
    private static String GroupOwnerIpAddress;
    private static boolean sEnable;

    @Override
    public synchronized void enable() {
        if(!sEnable) {
            WifiP2pListenerManager.registerListener(this);
            sEnable = true;
        }
    }

    @Override
    public void disable() {
        WifiP2pListenerManager.unregisterListener(this);
        sListeners.clear();
        sEnable = false;
    }

    /**
     * This method registers a listener
     * @param listener listener to be registered
     */
    public static void registerListener(HostManagerListener listener) {
        Log.i(TAG, "Registering a listener");
        if(sEnable) {
            sListeners.add(listener);
        }
    }

    /**
     * This method unregisters a listener
     * @param listener listener to be unregistered
     */
    public static void unregisterListener(HostManagerListener listener) {
        Log.i(TAG, "Unregistering a listener");
        if(sEnable) {
            sListeners.remove(listener);
        }
    }

    private static void notifyIpAddress(String ipAddress) {
        Log.i(TAG, "notifyServiceAvailable");
        for(HostManagerListener listener : sListeners) {
            if(listener instanceof HostManagerListener.Sender)
                ((HostManagerListener.Sender)listener).onReceiveIpAddress(ipAddress);
        }
    }

    @Override
    public void onConnected(Intent intent) {
        WifiP2pGroup wifip2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);
        notifyIpAddress(Utilities.extractIp(wifip2pGroup));
    }

    @Override
    public void onDisconnected(Intent intent) {
        //notifyIpAddress(UNKNOWN_HOST);
    }
}
