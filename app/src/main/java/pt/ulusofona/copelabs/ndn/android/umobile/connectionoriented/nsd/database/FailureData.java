/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class is used to store data related with the
 * connection oriented status.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.database;


import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class FailureData {

    /** This variable is used to debug FailureData class */
    private static final String TAG = FailureData.class.getSimpleName();

    /** This HashMap stores information related with the disconnection */
    private ConcurrentHashMap<String, Integer> mFailureData = new ConcurrentHashMap<>();

    /** This HashMap stores information related with connected devices */
    private ConcurrentHashMap<String, NsdInfo> mPeerData = new ConcurrentHashMap<>();

    /**
     * This method stores this device in HashMaps
     * @param nsdInfo device to add
     */
    public void add(NsdInfo nsdInfo) {
        Log.i(TAG, "Adding " + nsdInfo.toString());
        mPeerData.put(nsdInfo.getUuid(), nsdInfo);
        mFailureData.put(nsdInfo.getUuid(), 0);
    }

    /**
     * This method removes this device from HashMaps
     * @param nsdInfo device to remove
     */
    public void remove(NsdInfo nsdInfo) {
        Log.i(TAG, "Removing " + nsdInfo.toString());
        mPeerData.remove(nsdInfo.getUuid());
        mFailureData.remove(nsdInfo.getUuid());
    }

    /**
     * This method increments a failure to a specific host
     * @param nsdInfo host to increment failure
     */
    public void failed(NsdInfo nsdInfo) {
        if(contains(nsdInfo)) {
            int failures = mFailureData.get(nsdInfo.getUuid());
            mFailureData.put(nsdInfo.getUuid(), ++failures);
        }
    }

    /**
     * This method resets the failures of a specific host
     * @param nsdInfo host to reset
     */
    public void reset(NsdInfo nsdInfo) {
        if(contains(nsdInfo)) {
            mFailureData.put(nsdInfo.getUuid(), 0);
        }
    }

    /**
     * This method is used to clear both HashMaps
     */
    public void clear() {
        mFailureData.clear();
        mPeerData.clear();
    }

    /**
     * This method checks if a device exists in both HashMaps
     * @param nsdInfo device to check
     * @return true if exists, false if not
     */
    public boolean contains(NsdInfo nsdInfo) {
        return mFailureData.containsKey(nsdInfo.getUuid()) && mPeerData.containsKey(nsdInfo.getUuid());
    }

    /**
     * This method returns how many times a device failed
     * @param nsdInfo device to check
     * @return number of fails
     */
    public int getFailures(NsdInfo nsdInfo) {
        return mFailureData.get(nsdInfo.getUuid());
    }

    /**
     * This method returns the list of connected devices
     * @return connected devices
     */
    public ArrayList<NsdInfo> getPeers() {
        return new ArrayList<>(mPeerData.values());
    }

}
