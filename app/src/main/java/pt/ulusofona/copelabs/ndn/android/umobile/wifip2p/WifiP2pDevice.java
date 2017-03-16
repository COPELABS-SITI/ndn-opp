/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Peer entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.util.Log;

import java.lang.reflect.Field;

class WifiP2pDevice {
    private static final String TAG = WifiP2pDevice.class.getSimpleName();
    private Status currently;
	private String name;
	private String macAddress;

    private boolean isGroupOwner;
    private boolean hasGroupOwnerField;
    private String groupOwnerMacAddress;

    static WifiP2pDevice convert(android.net.wifi.p2p.WifiP2pDevice p2pDev) {
        WifiP2pDevice dev = new WifiP2pDevice();
        dev.currently = Status.convert(p2pDev.status);
        dev.name = p2pDev.deviceName;
        dev.macAddress = p2pDev.deviceAddress;
        dev.isGroupOwner = p2pDev.isGroupOwner();

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

    public String getName() {
        return name;
    }
    public Status getStatus() { return currently; }
    String getMacAddress() { return macAddress; }

	void markAsLost() {
        currently = Status.LOST;
        isGroupOwner = false;
        hasGroupOwnerField = false;
        groupOwnerMacAddress = null;
	}

    boolean isGroupOwner() { return isGroupOwner; }
    boolean hasGroupOwnerField() { return hasGroupOwnerField; }
    String getGroupOwnerMacAddress() {return groupOwnerMacAddress;}

    @Override
    public int hashCode() { return macAddress.hashCode(); }

    @Override
    public String toString() {
        return "[" + name + "#" + macAddress + "]";
    }
}