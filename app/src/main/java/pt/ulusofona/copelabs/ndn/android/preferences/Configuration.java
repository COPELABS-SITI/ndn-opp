package pt.ulusofona.copelabs.ndn.android.preferences;


import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public abstract class Configuration {

    private static final String SEND_OPTION = "send_option";

    public static void setSendOption(Context context, boolean status) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SEND_OPTION, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SEND_OPTION, status);
        editor.apply();
    }

    public static boolean isBackupOptionEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SEND_OPTION, MODE_PRIVATE);
        return sharedPreferences.getBoolean(SEND_OPTION, true);
    }

}
