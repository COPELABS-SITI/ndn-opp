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

    /** This attribute holds the neighbor uuid */
    private String mNeighbor;

    /** This attribute is described on NFD Developer's Guide */
    private String mPrefix;

    /** This attribute stores which strategy will be used */
    private String mStrategy;

    /** This attribute is described on NFD Developer's Guide */
    private long mFace;

    /** This attribute is described on NFD Developer's Guide */
    private long mOrigin;

    /** This attribute is described on NFD Developer's Guide */
    private long mCost;

    /** This attribute is described on NFD Developer's Guide */
    private long mFlag;


    /**
     * This method a constructor of RoutingEntry class
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
     * This method a constructor of RoutingEntry class
     * @param neighbor neighbor uuid
     * @param prefix name prefix
     * @param face face id
     * @param cost face cost
     */
    public RoutingEntry(String neighbor, String prefix, long face, long cost) {
        mNeighbor = neighbor;
        mPrefix = prefix;
        mFace = face;
        mCost = cost;
        mOrigin = DEFAULT_ORIGIN;
        mFlag = DEFAULT_FLAG;
    }

    /**
     * This method is a getter to mNeighbor
     * @return mNeighbor
     */
    public String getNeighbor() {
        return mNeighbor;
    }

    /**
     * This method is a setter to mNeighbor
     * @param neighbor new mNeighbor
     */
    public void setNeighbor(String neighbor) {
        mNeighbor = neighbor;
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
     * This method is a getter to mStrategy
     * @return mStrategy
     */
    public String getStrategy() {
        return mStrategy;
    }

    /**
     * This method is a setter to strategy
     * @param strategy new mStrategy
     */
    public void setStrategy(String strategy) {
        mStrategy = strategy;
    }

    /**
     * This method checks if there is any strategy defined
     * @return true if yes, false if not
     */
    public boolean isStrategyDefined() {
        return mStrategy != null;
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

    @Override
    public String toString() {
        return "Neighbor, " + mNeighbor + " face, " + mFace + " name, " + mPrefix + " cost: " + mCost;
    }
}
