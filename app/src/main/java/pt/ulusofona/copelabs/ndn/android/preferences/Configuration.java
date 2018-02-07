/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-02-02
 * Shared preferences in order to store NDN-OPP settings.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.preferences;


import android.content.Context;
import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public abstract class Configuration {

    /** This variable stores the uri to copelabs ndn node which is used by default */
    private static final String COPELABS_NDN_NODE = "tcp://193.137.75.171:6363";

    /** This variable is used to store in shared preferences the sending option
     * true for backup, false for size mechanism */
    private static final String SEND_OPTION = "send_option";

    /** This variable is used to store in shared preferences the ndn node to connect */
    private static final String NDN_NODE = "ndn_node";

    /**
     * This method stores in shared preferences which mechanism will be used while
     * sending messages over Wi-Fi Direct.
     * @param context Application context
     * @param status true for backup, false for size mechanism
     */
    public static void setSendOption(Context context, boolean status) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SEND_OPTION, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SEND_OPTION, status);
        editor.apply();
    }

    /**
     * This method is used to check which Wi-Fi Direct mechanism is selected
     * @param context application context
     * @return true for backup, false for size
     */
    public static boolean isBackupOptionEnabled(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SEND_OPTION, MODE_PRIVATE);
        return sharedPreferences.getBoolean(SEND_OPTION, true);
    }

    /**
     * This method stores in shared preferences which ndn will be used to connect
     * @param context application context
     * @param ndnNode ndn node to connect
     */
    public static void setNdnNode(Context context, String ndnNode) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NDN_NODE, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NDN_NODE, ndnNode);
        editor.apply();
    }

    /**
     * This method is used to fetch stored ndn node
     * @param context application context
     * @return ndn node stored
     */
    public static String getNdnNode(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(NDN_NODE, MODE_PRIVATE);
        return sharedPreferences.getString(NDN_NODE, COPELABS_NDN_NODE);
    }

}
