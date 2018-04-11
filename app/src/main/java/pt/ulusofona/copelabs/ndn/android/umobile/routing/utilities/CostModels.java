/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class contains all cost models. These models are
 * referenced in TR and also in the draft.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.utilities;

public abstract class CostModels {

    /**
     * This method is used to compute K1
     * @param cost cost
     * @param i i
     * @return K1
     */
    public static long computeK1(long cost, double i) {
        return (long) (cost * i);
    }

    /**
     * This method is used to computes K2
     * @param k1 k1
     * @param c c
     * @param a a
     * @param t t
     * @return K2
     */
    public static long computeK2(double k1, double c, double a, double t) {
        return (long) (0.3 * (k1 * (c + a)) + 0.7 * (k1 * t));
    }
}
