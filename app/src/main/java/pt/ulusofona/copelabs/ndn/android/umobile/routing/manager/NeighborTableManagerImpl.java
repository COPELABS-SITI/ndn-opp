package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.util.Log;

import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Neighbor;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.NeighborTable;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;

/**
 * Created by miguel on 07-03-2018.
 */

public class NeighborTableManagerImpl implements NeighborTableManager, AidlManager.Listener,
         WifiP2pListener.ServiceAvailable, Runnable {

    private static final String TAG = NeighborTableManagerImpl.class.getSimpleName();
    private static final int SCHEDULING_TIME = 30 * 1000;
    private NeighborTable mNeighborTable = new NeighborTable();
    private Handler mHandler = new Handler();
    private AidlManager.Manager mAidlManager;
    private boolean mIsConnected = false;

    NeighborTableManagerImpl(Context context) {
        mAidlManager = new AidlManagerImpl(context, this);
    }

    @Override
    public void start() {
        mAidlManager.start();
        WifiP2pListenerManager.registerListener(this);
        Log.i(TAG, "NeighborTableManagerImpl started");
    }

    @Override
    public void stop() {
        mAidlManager.stop();
        WifiP2pListenerManager.unregisterListener(this);
        mNeighborTable.clear();
        Log.i(TAG, "NeighborTableManagerImpl stopped");
    }

    @Override
    public void run() {
        if(mIsConnected) {
            List<Neighbor> neighbors = mNeighborTable.getNeighbors();
            for(Neighbor neighbor : neighbors) {
                // TODO update A, C, I
            }
            mHandler.postDelayed(this, SCHEDULING_TIME);
        }
    }

    @Override
    public synchronized void onContextualManagerConnected() {
        mIsConnected = true;
        mHandler.postDelayed(this, SCHEDULING_TIME);
        Log.i(TAG, "Connected to Contextual Manager");
    }

    @Override
    public synchronized void onContextualManagerDisconnected() {
        mIsConnected = false;
        mHandler.postDelayed(this, SCHEDULING_TIME);
        Log.i(TAG, "Disconnected from Contextual Manager");
    }

    @Override
    public Neighbor getNeighbor(String neighborUuid) throws NeighborNotFoundException {
        return mNeighborTable.getNeighbor(neighborUuid);
    }

    @Override
    public void onServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
        String neighborUuid = instanceName.split("\\.")[0];
        mNeighborTable.addNeighborIfDoesntExist(new Neighbor(srcDevice.deviceAddress, neighborUuid));
    }
}
