/*
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017/9/7.
 * Class is part of the NSense application.
 */

package pt.ulusofona.copelabs.ndn.android.wifi.p2p;


import android.content.Context;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;

/**
 * This class is used to manage all features related with wifi p2p GO
 * @author Miguel Tavares (COPELABS/ULHT)
 * @version 1.0, 2017
 */
class WifiP2pGo implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.GroupInfoListener {

    private static final String TAG = "WifiP2pGo";

    /** A mChannel that connects the application to the Wifi mWifiP2pManager framework. */
    private WifiP2pManager.Channel mChannel;

    /** Android WiFi P2P PacketManager */
    private WifiP2pManager mWifiP2pManager;

    /** This object stores the application context */
    private Context mContext;

    WifiP2pGo(Context context) {
        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), null);
        mContext = context;
        setup();
    }

    /**
     * This method clean everything before GO features starts
     */
    private void setup() {
        clearServiceRequests();
        clearLocalServices();
        removeGroup();
    }

    /**
     * Provides information about the Group Owner created (Access Point)
     * This information include the SSID and the MAC address.
     */
    @Override
    public void onGroupInfoAvailable(WifiP2pGroup group) {
        try {
            //startLocalService();
        } catch (NullPointerException e) {
            Log.e(TAG, "onGroupInfoAvailable NullPointerException");
            e.printStackTrace();
        }
    }

    /**
     * Request group information
     * @param info group information
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        try {
            if (info.isGroupOwner) {
                mWifiP2pManager.requestGroupInfo(mChannel, this);
            } else {
                Log.i(TAG, "we are client !! group owner address is: " + info.groupOwnerAddress.getHostAddress());
            }
        } catch (Exception e) {
            Log.i(TAG, "onConnectionInfoAvailable " + e.toString());
        }
    }

    /**
     * This method starts the local service
     */
    private void startLocalService() {
        /*
        WifiP2pTxtRecordPreferences.setRecord(mContext, BT_MAC_KEY, BTManager.getBTMACAddress(mContext));
        final Map<String, String> txtRecord =  WifiP2pTxtRecordPreferences.getRecordsMap(mContext);
        final WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = WifiP2pDnsSdServiceInfo.newInstance(instance, SERVICE_TYPE, txtRecord);
        */

        final WifiP2pDnsSdServiceInfo wifiP2pDnsSdServiceInfo = RequestManager.getDescriptor();

        mWifiP2pManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.i(TAG, "Service announced with success");
                mWifiP2pManager.addLocalService(mChannel, wifiP2pDnsSdServiceInfo, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Log.i(TAG,"Added Local Service");
                    }

                    @Override
                    public void onFailure(int error) {
                        Log.i(TAG,"Failed to add a mWifiP2pDnsSdServiceInfo " + error);
                    }
                });
            }

            @Override
            public void onFailure(int reason) {
                Log.e(TAG, "Service was not announced with success. Error "  + reason);
            }
        });

    }

    void requestConnectionInfo() {
        mWifiP2pManager.requestConnectionInfo(mChannel, this);
    }

    /**
     * Remove the group created. It stops the access point created in this device.
     */
    void removeGroup() {
        mWifiP2pManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Log.i(TAG,"Cleared Local Group ");
            }

            public void onFailure(int reason) {
                Log.i(TAG,"Clearing Local Group failed, error code " + reason);
            }
        });
    }

    void clearServiceRequests() {
        mWifiP2pManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.i(TAG,"Services cleared successfully");
            }

            @Override
            public void onFailure(int reason) {
                Log.i(TAG,"Clearing services failed, reason: " + reason);
            }
        });
    }

    /**
     * Create a Group Owner. It creates a Access point in this device.
     */
    void createGroup() {
        mWifiP2pManager.createGroup(mChannel,new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Log.i(TAG,"Group created successfully");
            }

            public void onFailure(int reason) {
                Log.i(TAG,"Local Group failed, error code " + reason);
                mWifiP2pManager.removeGroup(mChannel, null);
            }
        });
    }

    /**
     * Removes all local services created.
     */
    void clearLocalServices() {
        mWifiP2pManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Log.i(TAG,"Cleared local services");
            }

            public void onFailure(int reason) {
                Log.i(TAG,"Clearing local services failed, error code " + reason);
            }
        });
    }

}
