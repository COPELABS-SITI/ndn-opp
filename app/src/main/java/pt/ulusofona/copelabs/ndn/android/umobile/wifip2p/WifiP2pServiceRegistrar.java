/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class is used to register the NDN-Opp service at the WiFi P2P level for Service Discovery.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;

/** The WifiP2pServiceRegistrar wraps up the WifiP2p service registration of Android along with additional
 * logic. Specifically, once enabled, it automatically registers/unregisters the NDN-Opp service as the
 * state of Wi-Fi Direct toggles between ON and OFF.
 */
public class WifiP2pServiceRegistrar {
    private static final String TAG = WifiP2pServiceRegistrar.class.getSimpleName();

    private Context mContext;
    private WifiP2pManager mWifiP2pMgr;
    private WifiP2pManager.Channel mWifiP2pChn;

    // Used to detect changes in the state of Wi-Fi Direct (ON/OFF)
    private ServiceRegistrarEventDetector mEventDetector = new ServiceRegistrarEventDetector();

    private WifiP2pDnsSdServiceInfo mDescriptor;

    private boolean mEnabled = false;

    /** Enable the Service Registrar to automatically advertise the NDN-Opp service over Wi-Fi Direct
     * based on whether Wi-Fi Direct is ON or OFF.
     * @param context Android-provided Context
     * @param wifiP2pMgr Android-provided WifiP2pManager
     * @param wifiP2pChn Android-provided WifiP2pManager.Channel
     * @param uuid UUID of the current device
     */
    public synchronized void enable(Context context, WifiP2pManager wifiP2pMgr, WifiP2pManager.Channel wifiP2pChn, String uuid) {
        if(!mEnabled) {
            Log.w(TAG, "Enabling.");
            mEnabled = true;

            mContext = context;
            mWifiP2pMgr = wifiP2pMgr;
            mWifiP2pChn = wifiP2pChn;
            mDescriptor = WifiP2pDnsSdServiceInfo.newInstance(uuid, WifiP2pService.SVC_INSTANCE_TYPE, null);

            mContext.registerReceiver(mEventDetector, new IntentFilter(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION));
        } else
            Log.w(TAG, "Attempt to register a second time.");
    }

    /** Disable the Service Registrar.
     */
    public synchronized void disable() {
        if(mEnabled) {
            Log.w(TAG, "Disabling.");
            unregister();

            mContext.unregisterReceiver(mEventDetector);

            mContext = null;
            mWifiP2pMgr = null;
            mWifiP2pChn = null;
            mDescriptor = null;
            mEnabled = false;
        } else
            Log.w(TAG, "Attempt to unregister a second time.");
    }

    // Register the Wi-Fi Direct NDN-Opp service
    private synchronized void register() {
        if(mEnabled)
            mWifiP2pMgr.addLocalService(mWifiP2pChn, mDescriptor, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Log.v(TAG, "Service successfully added");
                }

                @Override
                public void onFailure(int i) {
                    Log.v(TAG, "Service failed to add ... " + i);
                }
            });
    }

    // Unregister the Wi-Fi Direct NDN-Opp service
    private synchronized void unregister() {
        if(mEnabled)
            mWifiP2pMgr.removeLocalService(mWifiP2pChn, mDescriptor, null);
    }

    private class ServiceRegistrarEventDetector extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();


            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int wifiP2pState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                if(wifiP2pState == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.v(TAG, "WiFi P2P State : ON");
                    register();
                }
                else if (wifiP2pState == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                    Log.v(TAG, "WiFi P2P State : OFF");
                    unregister();
                }
            }
        }
    }
}
