package pt.ulusofona.copelabs.ndn.android.preferences;


import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public abstract class Configuration {

    private static final String COPELABS_NDN_NODE = "tcp://193.137.75.171:6363";
    private static final String SEND_OPTION = "send_option";
    private static final String NDN_NODE = "ndn_node";

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

    public static void setNdnNode(Context context, String ndnNode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NDN_NODE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NDN_NODE, ndnNode);
        editor.apply();
    }

    public static String getNdnNode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NDN_NODE, MODE_PRIVATE);
        return sharedPreferences.getString(NDN_NODE, COPELABS_NDN_NODE);
    }

}
