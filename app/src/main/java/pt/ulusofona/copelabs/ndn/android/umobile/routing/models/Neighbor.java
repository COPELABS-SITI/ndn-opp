/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-13
 * This class is a model used to represent a Neighbor
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.models;


import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import pt.ulusofona.copelabs.ndn.android.utilities.Utilities;


public class Neighbor {

    /** This attribute is used to communicate with contextual manager */
    private String mCmIdentifier;

    /** This attribute is the neighbor uuid */
    private String mUuid;

    /** These attributes are metrics which is described on dabber's draft */
    private double mC, mA, mI;

    /** This attribute is a metric which is described on dabber's draft */
    private ConcurrentHashMap<String, Double> mTs = new ConcurrentHashMap<>();


    /**
     * This method is the constructor of Neighbor class
     * @param cmCommAttribute this attribute is used to communicate with contextual manager
     * @param uuid neighbor uuid
     */
    public Neighbor(String cmCommAttribute, String uuid) {
        try {
            mCmIdentifier = Utilities.calcMd5(cmCommAttribute);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        mUuid = uuid;
    }

    /**
     * This method is a getter to mCmIdentifier
     * @return mCmIdentifier
     */
    public String getCmIdentifier() {
        return mCmIdentifier;
    }

    /**
     * This method is a setter to mCmIdentifier
     * @param cmIdentifier new mCmIdentifier
     */
    public void setCmIdentifier(String cmIdentifier) {
        mCmIdentifier = cmIdentifier;
    }

    /**
     * This method is a getter to mUuid
     * @return mUuid
     */
    public String getUuid() {
        return mUuid;
    }

    /**
     * This method is a setter to mUuid
     * @param uuid new mUuid
     */
    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    /**
     * This method is a getter to mC
     * @return mC
     */
    public double getC() {
        return mC;
    }

    /**
     * This method is a setter to mC
     * @param c new mC
     */
    public void setC(double c) {
        mC = c;
    }

    /**
     * This method is a getter to mA
     * @return mA
     */
    public double getA() {
        return mA;
    }

    /**
     * This method is a setter to mA
     * @param a new mA
     */
    public void setA(double a) {
        mA = a;
    }

    /**
     * This method returns the T value for a certain name
     * @param name name
     * @return T
     */
    public double getT(String name) {
        return mTs.contains(name) ? mTs.get(name) : 0;
    }

    /**
     * This method sets a T value to a certain name
     * @param name name
     * @param t T
     */
    public void setT(String name, double t) {
        mTs.put(name, t);
    }

    /**
     * This method is a getter to mI
     * @return mI
     */
    public double getI() {
        return mI;
    }

    /**
     * This method is a setter to mI
     * @param i new mI value
     */
    public void setI(double i) {
        mI = i;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neighbor neighbor = (Neighbor) o;
        return Double.compare(neighbor.mC, mC) == 0 &&
                Double.compare(neighbor.mA, mA) == 0 &&
                Double.compare(neighbor.mI, mI) == 0 &&
                Objects.equals(mCmIdentifier, neighbor.mCmIdentifier) &&
                Objects.equals(mUuid, neighbor.mUuid) &&
                Objects.equals(mTs, neighbor.mTs);
    }

    @Override
    public int hashCode() {
        int result = mCmIdentifier.hashCode();
        result = 31 * result + mUuid.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Uuid: " + mUuid + ", C: " + mC + ", A: " + mA + ", I: " + mI;
    }

}
