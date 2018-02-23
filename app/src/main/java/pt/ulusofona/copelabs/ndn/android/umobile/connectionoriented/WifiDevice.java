/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * Implementation of the WiFi P2P device entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented;

import android.util.Log;

import java.lang.reflect.Field;

/** Customized representation of WifiDevice. Essentially expands the android.net.wifi.p2p.WifiDevice
 * by including details about whether the corresponding device is currently a Group Owner or a Client of a
 * Wi-Fi Direct Group.
 */
public class WifiDevice {
    private static final String TAG = WifiDevice.class.getSimpleName();
    private Status currently;
	private String name;
	private String macAddress;

    private boolean isGroupOwner;
    private boolean hasGroupOwnerField;
    private String groupOwnerMacAddress;

    /** Conversion of Android WifiP2pDevice to our own WifiDevice.
     * @param p2pDev original peer details provided by Android
     * @return WifiDevice including additional fields; namely whether the device is currently an owner or a client of a group
     */
    public static WifiDevice convert(android.net.wifi.p2p.WifiP2pDevice p2pDev) {
        WifiDevice dev = new WifiDevice();
        dev.currently = Status.convert(p2pDev.status);
        dev.name = p2pDev.deviceName;
        dev.macAddress = p2pDev.deviceAddress;
        dev.isGroupOwner = p2pDev.isGroupOwner();

        // Lower-level extraction of Group Owner/Client information. Not available on all Android devices.
        try {
            Class wpd = Class.forName("android.net.wifi.p2p.WifiP2pDevice");
            Field goAddress = wpd.getDeclaredField("groupownerAddress");
            dev.groupOwnerMacAddress = (String) goAddress.get(p2pDev);
            dev.hasGroupOwnerField = true;
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "Class not found ...");
            dev.groupOwnerMacAddress = null;
            dev.hasGroupOwnerField = false;
        } catch (NoSuchFieldException e) {
            Log.d(TAG, "Field not found ...");
            dev.groupOwnerMacAddress = null;
            dev.hasGroupOwnerField = false;
        } catch (IllegalAccessException e) {
            Log.d(TAG, "Cannot access field ...");
            dev.groupOwnerMacAddress = null;
            dev.hasGroupOwnerField = false;
        }

        return dev;
    }

    /** Retrieve the user-defined name of the device.
     * @return Name of the peer
     */
    public String getName() {
        return name;
    }
    public Status getStatus() { return currently; }
    String getMacAddress() { return macAddress; }

    /** Used to mark the device as no longer available. Called when device is no longer reported
     * by Android in the peer list.
     */
	void markAsLost() {
        currently = Status.LOST;
        isGroupOwner = false;
        hasGroupOwnerField = false;
        groupOwnerMacAddress = null;
	}

    boolean isGroupOwner() { return isGroupOwner; }
    boolean hasGroupOwnerField() { return hasGroupOwnerField; }
    String getGroupOwnerMacAddress() {return groupOwnerMacAddress;}

    /** Hashcode of device. Based on String.hashCode() of the MAC.
     * @return macAddress.hashCode()
     */
    @Override
    public int hashCode() { return macAddress.hashCode(); }

    /** Pretty-printable string representation of the device. Contains name and MAC.
     * @return
     */
    @Override
    public String toString() {
        return "[" + name + "#" + macAddress + "#" + currently +"]";
    }
}