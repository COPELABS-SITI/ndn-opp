/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class does the device discovery and also implements
 * a singleton design pattern.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services;


import android.util.Log;

import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications.CommIn;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications.DiscovererListener;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class ServiceDiscoverer implements DiscovererListener.Discoverer {

    /** This variable is used to debug ServiceDiscoverer class */
    private static final String TAG = ServiceDiscoverer.class.getSimpleName();

    /** This list is used to holds the registered listeners */
    private static ArrayList<DiscovererListener> sListeners = new ArrayList<>();

    /** This object is used to implement a singleton design patter */
    private static ServiceDiscoverer sInstance;

    /** This variable is used to control the status of this class */
    private boolean mDiscovering = false;

    /** This object is used to hold host data of this device in order to filter this on received list */
    private NsdInfo mMyNsdInfo;

    /** This object is used to receive data related with nsd */
    private CommIn mCommIn;

    private ServiceDiscoverer() {}

    /**
     * This method is used to return the instance of this class
     * @return ServiceDiscoverer object
     */
    public static ServiceDiscoverer getInstance() {
        if(sInstance == null)
            sInstance = new ServiceDiscoverer();
        return sInstance;
    }

    /**
     * This method is used to register a listener
     * @param listener listener to be registered
     */
    public static void registerListener(DiscovererListener listener) {
        Log.i(TAG, "Registering listener");
        sListeners.add(listener);
    }

    /**
     * This method is used to unregister a listener
     * @param listener listener to be unregistered
     */
    public static void unregisterListener(DiscovererListener listener) {
        Log.i(TAG, "Unregistering listener");
        sListeners.add(listener);
    }

    /**
     * This method starts the discovery
     * @param myInfo my own nsd info
     */
    public synchronized void start(NsdInfo myInfo) {
        if(!mDiscovering) {
            Log.i(TAG, "Starting Service Discoverer");
            mMyNsdInfo = myInfo;
            mCommIn = new CommIn(this, mMyNsdInfo);
            mCommIn.start();
            mDiscovering = true;
            Log.i(TAG, "Service Discoverer started");
        }
    }

    /**
     * This method is invoked when a new device is discovered
     * @param nsdInfo new device info
     */
    @Override
    public void onPeerDetected(NsdInfo nsdInfo) {
        Log.i(TAG, "Device detected " + nsdInfo.toString());
        for(DiscovererListener listener : sListeners) {
            if(listener instanceof PeerDiscoverer) {
                ((PeerDiscoverer) listener).onPeerDetected(nsdInfo);
            }
        }
    }

    /**
     * This method is invoked when a new updated list
     * @param nsdInfo updated list of devices
     */
    @Override
    public void onReceivePeerList(ArrayList<NsdInfo> nsdInfo) {
        Log.i(TAG, "Peer list received");
        nsdInfo.remove(mMyNsdInfo);
        for(DiscovererListener listener : sListeners) {
            if(listener instanceof PeerListDiscoverer) {
                ((PeerListDiscoverer) listener).onReceivePeerList(nsdInfo);
            }
        }
    }

    @Override
    public void onStartDiscoveringSuccess() {
        Log.i(TAG, "Discovering started.");
    }

    @Override
    public void onStartDiscoveringFailed() {
        Log.e(TAG, "Discovering failed.");
    }

    /**
     * This method is used to stop the discovering process
     */
    public synchronized void close() {
        if(mDiscovering) {
            Log.i(TAG, "Closing Service Discoverer");
            mCommIn.close();
            sListeners.clear();
            mDiscovering = false;
            Log.i(TAG, "Service Discoverer closed");
        }
    }

}
