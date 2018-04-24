/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class does the detection of disconnected devices.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services;


import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.database.FailureData;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class ServiceFailureDetector implements Runnable {

    /** This variable is used to debug ServiceFailureDetector class */
    private static final String TAG = ServiceFailureDetector.class.getSimpleName();

    /** This variable holds the value between disconnected verifications. */
    private static final int TIME_BETWEEN_FAILURES_CHECK = 4000;

    /** This variable holds how many times a device could be verified without be considered disconnected */
    private static final int MAX_FAILURES = 5;

    /** This object stores all data related with all devices */
    private FailureData mFailureData = new FailureData();

    /** This interface is used to notify every time that a device is considered disconnected */
    private ServiceFailureDetectorListener mListener;

    /** This object is used to schedule a new verification */
    private Handler mHandler = new Handler();

    public ServiceFailureDetector(ServiceFailureDetectorListener listener) {
        mListener = listener;
    }

    /**
     * This method is used to start the this detector
     */
    public void start() {
        mHandler.postDelayed(this, TIME_BETWEEN_FAILURES_CHECK);
    }


    /**
     * This method is invoked in order to update device's database
     * @param nsdInfo detected device
     */
    public void deviceDetected(NsdInfo nsdInfo) {
        if(!mFailureData.contains(nsdInfo)) {
            Log.i(TAG, "Adding " + nsdInfo.toString());
            mFailureData.add(nsdInfo);
        } else {
            Log.i(TAG, "Renewing " + nsdInfo.toString());
            mFailureData.reset(nsdInfo);
        }
    }

    @Override
    public void run() {
        Log.i(TAG, "Detecting missing peers");
        ArrayList<NsdInfo> peers = mFailureData.getPeers();
        for(NsdInfo peer : peers) {
            if(mFailureData.getFailures(peer) == MAX_FAILURES) {
                Log.i(TAG, peer.toString() + " is missing");
                mListener.onPeerLost(peer);
                mFailureData.remove(peer);
            }
            mFailureData.failed(peer);
        }
        mHandler.postDelayed(this, TIME_BETWEEN_FAILURES_CHECK);
    }

    /**
     * This method stops this detector and clears all data
     */
    public void close() {
        mHandler.removeCallbacks(this);
        mFailureData.clear();
    }

}
