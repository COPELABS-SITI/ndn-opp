/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class keeps track of WiFi P2P connection status advertised by Android.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Observable;

/** Implementation of a connectivity tracker. This class is used to cleanly conceal the logic of keeping track
 * of which Wi-Fi Direct Group the current device is connected to. Implemented as a Singleton and an Observable.
 */
public class WifiP2pConnectivityTracker extends Observable {
    private static final String TAG = WifiP2pConnectivityTracker.class.getSimpleName();

    private static WifiP2pConnectivityTracker INSTANCE = null;

    private final IntentFilter mIntents = new IntentFilter();
    private final ConnectionIntentReceiver mIntentReceiver = new ConnectionIntentReceiver();

    private boolean mEnabled = false;

    private boolean mConnected = false;
    private String mLastAccessPoint = null;
    private String mAssignedIpv4 = null;

    /** Retrieve the singleton instance.
     * @return the singleton instance of the connectivity tracker
     */
    public static WifiP2pConnectivityTracker getInstance() {
        if(INSTANCE == null)
            INSTANCE = new WifiP2pConnectivityTracker();
        return INSTANCE;
    }

    private WifiP2pConnectivityTracker() {
        mIntents.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    }

    /** Enable the connectivity tracker.
     * @param ctxt Android context within which the tracker should be registered
     */
    public synchronized void enable(Context ctxt) {
        if(!mEnabled) {
            ctxt.registerReceiver(mIntentReceiver, mIntents);
            mEnabled = true;
        }
    }

    /** Disable the connectivity tracker.
     * @param ctxt Android context within which the tracker is registered
     */
    public synchronized void disable(Context ctxt) {
        if(mEnabled) {
            ctxt.unregisterReceiver(mIntentReceiver);
            mConnected = false;
            mLastAccessPoint = null;
            mAssignedIpv4 = null;
            mEnabled = false;
        }
    }

    /** Retrieve the IP assigned to the current device in the current Wi-Fi Direct Group.
     * @return the IP address
     */
    public String getAssignedIp() {
        return mAssignedIpv4;
    }

    private class ConnectionIntentReceiver extends BroadcastReceiver {
        private boolean changeDetected(String lastNetworkName, String currentNetworkName) {
            boolean changeDetected;

            if(lastNetworkName != null)
                changeDetected = !lastNetworkName.equals(currentNetworkName);
            else
                changeDetected = (currentNetworkName != null);

            Log.d(TAG, "Change detected : " + changeDetected);

            return changeDetected;
        }

        private String extractIp(WifiP2pGroup group) {
            String ipAddress = null;
            String interfaceName = group.getInterface();
            if(interfaceName != null) {
                try {
                    Enumeration<NetworkInterface> allIfaces = NetworkInterface.getNetworkInterfaces();
                    Log.v(TAG, allIfaces.toString());
                    while (allIfaces.hasMoreElements()) {
                        NetworkInterface iface = allIfaces.nextElement();
                        if(interfaceName.equals(iface.getName())) {
                            for (InterfaceAddress ifAddr : iface.getInterfaceAddresses()) {
                                InetAddress address = ifAddr.getAddress();
                                if (address instanceof Inet4Address && !address.isAnyLocalAddress())
                                    ipAddress = address.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }
            return ipAddress;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "Received Intent : " + action);

            if(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
                NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                WifiP2pInfo wifiP2pInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_INFO);
                WifiP2pGroup wifip2pGroup = intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_GROUP);

                String currentGroupName = wifip2pGroup.getNetworkName();

                Log.v(TAG, "NetworkInfo : " + netInfo);
                Log.v(TAG, "WifiP2pInfo : " + wifiP2pInfo);
                Log.v(TAG, "WifiP2pGroup : " + wifip2pGroup);

                if(changeDetected(mLastAccessPoint, currentGroupName)) {
                    mAssignedIpv4 = netInfo.isConnected() ? extractIp(wifip2pGroup) : null;
                    Log.v(TAG, "Assigned IPv4 : " + mAssignedIpv4);
                    mConnected = currentGroupName != null;
                    setChanged(); notifyObservers(mConnected);
                }

                mLastAccessPoint = currentGroupName;
            }
        }
    }
}