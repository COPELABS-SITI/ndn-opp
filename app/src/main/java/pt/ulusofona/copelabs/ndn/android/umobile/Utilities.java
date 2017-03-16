package pt.ulusofona.copelabs.ndn.android.umobile;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class Utilities {
    private static final String PROPERTY_UUID_KEY = "UMOBILE_UUID";

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
