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

    /** This object is a reference to manage WifiP2pGo features */
    private WifiP2pGo mWifiP2pGo;

    /** This variable stores the current discovery stage */
    private int mDiscState = 0;

    WifiP2pScheduler(WifiP2pGo wifiP2pGo, WifiP2pSearcher wifiP2pSearcher) {
        mWifiP2pGo = wifiP2pGo;
        mWifiP2pSearcher = wifiP2pSearcher;
    }

    @Override
    public void run() {
        Log.i(TAG, "mDiscState is: " + mDiscState);
        if (mDiscState == 0) {
            mWifiP2pSearcher.startDiscovery();
            mDiscState = 1;
            mHandler.postDelayed(this, ((new Random().nextInt(6 - 1) + 15) * 1000));
        } else if (mDiscState == 1) {
            mWifiP2pGo.createGroup();
            mDiscState = 2;
            mHandler.postDelayed(this, ((new Random().nextInt(6 - 1) + 7) * 1000));
        } else if (mDiscState == 2) {
            mWifiP2pGo.removeGroup();
            mDiscState = new Random().nextInt(2);
            mHandler.postDelayed(this, 4000);
        }
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
        mWifiP2pGo = null;
    }

}
