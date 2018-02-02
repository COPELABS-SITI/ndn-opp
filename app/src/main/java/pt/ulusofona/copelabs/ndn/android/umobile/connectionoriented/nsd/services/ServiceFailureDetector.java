package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services;


import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.database.FailureData;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class ServiceFailureDetector implements Runnable {

    private static final String TAG = ServiceFailureDetector.class.getSimpleName();
    private static final int TIME_BETWEEN_FAILURES_CHECK = 3000;
    private static final int MAX_FAILURES = 3;
    private FailureData mFailureData = new FailureData();
    private ServiceFailureDetectorListener mListener;
    private Handler mHandler = new Handler();

    public ServiceFailureDetector(ServiceFailureDetectorListener listener) {
        mListener = listener;
    }

    public void start() {
        mHandler.postDelayed(this, TIME_BETWEEN_FAILURES_CHECK);
    }

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

    public void close() {
        mHandler.removeCallbacks(this);
        mFailureData.clear();
    }

}
