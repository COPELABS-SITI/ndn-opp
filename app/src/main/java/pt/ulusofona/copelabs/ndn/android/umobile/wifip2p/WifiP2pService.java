/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-21
 * The WifiP2pService represents a device running a the NDN-Opp service.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

/** Model for representing a service available on some WifiP2pDevice.
 */
class WifiP2pService {
    static final String SVC_INSTANCE_TYPE = "_wifip2ptracker._tcp";

    private Status mCurrently;
    private String mUuid;
    private String mMacAddress;

    /** Main constructor
     * @param uuid UUID of the service
     * @param status current status of the service
     * @param macAddress MAC Address of the device from which this service was advertised
     */
    WifiP2pService(String uuid, Status status, String macAddress) {
		mCurrently = status;
        mUuid = uuid;
		mMacAddress = macAddress;
	}

    public String getUuid() {
        return mUuid;
    }
    public Status getStatus() { return mCurrently; }
    String getMacAddress() { return mMacAddress; }
    public void setStatus(Status newStatus) {
        mCurrently = newStatus;
    }

    /** Equality testing
     * @param other WifiP2pService against which to test
     * @return true if-and-only-if status, UUID and MAC are identical. False otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        WifiP2pService that = (WifiP2pService) other;
        return this.mCurrently == that.mCurrently
                && this.mUuid.equals(that.mUuid)
                && this.mMacAddress.equals(that.mMacAddress);
    }

    /** Hashcode function of a service. Based on UUID.
     * @return hashcode of this service instance.
     */
    @Override
    public int hashCode() {
        return mUuid.hashCode();
    }
}