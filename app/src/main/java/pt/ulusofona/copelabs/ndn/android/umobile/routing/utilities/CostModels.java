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
     * This method is used to compute K1
     * @param cost cost
     * @param i i
     * @return K1
     */
    public static long computeK1(long cost, double i) {
        long k1 = (long) (cost * i);
        Log.i(TAG, "K1, " + k1);
        return k1;
    }

    /**
     * This method is used to computes K2
     * @param k1 k1
     * @param c c
     * @param a a
     * @param t t
     * @return K2
     */
    public static double computeK2(double k1, double c, double a, double t) {
        double k2 = (0.3 * (k1 * (c + a)) + 0.7 * (k1 * t));
        Log.i(TAG, "K2, " + k2);
        return k2;
    }

    public static long computeCost(double k2) {
        long cost = (long) ((1/k2) * 100);
        return cost > 0 ? cost : 0;
    }
}
