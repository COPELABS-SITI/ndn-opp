/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of a Peer, which is an NDN-Opp enabled device.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

/** OpportunisticPeer implementation for representing other devices running NDN-Opp. An OpportunisticPeer
 * encapsulates information from a WifiP2pDevice (Device mStatus, MAC address) along with a Service UUID.
 */
public class OpportunisticPeer {

    private Status mStatus;
    private String mUuid;

    /** Create a Peer from a Device and a Service.
     * @param st Status of the device
     * @param uu UUID of the NDN-Opp
     */
    OpportunisticPeer(String uu, Status st) {
        mStatus = st;
        mUuid = uu;
    }


    public void setStatus(Status mStatus) {
        this.mStatus = mStatus;
    }

    public String getUuid() {
        return mUuid;
    }

    public Status getStatus() {
        return mStatus;
    }
}