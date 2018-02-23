/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class is instantiated by the GO device in order to manage
 * the status of other devices.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services;


import android.util.Log;

import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications.CommOut;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications.DiscovererListener;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class ServiceLeader implements DiscovererListener.PeerDiscoverer, ServiceFailureDetectorListener {

    /** This variable is used to debug ServiceLeader class */
    private static final String TAG = ServiceLeader.class.getSimpleName();

    /** This object is used to detect disconnected devices */
    private ServiceFailureDetector mServiceFailureDetector;

    /** This list holds all available connected devices */
    private ArrayList<NsdInfo> mPeers = new ArrayList<>();

    /** This object is used to send NSD data to connected devices */
    private CommOut mCommOut;

    /** This variable holds the state of this class, if is running or not */
    private boolean mRunning;


    /**
     * This method starts all leader features
     */
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

    /**
     * This method is invoked when a device is detected. It notifies
     * the disconnect detector and if is also new device it adds to the list.
     * @param nsdInfo detected device
     */
    @Override
    public synchronized void onPeerDetected(NsdInfo nsdInfo) {
        mServiceFailureDetector.deviceDetected(nsdInfo);
        if(!mPeers.contains(nsdInfo)) {
            Log.i(TAG, "New peer detected " + nsdInfo.toString());
            mPeers.add(nsdInfo);
            broadcastUpdatedPeerList();
        }
    }


    /**
     * This method is invoked when a device is considered detected.
     * In first phase that device is removed from the connected devices
     * and then that list is sent to all devices
     * @param lostPeer disconnected device
     */
    @Override
    public synchronized void onPeerLost(NsdInfo lostPeer) {
        Log.i(TAG, "Lost peer " + lostPeer.toString());
        mPeers.remove(lostPeer);
        broadcastUpdatedPeerList();
    }


    /**
     * This method sends an updated list of connected devices
     */
    private void broadcastUpdatedPeerList() {
        mCommOut.broadcast(mPeers, mPeers);
    }


    /**
     * This method stops all leader features
     */
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
