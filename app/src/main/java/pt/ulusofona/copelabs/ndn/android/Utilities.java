/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class only provides a utility function to retrieve or generate a UUID.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

/** Utility class for methods used throughout the application. */
public class Utilities {
    private static final String PROPERTY_UUID_KEY = "UMOBILE_UUID";

    /** Retrieve the UUID of the installed instance of the app.
     * @param context Android-provided context from which the UUID can be retrieved
     * @return UUID of the current device
     */
    public static String obtainUuid(Context context) {
        String uuid;
        SharedPreferences storage = context.getSharedPreferences("Configuration", Context.MODE_PRIVATE);
        if(!storage.contains(PROPERTY_UUID_KEY)) {
            uuid = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = storage.edit();
            editor.putString(PROPERTY_UUID_KEY, uuid);
            editor.apply();
        } else
            uuid = storage.getString(PROPERTY_UUID_KEY, null);

        return uuid;
    }

}
