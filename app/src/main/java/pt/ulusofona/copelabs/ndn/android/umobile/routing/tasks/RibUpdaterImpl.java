package pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.manager.NeighborTableManagerImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.RoutingEntry;

/**
 * Created by miguel on 07-03-2018.
 */

public class RibUpdaterImpl implements Runnable, RibUpdater {

    private static final String TAG = RibUpdaterImpl.class.getSimpleName();
    private static final int SCHEDULING_TIME = 60 * 1000;
    private List<RoutingEntry> mRoutingTable = new ArrayList<>();
    private NeighborTableManagerImpl mNeighborTableManager;
    private OpportunisticDaemon.Binder mBinder;
    private Handler mHandler = new Handler();

    public RibUpdaterImpl(Context context, NeighborTableManagerImpl neighborTableManager, OpportunisticDaemon.Binder binder) {
        mNeighborTableManager = neighborTableManager;
        mBinder = binder;
    }

    @Override
    public void start() {
        mHandler.postDelayed(this, SCHEDULING_TIME);
        Log.i(TAG, "RibUpdater started");
    }

    @Override
    public void stop() {
        mHandler.removeCallbacks(this);
        mNeighborTableManager.stop();
        Log.i(TAG, "RibUpdater stopped");
    }

    @Override
    public void updateRoutingTable(String name, long cost) {
        //Log.i(TAG, "Updating RIB");
        // compute cost 1
        // compute cost 2
        // update rib
        // mBinder.addRoute
        // mBinder.removeRoute
        //Log.i(TAG, "RIB updated");
    }

    private void updateRouteEntry() {

    }

    private void updateRib() {

    }

    @Override
    public void run() {

        mHandler.postDelayed(this, SCHEDULING_TIME);
    }



}
