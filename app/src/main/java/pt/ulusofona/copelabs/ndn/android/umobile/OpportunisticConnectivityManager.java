/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class provides management of WiFi P2P Group Formation.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile;

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
import android.util.Base64;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import pt.ulusofona.copelabs.ndn.android.OperationResult;
import pt.ulusofona.copelabs.ndn.android.Utilities;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.OpportunisticPeerTracker;

/** Manager for WifiP2p connectivity to take care of everything related to forming groups, connecting
 *  devices together. */
public class OpportunisticConnectivityManager implements WifiP2pManager.ChannelListener {
    private static final String TAG = OpportunisticConnectivityManager.class.getSimpleName();
    private static final long SERVICE_DISCOVERY_INTERVAL = 3000; // Milliseconds between re-issuing a request to discover services.

    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    private String mAssignedUuid;
    private WifiP2pDnsSdServiceInfo mDescriptor;
    private WifiP2pServiceRequest mServiceRequest;

    // Maps UUIDs to lists of packets for the corresponding device
    private Map<String, String> allPackets = new HashMap<>();
    private Map<String, String> txtRecord = new HashMap<>();

    private OpportunisticFaceManager mOppFaceManager;
    private static OpportunisticConnectivityManager INSTANCE;
    private OpportunisticDaemon mOppDaemon;

    public static OpportunisticConnectivityManager getInstance() {
        if(INSTANCE == null)
            INSTANCE = new OpportunisticConnectivityManager();

        return INSTANCE;
    }

    public void enable(Context context, OpportunisticFaceManager ofm) {
        mContext = context;
        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(context, Looper.getMainLooper(), this);
        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, null, txtRecordListener);

        mAssignedUuid = Utilities.obtainUuid(context);
        mServiceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mDescriptor = WifiP2pDnsSdServiceInfo.newInstance(mAssignedUuid, OpportunisticPeerTracker.SVC_TYPE, null);

        mOppFaceManager = ofm;

        mContext.registerReceiver(mIntentReceiver, new IntentFilter(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION));
    }

    public void disable() {
        mContext.unregisterReceiver(mIntentReceiver);
    }

    private WifiP2pManager.DnsSdTxtRecordListener txtRecordListener = new WifiP2pManager.DnsSdTxtRecordListener() {
        @Override
        public void onDnsSdTxtRecordAvailable(String fulldomain, Map<String, String> txt, WifiP2pDevice dev) {
            String[] domainComponents = fulldomain.split(Pattern.quote("."));

            if (domainComponents.length >= 2) {
                String sender = domainComponents[0];
                String type = domainComponents[1];
                if (!mAssignedUuid.equals(sender) && OpportunisticPeerTracker.SVC_TYPE.equals(type)) {
                    Log.i(TAG, "Received from <" + sender + "> : " + txt.toString());

                    for(String key : txt.keySet()) {
                        // Analyze the key to determine the nature of the payload
                        String[] keyComponents = decodeKey(key);
                        if(keyComponents.length == 3) {
                            String recipient = keyComponents[0];
                            String operation = keyComponents[1];
                            String identifier = keyComponents[2];
                            if (mAssignedUuid.equals(recipient)) {
                                switch(operation) {
                                    case "I": // Payload is an Interest packet
                                        onPacketReceived(txt.get(key), sender);
                                        acknowledgeInterest(sender, identifier);
                                        break;
                                    case "i": // This is an acknowledgement for an Interest packet
                                        onInterestAcknowledged(sender, identifier);
                                        break;
                                    case "D": // Payload is a Data packet
                                        onPacketReceived(txt.get(key), sender);
                                        acknowledgeData(sender, identifier);
                                        break;
                                    case "d": // This is an acknowledgemment for a Data packet
                                        onDataAcknowledged(sender, identifier);
                                        break;
                                }
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    public void onChannelDisconnected() {
        Log.e(TAG, "Wi-Fi P2P Channel disconnected.");
    }

    private void acknowledgeInterest(String sender, String nonce) {
        String iaKey = getInterestAcknowledgmentKey(sender, nonce);
        if(!allPackets.containsKey(iaKey)) {
            allPackets.put(iaKey, null);
            txtRecord.put(iaKey, null);
            updateTxtRecord();
        }
    }

    private void acknowledgeData(String sender, String identifier) {
        String daKey = getDataAcknowledgmentKey(sender, identifier);
        if(!allPackets.containsKey(daKey)) {
            allPackets.put(daKey, null);
            txtRecord.put(daKey, null);
            updateTxtRecord();
        }
    }

    private void onPacketReceived(String sPayload, String recipient) {
        if (sPayload != null) {
            byte[] payload = Base64.decode(sPayload, Base64.DEFAULT);
            mOppDaemon.jniReceiveOnFace(mOppFaceManager.getFaceId(recipient), payload.length, payload);
        }
    }

    private void onInterestAcknowledged(String sender, String identifier) {}
    private void onDataAcknowledged(String sender, String identifier) {}

    void transferInterest(String uuid, int nonce, byte[] payload) {
        Log.v(TAG, "Transferring Interest to <" + uuid + "> nonce=" + nonce + " [" + payload.length + "]");
        // Add Interest to TXT Record entry for <uuid>
        String interestKey = getInterestKey(uuid, nonce);
        if(!allPackets.containsKey(interestKey)) {
            String base64 = Base64.encodeToString(payload, Base64.DEFAULT);
            allPackets.put(interestKey, base64);
            txtRecord.put(interestKey, base64);
            Log.v(TAG, "Key length : " + interestKey.length() + " value length : " + base64.length());
            updateTxtRecord();
        }

        Log.d(TAG, "NEW TXT Record : " + txtRecord.toString());
    }

    void cancelInterestTransfer(String uuid, int nonce) {
        Log.v(TAG, "Cancelling Interest for <" + uuid + "> [" + nonce + "]");
        String interestKey = getInterestKey(uuid, nonce);
        if(allPackets.containsKey(interestKey)) {
            allPackets.remove(interestKey);
            txtRecord.remove(interestKey);
            Log.d(TAG, "NEW TXT Record : " + txtRecord.toString());
            updateTxtRecord();
        }
    }

    private void updateTxtRecord() {
        mWifiP2pManager.removeLocalService(mWifiP2pChannel, mDescriptor, new OperationResult(TAG, "Remove Local Service"));
        mDescriptor = WifiP2pDnsSdServiceInfo.newInstance(mAssignedUuid, OpportunisticPeerTracker.SVC_TYPE, txtRecord);
        mWifiP2pManager.addLocalService(mWifiP2pChannel, mDescriptor, new OperationResult(TAG, "Add new TXT Record"));
    }

    private String getInterestKey(String uuid, int nonce) {
        return uuid + ":I:" + Integer.toString(nonce);
    }

    private String getInterestAcknowledgmentKey(String sender, String nonce) {
        return sender + ":i:" + nonce;
    }

    private String getDataKey(String recipient, String identifier) {
        return  recipient + ":D:" + identifier;
    }

    private String getDataAcknowledgmentKey(String sender, String identifier) {
        return sender + ":d:" + identifier;
    }

    private String[] decodeKey(String key) {
        return key.split(":");
    }

    void transferData(String uuid, String name, byte[] payload) {
        Log.v(TAG, "Transferring Data to <" + uuid + "> name=" + name + " [" + payload.length + "]");
        // Add Data to TXT Record entry for <uuid>
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

    public void attach(OpportunisticDaemon opportunisticDaemon) {
        mOppDaemon = opportunisticDaemon;
    }
}
