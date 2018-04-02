/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-13
 * This class is a model used to represent a Routing Entry
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.models;

import android.support.annotation.NonNull;


public class RoutingEntry implements Comparable<RoutingEntry> {

    /** This attribute is described on NFD Developer's Guide */
    private static final long DEFAULT_ORIGIN = 128L;

    /** This attribute is described on NFD Developer's Guide */
    private static final long DEFAULT_FLAG = 1L;

    /** This attribute is described on NFD Developer's Guide */
    private String mPrefix;

    /** This attribute is described on NFD Developer's Guide */
    private long mFace;

    /** This attribute is described on NFD Developer's Guide */
    private long mOrigin;

    /** This attribute is described on NFD Developer's Guide */
    private long mCost;

    /** This attribute is described on NFD Developer's Guide */
    private long mFlag;


    /**
     * This method is the constructor of RoutingEntry class
     * @param prefix name prefix
     * @param face face id
     * @param cost face cost
     */
    public RoutingEntry(String prefix, long face, long cost) {
        mPrefix = prefix;
        mFace = face;
        mCost = cost;
        mOrigin = DEFAULT_ORIGIN;
        mFlag = DEFAULT_FLAG;
    }

    /**
     * This method is a getter to mPrefix
     * @return mPrefix
     */
    public String getPrefix() {
        return mPrefix;
    }

    /**
     * This method is a setter to mPrefix
     * @param prefix new mPrefix
     */
    public void setPrefix(String prefix) {
        mPrefix = prefix;
    }

    /**
     * This method is a getter to mFace
     * @return mFace
     */
    public long getFace() {
        return mFace;
    }

    /**
     * This method is a setter to mFace
     * @param face new mFace
     */
    public void setFace(long face) {
        mFace = face;
    }

    /**
     * This method is a getter to mOrigin
     * @return mOrigin
     */
    public long getOrigin() {
        return mOrigin;
    }

    /**
     * This method is a setter to mOrigin
     * @param origin new mOrigin
     */
    public void setOrigin(long origin) {
        mOrigin = origin;
    }

    /**
     * This method is a getter to mCost
     * @return mCost
     */
    public long getCost() {
        return mCost;
    }

    /**
     * This method is a setter to mCost
     * @param cost new mCost
     */
    public void setCost(long cost) {
        mCost = cost;
    }

    /**
     * This method is a getter to mFlag
     * @return mFlag
     */
    public long getFlag() {
        return mFlag;
    }

    /**
     * This method is a setter to mFlag
     * @param flag new mFlag
     */
    public void setFlag(long flag) {
        mFlag = flag;
    }

    @Override
    public int compareTo(@NonNull RoutingEntry routingEntry) {
        return Double.compare(routingEntry.getCost(), mCost);
    }
}
