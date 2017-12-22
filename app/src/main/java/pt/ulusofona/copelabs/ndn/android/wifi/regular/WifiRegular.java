package pt.ulusofona.copelabs.ndn.android.wifi.regular;

import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import static android.net.wifi.WifiManager.NETWORK_STATE_CHANGED_ACTION;

public class WifiRegular {

    private static final String TAG = WifiRegular.class.getSimpleName();

    /** This object stores the application context */
    private Context mContext;
    private WifiRegularReceiver mWifiRegularReceiver;

    public WifiRegular(Context context) {
        mContext = context;
        mWifiRegularReceiver = new WifiRegularReceiver();
    }

    /**
     * This method starts this component
     */
    public void start() {
        Log.i(TAG, "Start");
        mContext.registerReceiver(mWifiRegularReceiver, new IntentFilter(NETWORK_STATE_CHANGED_ACTION));
    }

    /**
     * This method stops this component
     */
    public void stop() {
        Log.i(TAG, "Stop");
        mContext.unregisterReceiver(mWifiRegularReceiver);
    }
}
