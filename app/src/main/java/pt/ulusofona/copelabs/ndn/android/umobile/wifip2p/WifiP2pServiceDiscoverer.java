/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class implements the WiFi P2P Service discovery.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

/** The WifiP2pServiceDiscoverer (SD) wraps up the Wi-Fi P2P service discovery process of Android
 * and includes additional logic around it. It searches for service of type _wifip2ptracker._tcp
 * that are advertised through WifiP2p and maintains the list of all services ever detected, whether
 * the corresponding devices are still within range or not. */
class WifiP2pServiceDiscoverer extends Observable {
    private static final String TAG = WifiP2pServiceDiscoverer.class.getSimpleName();

    private Context mContext;
    private WifiP2pManager mWifiP2pMgr;
    private WifiP2pManager.Channel mWifiP2pChn;

    private final DeviceDiscovererEventDetector mEventDetector = new DeviceDiscovererEventDetector();

    private String mAssignedUuid;
    private WifiP2pDnsSdServiceRequest mRequest;

    private boolean mEnabled = false;

    // Associates a MAC address to a WifiP2pService.
    private Map<String, WifiP2pService> mServices = new HashMap<>();

    public Map<String, WifiP2pService> getServices() {
        return mServices;
    }

    /** Enable this Service Discoverer.
     * @param wifip2pMgr the WifiP2pManager provided by the Android API which is used in the service discovery process.
     * @param wifip2pChn the WifiP2pChannel used to interact with the WifiP2pManager
     * @param uuid the UUID of the current device
     */
    public synchronized void enable(Context context, WifiP2pManager wifip2pMgr, WifiP2pManager.Channel wifip2pChn, String uuid) {
        if(!mEnabled) {
            Log.v(TAG, "Enabling");
            mContext = context;
            mWifiP2pMgr = wifip2pMgr;
            mWifiP2pChn = wifip2pChn;

            mAssignedUuid = uuid;
            mRequest = WifiP2pDnsSdServiceRequest.newInstance(WifiP2pService.SVC_INSTANCE_TYPE);

            mWifiP2pMgr.setDnsSdResponseListeners(mWifiP2pChn, svcResponseListener, null);

            mContext.registerReceiver(mEventDetector, new IntentFilter(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION));
            mEnabled = true;
        } else
            Log.w(TAG, "Attempt to enable TWICE");
    }

    /** Disable this Service Discoverer. */
    public synchronized void disable() {
        if(mEnabled) {
            mWifiP2pMgr.setDnsSdResponseListeners(mWifiP2pChn, null, null);
            mContext.unregisterReceiver(mEventDetector);
            mEnabled = false;
        } else
            Log.w(TAG, "Attempt to disable TWICE");
    }

    private void start() {
        if(mEnabled)
            mWifiP2pMgr.addServiceRequest(mWifiP2pChn, mRequest, afterRequestAdded);
    }

    private void stop() {
        if(mEnabled)
            mWifiP2pMgr.removeServiceRequest(mWifiP2pChn, mRequest, afterRequestRemoved);
    }
    /** Processing when a new service is reported by Android. */
    private DnsSdServiceResponseListener svcResponseListener = new DnsSdServiceResponseListener() {
        @Override
        public void onDnsSdServiceAvailable(String instanceUuid, String registrationType, android.net.wifi.p2p.WifiP2pDevice node) {
            Log.d(TAG, "Service Found : " + instanceUuid + " : " + registrationType + "@" + node.deviceAddress);

            // Exclude the UUID of the current device.
            if (!instanceUuid.equals(mAssignedUuid)) {
                // Update the service information stored in the known Services.
                WifiP2pService svc = new WifiP2pService(instanceUuid, Status.convert(node.status), node.deviceAddress);
                mServices.put(node.deviceAddress, svc);
                setChanged(); notifyObservers(svc);
            }
        }
    };

    private ActionListener afterRequestAdded = new ActionListener() {
        @Override public void onSuccess() {Log.v(TAG, "Request added"); mWifiP2pMgr.discoverServices(mWifiP2pChn, afterDiscoverServices);}
        @Override public void onFailure(int e) {Log.e(TAG, "Request failed (" + e + ")");}
    };

    private ActionListener afterRequestRemoved = new ActionListener() {
        @Override public void onSuccess() {Log.v(TAG, "Request removed");}
        @Override public void onFailure(int e) {Log.v(TAG, "Request failed to remove (" + e + ")");}
    };

    private ActionListener afterDiscoverServices = new ActionListener() {
        @Override public void onSuccess() {
            Log.v(TAG, "Discovery started");
        }
        @Override public void onFailure(int i) {
            Log.v(TAG, "Discovery failed : " + i);
        }
    };

    private class DeviceDiscovererEventDetector extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int wifiP2pState = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                if(wifiP2pState == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.v(TAG, "WiFi P2P State : ON");
                    start();
                }
                else if (wifiP2pState == WifiP2pManager.WIFI_P2P_STATE_DISABLED) {
                    Log.v(TAG, "WiFi P2P State : OFF");
                    stop();
                }
            }
        }
    }
}
