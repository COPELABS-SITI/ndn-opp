/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class implements the Peer tracking functionality which keeps track of WiFi P2P peers
 * in the neighborhood.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.regex.Pattern;

import pt.ulusofona.copelabs.ndn.android.Identity;
import pt.ulusofona.copelabs.ndn.android.OperationResult;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;

/** The Peer Tracker is used to maintain up-to-date the lists of all NDN-Opp Peers ever detected.
 * The Peer Tracker integrates three components; the DeviceDiscoverer, the ServiceDiscoverer and
 * the ServiceRegistrar. */
public class OpportunisticPeerTracker extends Observable implements WifiP2pListener.PeersAvailable, WifiP2pListener.ServiceAvailable {

    private static final String TAG = OpportunisticPeerTracker.class.getSimpleName();

    private String mAssignedUuid;

    // Associates UUID to OpportunisticPeer
    private Map<String, OpportunisticPeer> mPeers = new HashMap<>();
    // Associates MAC Address to UUID
    private Map<String, String> mDevices = new HashMap<>();

    public Map<String, OpportunisticPeer> getPeers() {
        return mPeers;
    }

    /** Enables the PeerTracker. When enabled, it automatically performs ServiceDiscovery based on the current status of the Wi-Fi
     * P2P component of Android. At that point, it notifies any Observer whenever there is a change to the state of the list of NDN-Opp peers.
     * The changes that are advertised to the Observers are;
     *
     *  - New peer detected
     *  - Change to the status of a known peer; moved in (Available) or gone out (Unavailable) of communication range, connected to a group (Connected),
     *                                          invited to a group (Invited) or failed (Failed) as reported by Android.
     *         (see https://developer.android.com/reference/android/net/wifi/p2p/WifiP2pDevice.html)
     *
     * All these changes are announced as a list of OpportunisticPeer objects to the Observers.
     *
     * @param context
     */
    public void enable(Context context) {
        mAssignedUuid = Identity.getUuid();
        WifiP2pListenerManager.registerListener(this);
    }

    /** Disables the PeerTracker. All OpportunisticPeer objects are marked as Unavailable and all the Observers are notified. */
    public void disable() {
        WifiP2pListenerManager.unregisterListener(this);
        for(String peerUuid : mPeers.keySet())
            mPeers.get(peerUuid).setStatus(Status.UNAVAILABLE);
        setChanged(); notifyObservers(mPeers);
    }


    @Override
    public void onServiceAvailable(String uuid, String type, WifiP2pDevice dev) {
        Log.d(TAG, "Service Found : " + uuid + " : " + type + "@" + dev.deviceAddress);

        // Exclude the UUID of the current device
        if (!mAssignedUuid.equals(uuid)) {
            String[] components = type.split(Pattern.quote("."));
            if (components.length >= 1 && Identity.SVC_INSTANCE_TYPE.equals(components[0]) && !mPeers.containsKey(uuid)) {
                OpportunisticPeer peer = new OpportunisticPeer(uuid, dev);
                mPeers.put(uuid, peer);
                mDevices.put(dev.deviceAddress, uuid);
                Map<String, OpportunisticPeer> peerList = new HashMap<>();
                peerList.put(uuid, peer);
                setChanged(); notifyObservers(peerList);
            }
        }
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        //WifiP2pDeviceList devList = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
        // TODO: Improve this update sequence
        Map<String, OpportunisticPeer> peerList = new HashMap<>();
        Map<String, Status> scanResult = new HashMap<>();

        for(WifiP2pDevice dev : peers.getDeviceList())
            scanResult.put(dev.deviceAddress, Status.convert(dev.status));

        // Construct the list of peers whose device is not in the scan result.
        for(String mac : mDevices.keySet()) {
            if(!scanResult.containsKey(mac)) {
                String uuid = mDevices.get(mac);
                OpportunisticPeer peer = mPeers.get(uuid);
                peer.setStatus(Status.UNAVAILABLE);
                peerList.put(uuid, peer);
            }
        }

        // Construct the list of peers whose status in the scan has changed.
        for(WifiP2pDevice dev : peers.getDeviceList()) {
            String uuid = mDevices.get(dev.deviceAddress);
            if (uuid != null) {
                OpportunisticPeer peer = new OpportunisticPeer(uuid, dev);
                mPeers.put(uuid, peer);
                peerList.put(uuid, peer);
            }
        }

        setChanged(); notifyObservers(peerList);
    }
}