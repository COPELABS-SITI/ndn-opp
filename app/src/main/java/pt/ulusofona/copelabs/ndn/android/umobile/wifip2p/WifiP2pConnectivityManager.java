/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class provides management of WiFi P2P Group Formation.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

/** Manager for WifiP2p connectivity to take care of everything related to forming groups, connecting
 *  devices together. */
public class WifiP2pConnectivityManager {
    private static final String TAG = WifiP2pConnectivityManager.class.getSimpleName();

    private boolean mConnected = false;
    private String mAssignedUuid;
    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;
    private boolean mEnabled = false;

    /** Enable the connectivity manager;
     * @param context context within which the connection should occur
     * @param wifiP2pMgr Wi-Fi P2P Manager to rely on for establishing connections (groups)
     * @param wifiP2pChn Wi-Fi P2P Channel to use
     * @param uuid UUID assigned to the current device
     */
    public synchronized void enable(Context context, WifiP2pManager wifiP2pMgr, WifiP2pManager.Channel wifiP2pChn, String uuid) {
        if(!mEnabled) {
            mContext = context;
            mWifiP2pManager = wifiP2pMgr;
            mWifiP2pChannel = wifiP2pChn;
            mAssignedUuid = uuid;
            mEnabled = true;
        }
    }

    /** Disable the connectivity manager; group formation will no longer be possible
     */
    public synchronized void disable() {
        if(mEnabled) {
            mContext = null;
            mWifiP2pManager = null;
            mWifiP2pChannel = null;
            mEnabled = false;
        }
    }

    /** Initiate a group formation given a list of candidates. This method implements a simple heuristic to perform
     * a group formation in a way that maximizes the likelihood that most devices within physical proximity agree
     * on who ought to be the group owner as well as the likelihood that that device will become the group owner.
     *
     * Specifically, the rule is to select a device which:
     * - Is not a Group Owner
     * - Has the highest UUID
     *
     * Once a candidate has been selected, the current device will attempt a Group Formation (PBC) and set its own
     * Group Owner Intent to 0, thus making it the most likely that the candidate will emerge as Group Owner. This
     * step is only important when only two devices are involved.
     *
     * If there is no candidate satisfying those two criterions, this means that the current device
     * is the one with the highest UUID and that others will select it for Group Formation.
     *
     * @param candidates a map of all peers available around the current device
     */
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

    // Reporting of connection result.
    private WifiP2pManager.ActionListener afterConnect = new WifiP2pManager.ActionListener() {
        @Override public void onSuccess() {Log.d(TAG, "Connect success");}
        @Override public void onFailure(int e) {Log.d(TAG, "Connect failed (" + e + ")");}
    };

    // Reporting of disconnection result.
    private WifiP2pManager.ActionListener afterRemoveGroup = new WifiP2pManager.ActionListener() {
        @Override public void onSuccess() {Log.v(TAG, "Group removed");}
        @Override public void onFailure(int e) {Log.e(TAG, "Group removal error (" + e + ")");}
    };
}
