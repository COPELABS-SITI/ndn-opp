package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

public class WifiP2pConnectivityManager {
    private static final String TAG = WifiP2pConnectivityManager.class.getSimpleName();

    private boolean mConnected = false;
    private String mAssignedUuid;
    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;
    private boolean mEnabled = false;

    public synchronized void enable(Context context, WifiP2pManager wifiP2pMgr, WifiP2pManager.Channel wifiP2pChn, String uuid) {
        if(!mEnabled) {
            mContext = context;
            mWifiP2pManager = wifiP2pMgr;
            mWifiP2pChannel = wifiP2pChn;
            mAssignedUuid = uuid;
            mEnabled = true;
        }
    }

    public synchronized void disable() {
        if(mEnabled) {
            mContext = null;
            mWifiP2pManager = null;
            mWifiP2pChannel = null;
            mEnabled = false;
        }
    }

    public void join(Map<String, WifiP2pPeer> candidates) {
        if(!mConnected) {
            Log.d(TAG, "Selecting Group Owner among " + candidates);

            String selectedUuid = mAssignedUuid;

            for(WifiP2pPeer peer : candidates.values()) {
                if(peer.isGroupOwner()) {
                    selectedUuid = peer.getUuid();
                    break;
                }
                else if (!peer.hasGroupOwner() && selectedUuid.compareTo(peer.getUuid()) > 0)
                    selectedUuid = peer.getUuid();
            }

            if (!mAssignedUuid.equals(selectedUuid)) {
                Log.v(TAG, "Selected : " + selectedUuid);
                WifiP2pPeer selectedGroupOwner = candidates.get(selectedUuid);

                WifiP2pConfig connConfig = new WifiP2pConfig();
                connConfig.deviceAddress = selectedGroupOwner.getMacAddress();
                connConfig.wps.setup = WpsInfo.PBC;
                connConfig.groupOwnerIntent = 0;

                Toast.makeText(mContext, "Connecting to : " + connConfig.deviceAddress, Toast.LENGTH_LONG).show();
                Log.d(TAG, "Connecting to : " + connConfig.deviceAddress);

                mWifiP2pManager.connect(mWifiP2pChannel, connConfig, afterConnect);
                mConnected = true;
            } else {
                Toast.makeText(mContext, "Aspiring Group Owner", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Aspiring Group Owner");
            }
        } else {
            mConnected = false;
            mWifiP2pManager.removeGroup(mWifiP2pChannel, afterRemoveGroup);
        }
    }

    private WifiP2pManager.ActionListener afterConnect = new WifiP2pManager.ActionListener() {
        @Override public void onSuccess() {Log.d(TAG, "Connect success");}
        @Override public void onFailure(int e) {Log.d(TAG, "Connect failed (" + e + ")");}
    };

    private WifiP2pManager.ActionListener afterRemoveGroup = new WifiP2pManager.ActionListener() {
        @Override public void onSuccess() {Log.v(TAG, "Group removed");}
        @Override public void onFailure(int e) {Log.e(TAG, "Group removal error (" + e + ")");}
    };
}
