/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-13
 * This class is a model used to represent a Neighbor
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.models;


import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.utilities.Utilities;


public class Neighbor {

    /** This attribute is used to communicate with contextual manager */
    private String mCmIdentifier;

    /** This attribute is the neighbor uuid */
    private String mUuid;

    /** These attributes are metrics which is described on dabber's draft */
    private double mC, mA, mT;

    /** This attribute is a metric which is described on dabber's draft */
    private ArrayList<Double> mI = new ArrayList<>();


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
     * This method is a getter to mT
     * @return mT
     */
    public double getT() {
        return mT;
    }

    /**
     * This method is a setter to mT
     * @param t new mT
     */
    public void setT(double t) {
        mT = t;
    }

    /**
     * This method is a getter to mI
     * @return mI
     */
    public ArrayList<Double> getI() {
        return mI;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Neighbor)) return false;

        Neighbor neighbor = (Neighbor) o;

        if (Double.compare(neighbor.mC, mC) != 0) return false;
        if (Double.compare(neighbor.mA, mA) != 0) return false;
        if (!mCmIdentifier.equals(neighbor.mCmIdentifier)) return false;
        if (!mUuid.equals(neighbor.mUuid)) return false;
        return mI != null ? mI.equals(neighbor.mI) : neighbor.mI == null;
    }

    @Override
    public int hashCode() {
        int result = mCmIdentifier.hashCode();
        result = 31 * result + mUuid.hashCode();
        return result;
    }
}
