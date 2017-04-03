/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of a Peer, which is an NDN-Opp enabled device.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

public class WifiP2pPeer {
    private static final String TAG = WifiP2pPeer.class.getSimpleName();

    private Status currently;

    private boolean isGroupOwner;
    private boolean hasGroupOwnerField;
    private String groupOwnerMacAddress;

	private String uuid;

	private String macAddress;

    private WifiP2pPeer() {}

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