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
import android.os.Looper;
import android.util.Log;
import android.view.View;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/** The Peer Tracker is used to maintain up-to-date the lists of all NDN-Opp Peers ever detected.
 * The Peer Tracker integrates three components; the DeviceDiscoverer, the ServiceDiscoverer and
 * the ServiceRegistrar.
 */
public class OpportunisticPeerTracker extends Observable implements Observer {
    private static final String TAG = OpportunisticPeerTracker.class.getSimpleName();

    private static OpportunisticPeerTracker INSTANCE = null;

    private boolean mEnabled = false;

    private WifiP2pServiceRegistrar mWifiP2pServiceRegistrar = new WifiP2pServiceRegistrar();
    private WifiP2pDeviceDiscoverer mWifiP2pDeviceDiscoverer = new WifiP2pDeviceDiscoverer();
    private WifiP2pServiceDiscoverer mWifiP2pServiceDiscoverer = new WifiP2pServiceDiscoverer();

    // Associates UUID to WifiP2pPeer instance
    private Map<String, OpportunisticPeer> mPeers = new HashMap<>();
    // Associates MAC Address to WifiP2pDevice instance
    private Map<String, WifiP2pDevice> mDevices = new HashMap<>();
    // Associates MAC Address to WifiP2pService instance
    private Map<String, WifiP2pService> mServices = new HashMap<>();

    private OpportunisticPeerTracker() {}

    /** Retrieve the instance of the Peer Tracker.
     * @return singleton instance of Peer Tracker.
     */
    public static OpportunisticPeerTracker getInstance() {
        if(INSTANCE == null)
            INSTANCE = new OpportunisticPeerTracker();
        return INSTANCE;
    }

    /** Enable this PeerTracker. Changes in the Device and Service Discoverers will be merged together
     * and integrated to maintain the list of WifiP2pPeers with up-to-date status, UUID and MAC address
     * @param context Android provided Context
     * @param wifiP2pMgr Android provided WifiP2pManager
     * @param wifiP2pChn Android provided WifiP2pChannel
     * @param uuid UUID of the current device
     */
    public synchronized void enable(Context context, WifiP2pManager wifiP2pMgr, WifiP2pManager.Channel wifiP2pChn, String uuid) {
        if(!mEnabled) {
            Log.v(TAG, "Enabling Peer Tracker.");

            mWifiP2pServiceRegistrar.enable(context, wifiP2pMgr, wifiP2pChn, uuid);

            mWifiP2pDeviceDiscoverer.addObserver(this);
            mWifiP2pDeviceDiscoverer.enable(context, wifiP2pMgr, wifiP2pChn);

            mWifiP2pServiceDiscoverer.addObserver(this);
            mWifiP2pServiceDiscoverer.enable(context, wifiP2pMgr, wifiP2pChn, uuid);

            mEnabled = true;
        } else
            Log.w(TAG, "Attempt to register a second time.");
    }

    /** Disable the Peer Tracker. */
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

    public Map<String, OpportunisticPeer> getPeers() {
        return mPeers;
    }

    /** Used to process notifications from the two observed instances of device and service discoverers.
     * @param observable which observable notified this tracker of a change
     * @param obj optional parameter passed through the notification
     */
    @Override
    public void update(Observable observable, Object obj) {
        if (observable instanceof WifiP2pDeviceDiscoverer) {
            Log.d(TAG, "Update from Device Discoverer [" + mWifiP2pDeviceDiscoverer.getDevices().size() + "]");
            for (WifiP2pDevice dev : mWifiP2pDeviceDiscoverer.getDevices().values())
                updateDevice(dev);


        } else if (observable instanceof WifiP2pServiceDiscoverer) {
            Log.d(TAG, "Update from Service Discoverer [" + mWifiP2pServiceDiscoverer.getServices().size() + "]");
            if(obj != null && obj instanceof WifiP2pService)
                updateService((WifiP2pService) obj);
            else
                for (WifiP2pService svc : mWifiP2pServiceDiscoverer.getServices().values())
                    updateService(svc);
        }
    }

    /** Perform the update of the service instance concerned by a notification
     * @param svc the new details of the service
     */
    private void updateService(WifiP2pService svc) {
        // This implementation works no matter which is detected first; device or service.
        String svcMacAddress = svc.getMacAddress();

        // Overwrite the previous instance of the service
        mServices.put(svcMacAddress, svc);

        // If there is already a device known for this service, update the corresponding peer
        if(mDevices.containsKey(svcMacAddress))
            updatePeer(mDevices.get(svcMacAddress), svc);
    }

    /** Perform the update of the device instance concerned by a notification
     * @param dev the new details of the device
     */
    private void updateDevice(WifiP2pDevice dev) {
        // This implementation works no matter which is detected first; device or service.
        String devMacAddress = dev.getMacAddress();

        // Overwrite the previous instance of the device
        mDevices.put(devMacAddress, dev);

        // If there is already a service known for this device, update the corresponding peer
        if(mServices.containsKey(devMacAddress))
            updatePeer(dev, mServices.get(devMacAddress));
    }

    /** Perform the update of information concerning a certain peer
     * @param dev the new details of the device
     * @param svc the new details of the service
     */
    private void updatePeer(WifiP2pDevice dev, WifiP2pService svc) {
        OpportunisticPeer peer;
        String uuid = svc.getUuid();
        if(mPeers.containsKey(uuid)) {
            peer = mPeers.get(uuid);
            peer.update(dev);
        } else {
            peer = OpportunisticPeer.create(dev, svc);
            mPeers.put(uuid, peer);
        }
        setChanged(); notifyObservers(peer);
    }
}