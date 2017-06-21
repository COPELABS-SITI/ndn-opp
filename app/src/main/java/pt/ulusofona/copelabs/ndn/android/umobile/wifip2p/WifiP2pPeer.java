/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of a Peer, which is an NDN-Opp enabled device.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

/** Wi-Fi P2P Peer implementation for representing Peers. A Wi-Fi P2P Peer is the combination of information from
 * a Wi-Fi P2P Device (status, MAC address, is GO ?) with a Wi-Fi P2P Service (UUID of device).
 */
public class WifiP2pPeer {
    private static final String TAG = WifiP2pPeer.class.getSimpleName();

    private Status currently;

    private boolean isGroupOwner;
    private boolean hasGroupOwnerField;
    private String groupOwnerMacAddress;

	private String uuid;

	private String macAddress;

    /** Create a Peer from a Device and a Service.
     * @param dev the device from which status, MAC and whether it is GO are taken
     * @param svc the service from which the UUID is retrieved
     * @return the Peer containing all the information required from the service and device perspective
     */
    static WifiP2pPeer create(WifiP2pDevice dev, WifiP2pService svc) {
        WifiP2pPeer peer = new WifiP2pPeer();

        peer.currently = dev.getStatus();

        peer.isGroupOwner = dev.isGroupOwner();
        peer.hasGroupOwnerField = dev.hasGroupOwnerField();
        peer.groupOwnerMacAddress = dev.getGroupOwnerMacAddress();

        peer.uuid = svc.getUuid();
        peer.macAddress = dev.getMacAddress();

        return peer;
    }

    public String getUuid() {
        return uuid;
    }
    public Status getStatus() { return currently; }
    public String getMacAddress() { return macAddress; }

    /** Updates the Peer information when changes are detected in the corresponding Device object (e.g. going unavailable)
     * @param dev
     */
    void update(WifiP2pDevice dev) {
        currently = dev.getStatus();
        isGroupOwner = dev.isGroupOwner();
        hasGroupOwnerField = dev.hasGroupOwnerField();
        groupOwnerMacAddress = dev.getGroupOwnerMacAddress();
        macAddress = dev.getMacAddress();
    }

    public boolean isGroupOwner() { return isGroupOwner; }
    public boolean hasGroupOwnerField() { return hasGroupOwnerField; }
    public boolean hasGroupOwner() { return groupOwnerMacAddress != null; }
}