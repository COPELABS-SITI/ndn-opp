package pt.ulusofona.copelabs.ndn.android.umobile.routing.utilities;

import java.util.concurrent.TimeUnit;

/**
 * Created by miguel on 28-03-2018.
 */

public abstract class Utilities {

    public static long getTimestampInSeconds() {
        return TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
    }

}
