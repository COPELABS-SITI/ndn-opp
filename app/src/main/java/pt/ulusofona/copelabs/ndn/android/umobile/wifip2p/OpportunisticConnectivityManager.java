/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class provides management of WiFi P2P Group Formation.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import pt.ulusofona.copelabs.ndn.android.OperationResult;
import pt.ulusofona.copelabs.ndn.android.Utilities;

/** Manager for WifiP2p connectivity to take care of everything related to forming groups, connecting
 *  devices together. */
public class OpportunisticConnectivityManager implements WifiP2pManager.ChannelListener {
    private static final String TAG = OpportunisticConnectivityManager.class.getSimpleName();
    private static final long SERVICE_DISCOVERY_INTERVAL = 3000; // Milliseconds between re-issuing a request to discovery services.

    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    private String mAssignedUuid;
    private WifiP2pDnsSdServiceInfo mDescriptor;
    private WifiP2pServiceRequest mServiceRequest;
    private Map<String, String> txtRecord = new HashMap<>();
    private static OpportunisticConnectivityManager INSTANCE;

    public static OpportunisticConnectivityManager getInstance() {
        if(INSTANCE == null)
            INSTANCE = new OpportunisticConnectivityManager();

        return INSTANCE;
    }

    public void enable(Context context) {
        //super.onAttach(context);

        mContext = context;
        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(context, Looper.getMainLooper(), this);
        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, null, txtRecordListener);

        mAssignedUuid = Utilities.obtainUuid(context);
        mServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mDescriptor = WifiP2pDnsSdServiceInfo.newInstance(mAssignedUuid, OpportunisticPeerTracker.SVC_TYPE, null);

        mContext.registerReceiver(mIntentReceiver, new IntentFilter(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION));
    }

    public void disable() {
        mContext.unregisterReceiver(mIntentReceiver);
        //super.onDetach();
    }

    private WifiP2pManager.DnsSdTxtRecordListener txtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {
        @Override
        public void onDnsSdTxtRecordAvailable(String fulldomain, Map<String, String> txt, WifiP2pDevice dev) {
            String[] components = fulldomain.split(Pattern.quote("."));

            if (components.length >= 2) {
                String uuid = components[0];
                String type = components[1];
                if (!mAssignedUuid.equals(uuid)) {
                    if (OpportunisticPeerTracker.SVC_TYPE.equals(type)) {
                        Log.i(TAG, "Received from <" + uuid + "> : " + txt.toString());
                    }
                }
            }
        }
    };

    @Override
    public void onChannelDisconnected() {
        Log.e(TAG, "Wi-Fi P2P Channel disconnected.");
    }

    private Handler mHandler = new Handler();
    private Runnable mDiscoverServices = new Runnable() {
        @Override
        public void run() {
            mWifiP2pManager.discoverServices(mWifiP2pChannel, new OperationResult(TAG, "Service Discovery"));
            mHandler.postDelayed(mDiscoverServices, SERVICE_DISCOVERY_INTERVAL);
        }
    };

    private BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int extra = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

                switch (extra) {
                case WifiP2pManager.WIFI_P2P_STATE_ENABLED:
                    mWifiP2pManager.addLocalService(mWifiP2pChannel, mDescriptor, new OperationResult(TAG, "Local Service addition"));
                    mWifiP2pManager.addServiceRequest(mWifiP2pChannel, mServiceRequest, new OperationResult(TAG, "Service Request addition"));
                    mHandler.postDelayed(mDiscoverServices, 100);
                    break;
                case WifiP2pManager.WIFI_P2P_STATE_DISABLED:
                    mWifiP2pManager.removeLocalService(mWifiP2pChannel, mDescriptor, new OperationResult(TAG, "Local Service removal"));
                    mWifiP2pManager.removeServiceRequest(mWifiP2pChannel, mServiceRequest, new OperationResult(TAG, "Service Request removal"));
                    mHandler.removeCallbacks(mDiscoverServices);
                    break;
                default:
                    Log.e(TAG, "EXTRA_WIFI_P2P_STATE not found in Intent ...");
                    break;
                }
            }
        }
    };
}
