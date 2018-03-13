package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import android.content.Context;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.ContextualManagerNotConnectedException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Neighbor;

/**
 * Created by miguel on 07-03-2018.
 */

public class NeighborTableManagerImpl implements NeighborTableManager, AidlManager.Listener, Runnable {

    private static final String TAG = NeighborTableManagerImpl.class.getSimpleName();
    private static final int SCHEDULING_TIME = 30 * 1000;
    private ArrayList<Neighbor> mNeighborTable = new ArrayList<>();
    private Handler mHandler = new Handler();
    private AidlManager.Manager mAidlManager;
    private boolean mIsConnected = false;

    NeighborTableManagerImpl(Context context) {
        mAidlManager = new AidlManagerImpl(context, this);
    }

    @Override
    public void start() {
        mAidlManager.start();
        mHandler.postDelayed(this, SCHEDULING_TIME);
        Log.i(TAG, "NeighborTableManagerImpl started");
    }

    @Override
    public void stop() {
        mAidlManager.stop();
        mHandler.removeCallbacks(this);
        Log.i(TAG, "NeighborTableManagerImpl stopped");
    }

    @Override
    public void run() {

    }

    @Override
    public synchronized void onContextualManagerConnected() {
        mIsConnected = true;
        Log.i(TAG, "Connected to Contextual Manager");
    }

    @Override
    public synchronized void onContextualManagerDisconnected() {
        mIsConnected = false;
        Log.i(TAG, "Disconnected from Contextual Manager");
    }

}
