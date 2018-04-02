package pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.Collections;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.RoutingEntryDao;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.RoutingEntryDaoImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.manager.NeighborTableManager;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Neighbor;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.RoutingEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.utilities.CostModels;

/**
 * Created by miguel on 07-03-2018.
 */

public class RibUpdaterImpl implements Runnable, RibUpdater {

    private static final String TAG = RibUpdaterImpl.class.getSimpleName();
    private static final int SCHEDULING_TIME = 60 * 1000;
    //private List<RoutingEntry> mRoutingTable = new ArrayList<>();
    private NeighborTableManager mNeighborTableManager;
    private OpportunisticDaemon.Binder mBinder;
    private RoutingEntryDao mRoutingEntryDao;
    private Handler mHandler = new Handler();

    public RibUpdaterImpl(Context context, NeighborTableManager neighborTableManager, OpportunisticDaemon.Binder binder) {
        mRoutingEntryDao = new RoutingEntryDaoImpl(context);
        //mRoutingTable = mRoutingEntryDao.getAllEntries();
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
    public void updateRoutingTable(String name, String neighborUuid, long cost) {
        Log.i(TAG, "Updating RIB");
        try {
            Neighbor neighbor = mNeighborTableManager.getNeighbor(neighborUuid);
            // guardar a face do neighbor com este uuid recebido em argumento
            long k1 = CostModels.computeK1(cost, neighbor.getI());
            long k2 = CostModels.computeK2(k1, neighbor.getC(), neighbor.getA(), neighbor.getT());


            // insert on routing table?

            //Collections.sort(mRoutingTable);

            updateRib();
            Log.i(TAG, "RIB updated");
        } catch (NeighborNotFoundException ex) {
            Log.e(TAG, "Neighbor " + neighborUuid + " not found");
        }

    }

    private void storeInDatabase(RoutingEntry routingEntry) {
        if(mRoutingEntryDao.isRoutingEntryExists(routingEntry)) {
            mRoutingEntryDao.updateRoutingEntry(routingEntry);
        } else {
            mRoutingEntryDao.createRoutingEntry(routingEntry);
        }
    }

    private void updateRib() {
        // TODO update rib on NFD
        List<RoutingEntry> mRoutingTable = mRoutingEntryDao.getAllEntries();
        Collections.sort(mRoutingTable);
        // insert 5 best routes in rib
        // mBinder.addRoute
        // mBinder.removeRoute
    }

    @Override
    public void run() {

        mHandler.postDelayed(this, SCHEDULING_TIME);
    }

}
