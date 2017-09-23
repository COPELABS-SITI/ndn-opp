package pt.ulusofona.copelabs.ndn.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import pt.ulusofona.copelabs.ndn.R;

public class Identity {
    private static final String TAG = Identity.class.getSimpleName();
    public static final String SVC_INSTANCE_TYPE = "_ndnoppcl";
    public static final String SVC_TRANSFER_TYPE = SVC_INSTANCE_TYPE + "transfer" ;

    private static final String PROPERTY_UUID_KEY = "UMOBILE_UUID";
    public static final String PROPERTY_DEMO_KEY = "DEMO_ID";

    private static boolean mInitialized = false;
    private static String mAssignedUuid = null;

    private static String mScenarioIdentity = null;
    private static Map<String, String> mScenarioUuids = null;
    private static Map<String, String> mKnownPeers = null;

    /** Initialize the NDN-Opp Identity for the current device. This enables to get its UUID, the Service Type used throughout the system
     *  and service descriptors as required for making itself discoverable and transfer packets with the Connection-Less functionalities.
     * @param context the Android context to use for setting up this Identity.
     */
    public static void initialize(Context context) {
        if(!mInitialized) {
            SharedPreferences storage = context.getSharedPreferences(Identity.class.getSimpleName(), Context.MODE_PRIVATE);
            Log.v(TAG, "Is in Demo mode ? " + storage.contains(PROPERTY_DEMO_KEY));
            if(storage.contains(PROPERTY_DEMO_KEY)) {
                mScenarioIdentity = storage.getString(PROPERTY_DEMO_KEY, "R");
                mScenarioUuids = loadDemoScenario(context);
                mKnownPeers = loadKnownPeers(context);
                mAssignedUuid = mScenarioUuids.get(mScenarioIdentity);
            } else if (!storage.contains(PROPERTY_UUID_KEY)) {
                UUID uuid = UUID.randomUUID();
                mAssignedUuid = uuid.toString();
                SharedPreferences.Editor editor = storage.edit();
                editor.putString(PROPERTY_UUID_KEY, mAssignedUuid);
                editor.apply();
            } else
                mAssignedUuid = storage.getString(PROPERTY_UUID_KEY, null);
            mInitialized = true;
        }
    }

    private static Map<String, String> loadKnownPeers(Context context) {
        Map<String, String> knownPeers = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.scenario_peers)));
            try {
                while (br.ready()) {
                    String components[] = br.readLine().split(Pattern.quote(":"));
                    knownPeers.put(components[0], components[1]);
                }
            } finally { br.close(); }
        } catch (IOException e) {
            Log.d(TAG, "I/O error while reading configuration : " + e.getMessage());
        }
        Log.d(TAG, "Read Scenario Peers : " + knownPeers.toString());
        return knownPeers;
    }

    private static Map<String, String> loadDemoScenario(Context context) {
        Map<String, String> scenarioUuids = new HashMap<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.scenario_uuids)));
            try {
                while (br.ready()) {
                    String components[] = br.readLine().split(Pattern.quote(":"));
                    scenarioUuids.put(components[0], components[1]);
                }
            } finally { br.close(); }
        } catch (IOException e) {
            Log.d(TAG, "I/O error while reading configuration : " + e.getMessage());
        }
        Log.d(TAG, "Read scenario UUIDs : " + scenarioUuids.toString());
        return scenarioUuids;
    }

    /** Retrieve the UUID of the installed instance of the app (see https://docs.oracle.com/javase/7/docs/api/java/util/UUID.html)
     * @return UUID of the current device.
     */
    public static String getUuid() {
        if(!mInitialized)
            throw new RuntimeException("Uninitialized Identity. Call initialize(Context context) method prior to retrieving UUID.");

        return mAssignedUuid;
    }

    public static String getScenarioKnownPeers() {
        return mKnownPeers.get(mScenarioIdentity);
    }

    /** Create a new Wifi P2P Service Descriptor with no TXT-Record embedded into it.
      * @return the Service Descriptor for this instance of NDN-Opp
     */
    public static WifiP2pDnsSdServiceInfo getDescriptor() {
        if(!mInitialized)
            throw new RuntimeException("Uninitialized Identity. Call initialize(Context context) method prior to retrieving descriptor.");

        return WifiP2pDnsSdServiceInfo.newInstance(mAssignedUuid, SVC_INSTANCE_TYPE, null);
    }

    /** Create a new Wifi P2P Service Descriptor with a TXT-Record embedded in it. Used for the Connection-Less transfers.
     * @param txtRecord the TXT-Record to embed in this Service Descriptor that will be provided to a remote Wifi P2P Peers during the Service Discovery Phase
     * @return the Service Descriptor
     */
    public static WifiP2pDnsSdServiceInfo getTransferDescriptorWithTxtRecord(String recipient, Map<String, String> txtRecord) {
        if(!mInitialized)
            throw new RuntimeException("Uninitialized Identity. Call initialize(Context context) method prior to retrieving descriptor.");

        return WifiP2pDnsSdServiceInfo.newInstance(mAssignedUuid, SVC_TRANSFER_TYPE + "." + recipient, txtRecord);
    }
}