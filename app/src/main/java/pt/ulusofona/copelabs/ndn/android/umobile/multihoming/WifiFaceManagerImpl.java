/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-12-22
 * This class is responsible to implement the multihoming feature
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.multihoming;


import android.content.Context;
import android.os.Handler;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.preferences.Configuration;
import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.wifi.regular.WifiRegularListener;
import pt.ulusofona.copelabs.ndn.android.wifi.regular.WifiRegularListenerManager;

public class WifiFaceManagerImpl implements WifiFaceManager, Runnable, WifiRegularListener {

    /** This variable is used to debug WifiFaceManagerImpl class */
    private static final String TAG = WifiFaceManagerImpl.class.getSimpleName();

    /** This time interval in order to create the route right after the creation of Wi-Fi's face */
    private static final int WAIT_TIME = 1000;

    /** This interface is used to communicate with the binder */
    private OpportunisticDaemon.Binder mBinder;

    /** This object is used to schedule a new wait right after the Wi-Fi's face creation */
    private Handler mHandler = new Handler();

    /** This object holds the application context */
    private Context mContext;

    /** This variable holds the state of this class, if is enabled or not */
    private boolean mEnabled;


    /**
     * This method enables the multihoming feature
     * @param context application context
     * @param binder daemon reference
     */
    @Override
    public synchronized void enable(Context context, OpportunisticDaemon.Binder binder) {
        if(!mEnabled) {
            mBinder = binder;
            mContext = context;
            WifiRegularListenerManager.registerListener(this);
            mEnabled = true;
        }
    }

    /**
     * This method is used to disable the multihoming feature
     */
    @Override
    public void disable() {
        WifiRegularListenerManager.unregisterListener(this);
        mEnabled = false;
    }

    /**
     * This method is invoked when the connection between this device and
     * the AP is successfully performed.
     */
    @Override
    public void onConnected() {
        Log.i(TAG, "I'm connected to an AP");
        createFaceAndRoute();
    }

    /**
     * This method is called when the connection with the AP goes down
     */
    @Override
    public void onDisconnected() {
        Log.i(TAG, "I'm disconnected");
    }

    /**
     * This method creates the face and the route.
     */
    private void createFaceAndRoute() {
        Log.i(TAG, "Creating Wi-Fi face.");
        mHandler.postDelayed(this, WAIT_TIME);
    }

    @Override
    public void run() {
        String faceUri = Configuration.getNdnNode(mContext);
        if(mBinder.getFaceId(faceUri) == -1) {
            mBinder.createFace(faceUri, 0,false);
            mHandler.postDelayed(this, WAIT_TIME);
        } else {
            Log.i(TAG, "Face " + faceUri + " created");
            mBinder.addRoute("/ndn", mBinder.getFaceId(faceUri), 0L, 0L, 1L);
            Log.i(TAG, "Route for face " + mBinder.getFaceId(faceUri) + " created");
            mHandler.removeCallbacks(this);
        }
    }

}
