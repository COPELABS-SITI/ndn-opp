package pt.ulusofona.copelabs.ndn.android.umobile.routing.models;


import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.utilities.Utilities;


/**
 * Created by miguel on 07-03-2018.
 */

public class Neighbor {

    private String mCmIdentifier;
    private String mUuid;
    private double mC, mA, mT;
    private ArrayList<Double> mI = new ArrayList<>();

    public Neighbor(String wifiP2pMac, String uuid) {
        try {
            mCmIdentifier = Utilities.calcMd5(wifiP2pMac);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        mUuid = uuid;
    }

    public String getCmIdentifier() {
        return mCmIdentifier;
    }

    public void setCmIdentifier(String cmIdentifier) {
        mCmIdentifier = cmIdentifier;
    }

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public double getC() {
        return mC;
    }

    public void setC(double c) {
        mC = c;
    }

    public double getA() {
        return mA;
    }

    public void setA(double a) {
        mA = a;
    }

    public double getT() {
        return mT;
    }

    public void setT(double t) {
        mT = t;
    }

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
