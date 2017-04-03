/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class implements the WiFi P2P Service discovery.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pConnectivityTracker;
import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pStateTracker;

class WifiP2pServiceDiscoverer extends Observable implements Observer {
    private static final String TAG = WifiP2pServiceDiscoverer.class.getSimpleName();

    private WifiP2pManager mWifiP2pMgr;
    private WifiP2pManager.Channel mWifiP2pChn;

    private String mAssignedUuid;
    private WifiP2pDnsSdServiceRequest mRequest;

    private WifiP2pStateTracker mStateTracker = WifiP2pStateTracker.getInstance();
    private WifiP2pConnectivityTracker mConnectivityTracker = WifiP2pConnectivityTracker.getInstance();

    private boolean mEnabled = false;
    private boolean mDiscoveryEnabled = false;

    private boolean mWifiP2pEnabled = false;
    private boolean mWifiP2pConnected = false;

    // Associates a MAC address to a WifiP2pService.
    private Map<String, WifiP2pService> mServices = new HashMap<>();

    public Map<String, WifiP2pService> getServices() {
        return mServices;
    }

    public synchronized void enable(WifiP2pManager wifip2pMgr, WifiP2pManager.Channel wifip2pChn, String uuid) {
        if(!mEnabled) {
            Log.v(TAG, "Enabling");
            mWifiP2pMgr = wifip2pMgr;
            mWifiP2pChn = wifip2pChn;

            mAssignedUuid = uuid;
            mRequest = WifiP2pDnsSdServiceRequest.newInstance(WifiP2pService.SVC_INSTANCE_TYPE);

            mStateTracker.addObserver(this);
            mConnectivityTracker.addObserver(this);

            mEnabled = true;
        } else
            Log.w(TAG, "Attempt to register TWICE");
    }

    public synchronized void disable() {
        if(mEnabled) {
            disableDiscovery();

            mStateTracker.deleteObserver(this);
            mConnectivityTracker.deleteObserver(this);

            mEnabled = false;
        } else
            Log.w(TAG, "Attempt to unregister TWICE");
    }

    private synchronized void enableDiscovery() {
        if(!mDiscoveryEnabled) {
            Log.v(TAG, "Enabling Discovery");
            mWifiP2pMgr.setDnsSdResponseListeners(mWifiP2pChn, svcResponseListener, null);
            mWifiP2pMgr.addServiceRequest(mWifiP2pChn, mRequest, afterRequestAdded);
            mDiscoveryEnabled = true;
        } else
            Log.v(TAG, "Attempt to register Discovery TWICE");
    }

    private synchronized void disableDiscovery() {
        if(mDiscoveryEnabled) {
            Log.v(TAG, "Disabling Discovery");
            mWifiP2pMgr.setDnsSdResponseListeners(mWifiP2pChn, null, null);
            mWifiP2pMgr.clearServiceRequests(mWifiP2pChn, afterRequestClear);

            for (WifiP2pService current : mServices.values()) current.setStatus(Status.LOST);

            setChanged(); notifyObservers();

            mDiscoveryEnabled = false;
        } else
            Log.v(TAG, "Attempt to unregister Discovery TWICE");
    }

    private synchronized void startDiscovery() {
        if(mDiscoveryEnabled) {
            Log.v(TAG, "Starting Discovery");
            mWifiP2pMgr.discoverServices(mWifiP2pChn, afterDiscoveryStarted);
        }
    }

    private DnsSdServiceResponseListener svcResponseListener = new DnsSdServiceResponseListener() {
        @Override
        public void onDnsSdServiceAvailable(String instanceUuid, String registrationType, android.net.wifi.p2p.WifiP2pDevice node) {
            Log.d(TAG, "Service Found : " + instanceUuid + " : " + registrationType + "@" + node.deviceAddress);
            if (!instanceUuid.equals(mAssignedUuid)) {
                WifiP2pService svc = new WifiP2pService(instanceUuid, Status.convert(node.status), node.deviceAddress);
                mServices.put(node.deviceAddress, svc);
                setChanged(); notifyObservers(svc);
            }
        }
    };

    private ActionListener afterRequestAdded = new ActionListener() {
        @Override public void onSuccess() {Log.v(TAG, "Request added"); startDiscovery();}
        @Override public void onFailure(int e) {Log.e(TAG, "Request failed (" + e + ")");}
    };

    private ActionListener afterRequestClear = new ActionListener() {
        @Override public void onSuccess() {Log.v(TAG, "Request removed");}
        @Override public void onFailure(int e) {Log.e(TAG, "Request failed (" + e + ")");}
    };

    private ActionListener afterDiscoveryStarted = new ActionListener() {
        @Override public void onSuccess() {Log.v(TAG, "Started");}
        @Override public void onFailure(int e) {Log.e(TAG, "Failure (" + e + ")");}
    };

    @Override
    public void update(Observable observable, Object o) {
        if (observable instanceof WifiP2pStateTracker) {
            boolean enabled = (boolean) o;
            Log.v(TAG, "Update from WifiP2pStateTracker : " + (enabled ? "ENABLED" : "DISABLED"));
            if(enabled) enableDiscovery();
            else disableDiscovery();


        } else if (observable instanceof WifiP2pConnectivityTracker) {
            boolean connected = (boolean) o;
            Log.v(TAG, "Update from WifiP2pStateTracker : " + (connected ? "CONNECTED" : "DISCONNECTED"));
            if(!connected) startDiscovery();
        }
    }
}
