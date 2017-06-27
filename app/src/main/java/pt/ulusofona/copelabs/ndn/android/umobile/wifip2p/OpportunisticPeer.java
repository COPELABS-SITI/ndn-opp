/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of a Peer, which is an NDN-Opp enabled device.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

/** OpportunisticPeer implementation for representing other devices running NDN-Opp. An OpportunisticPeer
 * encapsulates information from a WifiP2pDevice (Device status, MAC address, is the device
 * a Group Owner or a Client of a Group) and from a WifiP2pService (Service UUID).
 */
public class OpportunisticPeer {
    private WifiP2pDevice mDevice;
    private WifiP2pService mService;

    /** Create a Peer from a Device and a Service.
     * @param dev the device from which status, MAC and whether it is GO are taken
     * @param svc the service from which the UUID is retrieved
     * @return the Peer containing all the information required from the service and device perspective
     */
    static OpportunisticPeer create(WifiP2pDevice dev, WifiP2pService svc) {
        OpportunisticPeer peer = new OpportunisticPeer();

        peer.mDevice = dev;
        peer.mService = svc;

        return peer;
    }

    /** Updates the Peer information when changes are detected in the corresponding Device object (e.g. going unavailable)
     * @param device the device corresponding to this Peer
     */
    void update(WifiP2pDevice device) {
        mDevice = device;
    }

    public String getUuid() {
        return mService.getUuid();
    }
    public Status getStatus() { return mDevice.getStatus(); }
    public String getMacAddress() { return mDevice.getMacAddress(); }

    public boolean isGroupOwner() { return mDevice.isGroupOwner(); }
    public boolean hasGroupOwnerField() { return mDevice.hasGroupOwnerField(); }
    public boolean hasGroupOwner() { return mDevice.getGroupOwnerMacAddress() != null; }
}