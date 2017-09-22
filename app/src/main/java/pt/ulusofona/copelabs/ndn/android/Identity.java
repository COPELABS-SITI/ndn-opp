package pt.ulusofona.copelabs.ndn.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;

import java.util.Map;
import java.util.UUID;

public class Identity {
    public static final String SVC_INSTANCE_TYPE = "_ndnoppcl";
    public static final String SVC_TRANSFER_TYPE = SVC_INSTANCE_TYPE + "transfer" ;

    private static final String PROPERTY_UUID_KEY = "UMOBILE_UUID";

    private static boolean mInitialized = false;
    private static String mAssignedUuid = null;

    /** Initialize the NDN-Opp Identity for the current device. This enables to get its UUID, the Service Type used throughout the system
     *  and service descriptors as required for making itself discoverable and transfer packets with the Connection-Less functionalities.
     * @param context the Android context to use for setting up this Identity.
     */
    public static void initialize(Context context) {
        if(!mInitialized) {
            SharedPreferences storage = context.getSharedPreferences(Identity.class.getSimpleName(), Context.MODE_PRIVATE);
            if(!storage.contains(PROPERTY_UUID_KEY)) {
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

    /** Retrieve the UUID of the installed instance of the app (see https://docs.oracle.com/javase/7/docs/api/java/util/UUID.html)
     * @return UUID of the current device.
     */
    public static String getUuid() {
        if(!mInitialized)
            throw new RuntimeException("Uninitialized Identity. Call initialize(Context context) method prior to retrieving UUID.");

        return mAssignedUuid;
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