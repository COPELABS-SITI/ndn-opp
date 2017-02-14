/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * The DeviceTracker implements a simple approach to maintaining a list of other UMobile peers available.
 * On one side, it relies on Wifi-P2P Peer Discovery to populate the list of devices around, on the other
 * it uses ServiceDiscovery (currently Dns-SD) to filter out the devices that are not running UMobile.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.service;

import android.content.Context;
import android.content.Intent;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;

import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.ulusofona.copelabs.ndn.android.Peer;
import pt.ulusofona.copelabs.ndn.android.Peer.Status;

class UmobileDeviceTracker extends android.content.BroadcastReceiver {
    private static final String TAG = UmobileDeviceTracker.class.getSimpleName();

    private Map<String, Peer> mWifiP2pPeers;

    private ContextualManager mCtxtMgr;
    private ServiceDiscovery mDiscoverer;

    private WifiP2pManager mWfdMgr;
    private WifiP2pManager.Channel mWfdChannel;

    UmobileDeviceTracker(ContextualManager ctxtMgr, WifiP2pManager wfdMgr, WifiP2pManager.Channel wfdChan) {
        mWifiP2pPeers = new HashMap<>();

        mWfdMgr = wfdMgr;
        mWfdChannel = wfdChan;

        mCtxtMgr = ctxtMgr;
        mDiscoverer = new ServiceDiscovery();
    }

    public List<Peer> getUmobilePeers() {
        List<Peer> upeers = new ArrayList<>();
        for(String addr : mDiscoverer.mUmobilePeersAddresses)
            if(mWifiP2pPeers.containsKey(addr))
                upeers.add(mWifiP2pPeers.get(addr));
        return upeers;
    }

    @Override
    public void onReceive(Context ctxt, Intent in) {
        String action = in.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int extra = in.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, WifiP2pManager.WIFI_P2P_STATE_DISABLED);
            switch (extra) {
                case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                    Log.d(TAG, "Wifi P2P : enabled");
                    mWfdMgr.discoverPeers(mWfdChannel, new ActionListener("Discover peers"));
                    mDiscoverer.enable();
                    break;
                case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                    Log.d(TAG, "Wifi P2P : disabled");
                    mDiscoverer.disable();
                    break;
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            mWfdMgr.requestPeers(mWfdChannel, new WifiP2pManager.PeerListListener() {
                @Override
                public void onPeersAvailable(WifiP2pDeviceList availablePeers) {
                    Map<String, Peer> newScanResult = new HashMap<>();
                    for (WifiP2pDevice current : availablePeers.getDeviceList())
                        newScanResult.put(current.deviceAddress, new Peer(Status.convert(current.status), current.deviceName, current.deviceAddress));
                    Log.d(TAG, "New Scan Result : " + newScanResult);

                    // Add all newly detected devices.
                    mWifiP2pPeers.putAll(newScanResult);

                    for(String deviceAddress : mWifiP2pPeers.keySet())
                        if(!newScanResult.containsKey(deviceAddress))
                            mWifiP2pPeers.get(deviceAddress).setStatus(Status.UNAVAILABLE);

                    mCtxtMgr.notifyUmobilePeerChange();
                }
            });
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            int extra = in.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);
            switch (extra) {
                case WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED:
                    Log.d(TAG, "Wifi P2P discovery started.");
                    break;
                case WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED:
                    Log.d(TAG, "Wifi P2P discovery stopped.");
                    mWfdMgr.discoverServices(mWfdChannel, new ActionListener("Discover peers"));
                    break;
            }
        }
    }

    class ServiceDiscovery {
        private static final String INSTANCE_NAME = "_umobile";
        private static final String INSTANCE_TYPE = "_ndn._tcp";

        Set<String> mUmobilePeersAddresses;

        private WifiP2pDnsSdServiceInfo mDescriptor;
        private WifiP2pDnsSdServiceRequest mRequest;

        ServiceDiscovery() {
            mUmobilePeersAddresses = new HashSet<>();

            mWfdMgr.setDnsSdResponseListeners(mWfdChannel,
                new WifiP2pManager.DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice wifiP2pDevice) {
                        Log.d(TAG, "ServiceAvailable : " + instanceName + " : " + registrationType + "@" + wifiP2pDevice.deviceAddress);
                        if(instanceName.equals(INSTANCE_NAME)) mUmobilePeersAddresses.add(wifiP2pDevice.deviceAddress);
                    }
                },
                new WifiP2pManager.DnsSdTxtRecordListener() {
                    @Override
                    public void onDnsSdTxtRecordAvailable(String fullDomain, Map<String, String> map, WifiP2pDevice wifiP2pDevice) {
                        Log.d(TAG, "TxtRecord : " + fullDomain + "@" + wifiP2pDevice.deviceAddress);
                    }
                }
            );

            mDescriptor = WifiP2pDnsSdServiceInfo.newInstance(INSTANCE_NAME, INSTANCE_TYPE, null);
            mRequest = WifiP2pDnsSdServiceRequest.newInstance();
        }

        void enable() {
            mWfdMgr.addLocalService(mWfdChannel, mDescriptor, new ActionListener("ADD Local Service"));
            mWfdMgr.addServiceRequest(mWfdChannel, mRequest, new ActionListener("ADD Service request"));
            mWfdMgr.discoverServices(mWfdChannel, new ActionListener("Discover service"));
        }

        void disable() {
            mWfdMgr.removeServiceRequest(mWfdChannel, mRequest, new ActionListener("REMOVE Service request"));
            mWfdMgr.removeLocalService(mWfdChannel, mDescriptor, new ActionListener("REMOVE Local Service"));
        }
    }

    class ActionListener implements WifiP2pManager.ActionListener {
        private String operation;

        ActionListener(String op) {
            operation = op;
        }

        @Override
        public void onSuccess() {
            Log.d(TAG, operation + " succeeded");
        }

        @Override
        public void onFailure(int error) {
            Log.d(TAG, operation + " failed (" + error + ")");
        }
    }
}
