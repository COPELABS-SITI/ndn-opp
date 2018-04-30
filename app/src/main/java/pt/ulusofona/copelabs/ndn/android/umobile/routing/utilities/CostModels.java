/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class contains all cost models. These models are
 * referenced in TR and also in the draft.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.utilities;

import android.util.Log;

public abstract class CostModels {

    /** This variable is used to debug this class*/
    private static final String TAG = CostModels.class.getSimpleName();

    /**
     * This method is used to compute V1
     * @param v cost
     * @param i i
     * @param iMax imax
     * @return V1
     */
    public static long computeV1(long v, double i, double iMax) {
        double i1 = i / iMax;
        long v1 = (long) (v * i1);
        Log.i(TAG, "V1, " + v1);
        return v1;
    }

    /**
     * This method is used to computes V2
     * @param v1 k1
     * @param c c
     * @param cMax cmax
     * @param a a
     * @param t t
     * @return V2
     */
    public static double computeV2(long v, double v1, double c, double cMax, double a, double t) {
        double c1 = c / cMax;
        double v2 = (0.3 * (v1 * (c1 * a)) + 0.7 * (v1 * (t / v)));
        Log.i(TAG, "V2, " + v2);
        return v2;
    }

    // TODO check best formula for cost
    public static long computeCost(double v2) {
        long cost = (long) ((1 / v2) * 1000000000);
        Log.i(TAG, "Cost, " + cost);
        return cost > 0 ? cost : 0;
    }
}
