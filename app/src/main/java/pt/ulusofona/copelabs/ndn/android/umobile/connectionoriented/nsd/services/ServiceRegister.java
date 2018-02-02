package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services;


import android.os.Handler;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications.CommOut;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.HostInfo;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class ServiceRegister implements Runnable {

    private static final String TAG = ServiceRegister.class.getSimpleName();
    private static final int REGISTERING_TIME_INTERVAL = 3000;
    private static ServiceRegister sInstance;
    private Handler mHandler = new Handler();
    private NsdInfo mMyNsdInfo;
    private HostInfo mLeader;
    private CommOut mCommOut;
    private boolean mRunning;

    private ServiceRegister() {}

    public static ServiceRegister getInstance() {
        if(sInstance == null)
            sInstance = new ServiceRegister();
        return sInstance;
    }

    public synchronized void start(HostInfo leader, NsdInfo nsdInfo) {
        if(!mRunning) {
            Log.i(TAG, "Starting Service Register");
            mMyNsdInfo = nsdInfo;
            mLeader = leader;
            createChannel();
            doRegistration();
            mRunning = true;
            Log.i(TAG, "Service Register started");
        }
    }

    private void createChannel() {
        if(mCommOut == null) {
            Log.i(TAG, "Creating CommOut channel");
            mCommOut = new CommOut();
            Log.i(TAG, "CommOut channel created");
        }
    }

    private void doRegistration() {
        mCommOut.sendData(mLeader, mMyNsdInfo);
        mHandler.postDelayed(this, REGISTERING_TIME_INTERVAL);
    }

    @Override
    public void run() {
        doRegistration();
    }

    public synchronized void close() {
        if(mRunning) {
            Log.i(TAG, "Closing Service Register");
            mHandler.removeCallbacks(this);
            mCommOut = null;
            mMyNsdInfo = null;
            mLeader = null;
            mRunning = false;
            Log.i(TAG, "Service Register closed");
        }
    }
}
