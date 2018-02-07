/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class checks if a device is outdated on cache and remove it.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.wifi.p2p.cache;


import android.content.Context;
import android.os.Handler;

import java.util.ArrayList;
import java.util.Set;

import pt.ulusofona.copelabs.ndn.android.utilities.Utilities;

public class WifiP2pCacheRefresher implements Runnable {

    /** This variable is used to debug WifiP2pCacheRefresher */
    private static final String TAG = WifiP2pCacheRefresher.class.getSimpleName();

    /** This variable stores the time between cache verifications */
    private static final int INTERVAL_BETWEEN_REFRESH = 60 * 1000;

    /** This variable stores the time that classifies an outdated device in cache */
    private static final int CACHE_VALIDITY = 20 * 60 * 1000;

    /** This object is used to schedule a new cache verification */
    private Handler mHandler = new Handler();

    /** This object contains the application context */
    private Context mContext;


    /**
     * This method is the constructor of WifiP2pCacheRefresher class
     * @param context application context
     */
    public WifiP2pCacheRefresher(Context context) {
        mContext = context;
    }

    /**
     * This method starts the verification scheduling
     */
    public void startRefreshing() {
        scheduleRefresh();
    }

    @Override
    public void run() {
        Set<String> devices = WifiP2pCache.getDevicesIndex(mContext);
        ArrayList<String> devicesToRemove = new ArrayList<>();
        for(String device : devices) {
            long lastSeen = WifiP2pCache.getLastSeen(mContext, device);
            if(Utilities.getTimestamp() > lastSeen + CACHE_VALIDITY) {
                devicesToRemove.add(device);
            }
        }
        for(String device : devicesToRemove) {
            WifiP2pCache.removeDevice(mContext, device);
        }
        scheduleRefresh();
    }

    /**
     * This method schedules a new cache verification in INTERVAL_BETWEEN_REFRESH milliseconds
     */
    private void scheduleRefresh() {
        mHandler.postDelayed(this, INTERVAL_BETWEEN_REFRESH);
    }

    /**
     * This method stops the verification scheduling
     */
    public void stopRefreshing() {
        mHandler.removeCallbacks(this);
    }

}
