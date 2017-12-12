/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class only provides a utility function to retrieve or generate a UUID.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.nio.ByteBuffer;
import java.util.UUID;

/** Utility class for methods used throughout the application. */
public class Utilities {
    private static final String PROPERTY_UUID_KEY = "UMOBILE_UUID";


    public static String obtainUuid(Context context) {
        String sUuid;
        SharedPreferences storage = context.getSharedPreferences("Configuration", Context.MODE_PRIVATE);
        if(!storage.contains(PROPERTY_UUID_KEY)) {
            UUID uuid = UUID.randomUUID();
            ByteBuffer bUuid = ByteBuffer.allocate(16);
            bUuid.putLong(0, uuid.getMostSignificantBits());
            bUuid.putLong(8, uuid.getLeastSignificantBits());
            sUuid = Base64.encodeToString(bUuid.array(), Base64.NO_PADDING);
            SharedPreferences.Editor editor = storage.edit();
            editor.putString(PROPERTY_UUID_KEY, sUuid);
            editor.apply();
        } else
            sUuid = storage.getString(PROPERTY_UUID_KEY, null);

        return sUuid;
    }

}
