/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of a Peer, which is an NDN-Opp enabled device.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented;

import android.net.wifi.p2p.WifiP2pDevice;
import android.util.Log;

import java.lang.reflect.Field;

/** OpportunisticPeer implementation for representing other devices running NDN-Opp. An OpportunisticPeer
 * encapsulates information from a WifiP2pDevice (Device mStatus, MAC address) along with a Service UUID.
 */
public class OpportunisticPeer {
    private static final String TAG = OpportunisticPeer.class.getSimpleName();

    private Status mStatus;
    private String mUuid;
    private String mMacAddress;

    private boolean mIsGroupOwner;
    private boolean mHasGroupOwnerField;
    private String mGroupOwnerMacAddress;

    /** Create a Peer from a UUID and a Device
     * @param uu UUID of the NDN-Opp
     * @param dev WifiP2pDevice used to initialize this OpportunisticPeer
     */
    public OpportunisticPeer(String uu, WifiP2pDevice dev) {
        mStatus = Status.convert(dev.status);
        mUuid = uu;
        mMacAddress = dev.deviceAddress;

        mIsGroupOwner = dev.isGroupOwner();

        // Lower-level extraction of Group Owner/Client information. Not available on all Android devices.
        try {
            Class wpd = Class.forName("android.net.wifi.p2p.WifiP2pDevice");
            Field goAddress = wpd.getDeclaredField("groupownerAddress");
            mGroupOwnerMacAddress = (String) goAddress.get(dev);
            mHasGroupOwnerField = true;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "Class not found ...");
            mGroupOwnerMacAddress = null;
            mHasGroupOwnerField = false;
        } catch (NoSuchFieldException e) {
            Log.e(TAG, "Field not found ...");
            mGroupOwnerMacAddress = null;
            mHasGroupOwnerField = false;
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Cannot access field ...");
            mGroupOwnerMacAddress = null;
            mHasGroupOwnerField = false;
        }
    }

    public boolean isGroupOwner() {
        return mIsGroupOwner;
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

    public boolean hasGroupOwner() {
        return mHasGroupOwnerField;
    }

    public String getMacAddress() {
        return mMacAddress;
    }
}