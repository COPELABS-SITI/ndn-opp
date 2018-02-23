/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class does the registration of itself and also
 * sends heartbeats to inform the leader that it remains alive.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services;


import android.os.Handler;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications.CommOut;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.HostInfo;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class ServiceRegister implements Runnable {

    /** This variable is used to debug ServiceRegister class */
    private static final String TAG = ServiceRegister.class.getSimpleName();

    /** This variable is used to set the heartbeat interval */
    private static final int REGISTERING_TIME_INTERVAL = 3000;

    /** This object is used in order to implement a singleton design pattern */
    private static ServiceRegister sInstance;

    /** This variable is used to schedule a new heartbeat */
    private Handler mHandler = new Handler();

    /** This object holds the information related with this device */
    private NsdInfo mMyNsdInfo;

    /** This object holds the information related with the elected GO */
    private HostInfo mLeader;

    /** This object is used to send NSD data to GO */
    private CommOut mCommOut;

    /** This variable holds the status of this class, if is running or not */
    private boolean mRunning;

    private ServiceRegister() {}

    /**
     * This method returns an instance of ServiceRegister using singleton design pattern
     * @return ServiceRegister
     */
    public static ServiceRegister getInstance() {
        if(sInstance == null)
            sInstance = new ServiceRegister();
        return sInstance;
    }

    /**
     * This method starts the register
     * @param leader leader host info
     * @param nsdInfo this device host info
     */
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

    /**
     * This method instantiates the channel used to communicate with the GO
     */
    private void createChannel() {
        if(mCommOut == null) {
            Log.i(TAG, "Creating CommOut channel");
            mCommOut = new CommOut();
            Log.i(TAG, "CommOut channel created");
        }
    }

    /**
     * This method does sends a registration request to the leader
     */
    private void doRegistration() {
        mCommOut.sendData(mLeader, mMyNsdInfo);
        mHandler.postDelayed(this, REGISTERING_TIME_INTERVAL);
    }

    @Override
    public void run() {
        doRegistration();
    }

    /**
     * This method stops the register
     */
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
