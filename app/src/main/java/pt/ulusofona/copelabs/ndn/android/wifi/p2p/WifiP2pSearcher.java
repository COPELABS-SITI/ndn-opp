/*
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017/9/7.
 * Class is part of the NSense application.
 */

package pt.ulusofona.copelabs.ndn.android.wifi.p2p;


import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.util.Log;

import java.util.Map;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.WifiDevice;


/**
 * This class is responsible for discover wifi p2p devices, services
 * and also text records which are being announced.
 * @author Miguel Tavares (COPELABS/ULHT)
 * @version 1.0, 2017
 */
class WifiP2pSearcher implements DnsSdServiceResponseListener, DnsSdTxtRecordListener, PeerListListener {

    /** This variable is used to debug WifiP2pSearcher */
    private static final String TAG = "WifiP2pSearcher";

    /** This object contains the service request */
    private WifiP2pDnsSdServiceRequest mServiceRequest;

    /** A mChannel that connects the application to the WifiRegular mWifiP2pManager framework. */
    private WifiP2pManager.Channel mChannel;

    /** Android WiFi P2P PacketManager */
    private WifiP2pManager mWifiP2pManager;

    WifiP2pSearcher(Context context) {
        mWifiP2pManager = (android.net.wifi.p2p.WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), null);
        mWifiP2pManager.setDnsSdResponseListeners(mChannel, this, this);
    }

    /**
     * This method adds the service request
     */
    void addServiceRequest() {
        mServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mWifiP2pManager.addServiceRequest(mChannel, mServiceRequest, new android.net.wifi.p2p.WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i(TAG,"Added service discovery request");
            }

            @Override
            public void onFailure(int reason) {
                Log.i(TAG,"Failed adding service discovery request, reason: " + reason);
            }
        });
    }

    /**
     * This method starts the discoveries
     */
    void startDiscovery() {
        discoverPeers();
        discoverServices();
    }

    /**
     * This method removes the service request
     */
    void removeServiceRequest() {
        if(mServiceRequest != null)
            mWifiP2pManager.removeServiceRequest(mChannel, mServiceRequest, null);
    }

    /**
     * This method does the peers request
     */
    void requestPeers() {
        mWifiP2pManager.requestPeers(mChannel, this);
    }

    /**
     * This method does the discovery peers
     */
    private void discoverPeers() {
        mWifiP2pManager.discoverPeers(mChannel, new android.net.wifi.p2p.WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i(TAG, "Peers discovery initiated");
            }

            @Override
            public void onFailure(int reason) {
                Log.i(TAG, "Peers discovery failed, reason: " + reason);
            }
        });
    }

    /**
     * This method does the discovery services
     */
    private void discoverServices() {
        mWifiP2pManager.discoverServices(mChannel, new android.net.wifi.p2p.WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i(TAG,"Service discovery initiated");
            }

            @Override
            public void onFailure(int reason) {
                Log.i(TAG,"Service discovery failed reason: " + reason);

                /*
                mServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
                mWifiP2pManager.removeServiceRequest(mChannel, mServiceRequest, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.e(TAG,"OK");
                    }

                    @Override
                    public void onFailure(int i) {
                        Log.e(TAG,"NOK");
                    }
                });

                mWifiP2pManager.removeLocalService(mChannel, );
                */

            }
        });
    }

    @Override
    public void onDnsSdServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
        Log.i(TAG, "onDnsSdServiceAvailable " + instanceName + " " + registrationType);
        WifiP2pListenerManager.notifyServiceAvailable(instanceName, registrationType, srcDevice);
    }

    @Override
    public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
        Log.i(TAG, "onDnsSdTxtRecordAvailable " + fullDomainName);
        WifiP2pListenerManager.notifyTxtRecordAvailable(fullDomainName, txtRecordMap, srcDevice);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        Log.i(TAG, "Peers Available ready");
        for(WifiP2pDevice peer : peers.getDeviceList()) {
            WifiDevice wifiDevice = WifiDevice.convert(peer);
            Log.i(TAG, wifiDevice.toString());
        }
        WifiP2pListenerManager.notifyPeersAvailable(peers);
    }
}
