/**
 * @version 1.1
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class provides a set of utility methods to routing
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.utilities;

import java.util.concurrent.TimeUnit;


public abstract class Utilities {

    /**
     * This method returns a current timestamp in seconds
     * @return timestamp in seconds
     */
    public static long getTimestampInSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

}
