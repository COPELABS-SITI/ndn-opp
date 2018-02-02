package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services;


import android.util.Log;

import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications.DiscovererListener;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications.CommOut;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class ServiceLeader implements DiscovererListener.PeerDiscoverer, ServiceFailureDetectorListener {

    private static final String TAG = ServiceLeader.class.getSimpleName();
    private ServiceFailureDetector mServiceFailureDetector;
    private ArrayList<NsdInfo> mPeers = new ArrayList<>();
    private CommOut mCommOut;
    private boolean mRunning;


    public synchronized void start() {
        if(!mRunning) {
            Log.i(TAG, "Starting Service Leader");
            ServiceDiscoverer.registerListener(this);
            mCommOut = new CommOut();
            mServiceFailureDetector = new ServiceFailureDetector(this);
            mServiceFailureDetector.start();
            mRunning = true;
            Log.i(TAG, "Service Leader started");
        }
    }

    @Override
    public synchronized void onPeerDetected(NsdInfo nsdInfo) {
        mServiceFailureDetector.deviceDetected(nsdInfo);
        if(!mPeers.contains(nsdInfo)) {
            Log.i(TAG, "New peer detected " + nsdInfo.toString());
            mPeers.add(nsdInfo);
            broadcastUpdatedPeerList();
        }
    }

    @Override
    public synchronized void onPeerLost(NsdInfo lostPeer) {
        Log.i(TAG, "Lost peer " + lostPeer.toString());
        mPeers.remove(lostPeer);
        broadcastUpdatedPeerList();
    }

    private void broadcastUpdatedPeerList() {
        mCommOut.broadcast(mPeers, mPeers);
    }

    public synchronized void close() {
        if(mRunning) {
            Log.i(TAG, "Closing Service Leader");
            ServiceDiscoverer.unregisterListener(this);
            mServiceFailureDetector.close();
            mPeers.clear();
            mRunning = false;
            Log.i(TAG, "Service Leader closed");
        }
    }

}
