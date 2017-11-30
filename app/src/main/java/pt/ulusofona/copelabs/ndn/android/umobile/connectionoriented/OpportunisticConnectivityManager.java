/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class provides management of WiFi P2P Group Formation.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented;

import android.content.Context;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Map;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionless.Identity;

/** Manager for WifiP2p connectivity to take care of everything related to forming groups for connecting
 *  devices together. */
public class OpportunisticConnectivityManager implements WifiP2pManager.ChannelListener {
    private static final String TAG = OpportunisticConnectivityManager.class.getSimpleName();

    private boolean mConnected = false;
    private String mAssignedUuid;
    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;
    public boolean mEnabled = false;

    /** Enable the connectivity manager. When enabled, this manager can be used to perform Group Formations
     * with other detected NDN-Opp peers.
     * @param context context within which the connection should occur
     */
    public synchronized void enable(Context context) {
        if(!mEnabled) {
            mContext = context;
            mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
            mWifiP2pChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), this);
            mAssignedUuid = Identity.getUuid();
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

    /** Implements a heuristic to select the best candidate to which connection should be performed
     * among the list of currently available NDN-Opp Peers in the neighborhood.
     *
     * Specifically, the rule is to select a device which:
     * - Is not a Group Owner
     * - Has the highest UUID
     *
     * @param candidates the map
     * @return the UUID of the device who ought to be Group Owner according to the heuristic
     */
    private String selectGroupOwner(Map<String, OpportunisticPeer> candidates) {
        Log.d(TAG, "Selecting Group Owner among " + candidates);

        String selectedUuid = mAssignedUuid;

        for (OpportunisticPeer peer : candidates.values()) {
            if (peer.isGroupOwner()) {
                selectedUuid = peer.getUuid();
                break;
            } else if (/*!peer.hasGroupOwner()*/ !peer.isGroupOwner() && selectedUuid.compareTo(peer.getUuid()) > 0)
                selectedUuid = peer.getUuid();
        }

        return selectedUuid;
    }

    public boolean isAspiringGroupOwner(Map<String, OpportunisticPeer> candidates) {
        return mAssignedUuid.equals(selectGroupOwner(candidates));
    }

    /** Initiate a group formation given a list of candidates. This method implements a simple heuristic to perform
     * a group formation in a way that maximizes the likelihood that most devices within physical proximity agree
     * on who ought to be the group owner as well as the likelihood that that device will become the group owner.
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
    public void join(Map<String, OpportunisticPeer> candidates) {
        if(mEnabled) {
            if (!mConnected) {
                String selectedUuid = selectGroupOwner(candidates);

                if (!mAssignedUuid.equals(selectedUuid)) {
                    Log.v(TAG, "Selected : " + selectedUuid);
                    OpportunisticPeer selectedGroupOwner = candidates.get(selectedUuid);

                    WifiP2pConfig connConfig = new WifiP2pConfig();
                    connConfig.deviceAddress = selectedGroupOwner.getMacAddress();
                    connConfig.wps.setup = WpsInfo.PBC;
                    connConfig.groupOwnerIntent = 0;

                    Toast.makeText(mContext, "Connecting to : " + connConfig.deviceAddress, Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Connecting to : " + connConfig.deviceAddress);

                    mConnected = true;
                    mWifiP2pManager.connect(mWifiP2pChannel, connConfig, afterConnect);
                } else {
                    Toast.makeText(mContext, "Aspiring Group Owner", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Aspiring Group Owner");
                }
            }
        }
    }

    /** Leave the currently connected Wi-Fi Direct Group */
    public void leave() {
        if(mEnabled && mConnected) {
            mConnected = false;
            mWifiP2pManager.removeGroup(mWifiP2pChannel, afterRemoveGroup);
        }
    }

    // Reporting of connection result.
    private WifiP2pManager.ActionListener afterConnect = new WifiP2pManager.ActionListener() {
        @Override public void onSuccess() {Log.d(TAG, "Connect success"); mConnected = true;}
        @Override public void onFailure(int e) {Log.d(TAG, "Connect failed (" + e + ")"); mConnected = false;}
    };

    // Reporting of disconnection result.
    private WifiP2pManager.ActionListener afterRemoveGroup = new WifiP2pManager.ActionListener() {
        @Override public void onSuccess() {Log.v(TAG, "Group removed");}
        @Override public void onFailure(int e) {Log.e(TAG, "Group removal error (" + e + ")");}
    };

    @Override
    public void onChannelDisconnected() {

    }
}
