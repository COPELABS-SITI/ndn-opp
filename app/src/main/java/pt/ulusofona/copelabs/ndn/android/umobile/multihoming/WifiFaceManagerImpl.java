package pt.ulusofona.copelabs.ndn.android.umobile.multihoming;


import android.content.Context;
import android.os.Handler;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.preferences.Configuration;
import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.wifi.regular.WifiRegularListener;
import pt.ulusofona.copelabs.ndn.android.wifi.regular.WifiRegularListenerManager;

public class WifiFaceManagerImpl implements WifiFaceManager, Runnable, WifiRegularListener {

    private static final String TAG = WifiFaceManagerImpl.class.getSimpleName();
    private static final int WAIT_TIME = 1000;
    private OpportunisticDaemon.Binder mBinder;
    private Handler mHandler = new Handler();
    private Context mContext;
    private boolean mEnabled;

    @Override
    public synchronized void enable(Context context, OpportunisticDaemon.Binder binder) {
        if(!mEnabled) {
            mBinder = binder;
            mContext = context;
            WifiRegularListenerManager.registerListener(this);
            mEnabled = true;
        }
    }

    @Override
    public void disable() {
        WifiRegularListenerManager.unregisterListener(this);
        mEnabled = false;
    }

    @Override
    public void onConnected() {
        Log.i(TAG, "I'm connected to an AP");
        createFaceAndRoute();
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "I'm disconnected");
    }

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
