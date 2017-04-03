/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class implements the Peer tracking functionality which keeps track of WiFi P2P peers
 * in the neighborhood.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class WifiP2pPeerTracker extends Observable implements Observer {
    private static final String TAG = WifiP2pPeerTracker.class.getSimpleName();

    private static WifiP2pPeerTracker INSTANCE = null;

    private boolean mEnabled = false;

    private WifiP2pServiceRegistrar mWifiP2pServiceRegistrar = new WifiP2pServiceRegistrar();
    private WifiP2pDeviceDiscoverer mWifiP2pDeviceDiscoverer = new WifiP2pDeviceDiscoverer();
    private WifiP2pServiceDiscoverer mWifiP2pServiceDiscoverer = new WifiP2pServiceDiscoverer();

    /** A Peer encapsulates information from a Service on a Device, namely
     * - Device status
     * - Service UUID
     * - MAC address
     * - Is the device a Group Owner or a Client in a Group
     */
    private Map<String, WifiP2pPeer> mPeers = new HashMap<>();
    private Map<String, WifiP2pDevice> mDevices = new HashMap<>();
    private Map<String, WifiP2pService> mServices = new HashMap<>();

    public static WifiP2pPeerTracker getInstance() {
        if(INSTANCE == null)
            INSTANCE = new WifiP2pPeerTracker();
        return INSTANCE;
    }

    public synchronized void enable(Context context, WifiP2pManager wifiP2pMgr, WifiP2pManager.Channel wifiP2pChn, String uuid) {
        if(!mEnabled) {
            Log.v(TAG, "Enabling Peer Tracker.");

            mWifiP2pServiceRegistrar.enable(wifiP2pMgr, wifiP2pChn, uuid);

            mWifiP2pDeviceDiscoverer.addObserver(this);
            mWifiP2pDeviceDiscoverer.enable(context, wifiP2pMgr, wifiP2pChn);

            mWifiP2pServiceDiscoverer.addObserver(this);
            mWifiP2pServiceDiscoverer.enable(wifiP2pMgr, wifiP2pChn, uuid);

            mEnabled = true;
        } else
            Log.w(TAG, "Attempt to register a second time.");
    }

    public synchronized void disable() {
        if(mEnabled) {
            Log.v(TAG, "Disabling Peer Tracker");

            mWifiP2pServiceDiscoverer.disable();
            mWifiP2pServiceDiscoverer.deleteObserver(this);

            mWifiP2pDeviceDiscoverer.disable();
            mWifiP2pDeviceDiscoverer.deleteObserver(this);

            mWifiP2pServiceRegistrar.disable();

            mEnabled = false;
        } else
            Log.w(TAG, "Attempt to unregister a second time.");
    }

    public Map<String, WifiP2pPeer> getPeers() {return mPeers;}

    @Override
    public void update(Observable observable, Object obj) {
        if (observable instanceof WifiP2pDeviceDiscoverer) {
            Log.d(TAG, "Update from Device Discoverer [" + mWifiP2pDeviceDiscoverer.getDevices().size() + "]");
            for (WifiP2pDevice dev : mWifiP2pDeviceDiscoverer.getDevices().values())
                updateDevice(dev);
            setChanged(); notifyObservers();


        } else if (observable instanceof WifiP2pServiceDiscoverer) {
            Log.d(TAG, "Update from Service Discoverer [" + mWifiP2pServiceDiscoverer.getServices().size() + "]");
            if(obj != null && obj instanceof WifiP2pService)
                updateService((WifiP2pService) obj);
            else
                for (WifiP2pService svc : mWifiP2pServiceDiscoverer.getServices().values())
                    updateService(svc);
            setChanged(); notifyObservers();
        }
    }

    private void updateService(WifiP2pService svc) {
        String svcMacAddress = svc.getMacAddress();

        mServices.put(svcMacAddress, svc);

        if(mDevices.containsKey(svcMacAddress)) {
            WifiP2pDevice dev = mDevices.get(svcMacAddress);
            String uuid = svc.getUuid();
            if(mPeers.containsKey(uuid)) {
                WifiP2pPeer peer = mPeers.get(uuid);
                peer.update(dev);
            } else
                mPeers.put(uuid, WifiP2pPeer.create(dev, svc));
        }
    }

    private void updateDevice(WifiP2pDevice dev) {
        String devMacAddress = dev.getMacAddress();

        mDevices.put(devMacAddress, dev);

        if(mServices.containsKey(devMacAddress)) {
            WifiP2pService svc = mServices.get(devMacAddress);
            String uuid = svc.getUuid();
            if(mPeers.containsKey(uuid)) {
                WifiP2pPeer peer = mPeers.get(uuid);
                peer.update(dev);
            } else
                mPeers.put(uuid, WifiP2pPeer.create(dev, svc));
        }
    }
}