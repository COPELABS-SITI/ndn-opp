package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.services;


import android.util.Log;

import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications.CommIn;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications.DiscovererListener;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class ServiceDiscoverer implements DiscovererListener.Discoverer {

    private static final String TAG = ServiceDiscoverer.class.getSimpleName();
    private static ArrayList<DiscovererListener> sListeners = new ArrayList<>();
    private static ServiceDiscoverer sInstance;
    private boolean mDiscovering = false;
    private NsdInfo mMyNsdInfo;
    private CommIn mCommIn;

    private ServiceDiscoverer() {}

    public static ServiceDiscoverer getInstance() {
        if(sInstance == null)
            sInstance = new ServiceDiscoverer();
        return sInstance;
    }

    public static void registerListener(DiscovererListener listener) {
        Log.i(TAG, "Registering listener");
        sListeners.add(listener);
    }

    public static void unregisterListener(DiscovererListener listener) {
        Log.i(TAG, "Unregistering listener");
        sListeners.add(listener);
    }

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

    @Override
    public void onPeerDetected(NsdInfo nsdInfo) {
        Log.i(TAG, "Device detected " + nsdInfo.toString());
        for(DiscovererListener listener : sListeners) {
            if(listener instanceof PeerDiscoverer) {
                ((PeerDiscoverer) listener).onPeerDetected(nsdInfo);
            }
        }
    }

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
