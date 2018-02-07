/*
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017/9/7.
 * Class is part of the NSense application.
 */

package pt.ulusofona.copelabs.ndn.android.wifi.p2p;


import android.os.Handler;
import android.util.Log;

import java.util.Random;

/**
 * This class is responsible for schedule the different wifi p2p stages
 * @author Miguel Tavares (COPELABS/ULHT)
 * @version 1.0, 2017
 */
class WifiP2pScheduler implements Runnable {

    /** This variable is used to debug WifiP2pScheduler */
    private static final String TAG = "WifiP2pScheduler";

    /** This object is used to schedule the different stages */
    private Handler mHandler = new Handler();

    /** This object is a reference to manage WifiP2pSearcher features */
    private WifiP2pSearcher mWifiP2pSearcher;

    WifiP2pScheduler(WifiP2pSearcher wifiP2pSearcher) {
        mWifiP2pSearcher = wifiP2pSearcher;
    }

    @Override
    public void run() {
        Log.i(TAG, "Preparing to discover again.");
        mWifiP2pSearcher.startDiscovery();
        mHandler.postDelayed(this, ((new Random().nextInt(6 - 1) + 15) * 1000));
    }

    /**
     * This method starts the scheduling
     */
    public void start() {
        mHandler.post(this);
    }

    /**
     * This method stops the scheduling
     */
    public void stop() {
        mHandler.removeCallbacks(this);
    }

    /**
     * This method closes the scheduling
     */
    public void close() {
        stop();
        mWifiP2pSearcher = null;
    }

}
