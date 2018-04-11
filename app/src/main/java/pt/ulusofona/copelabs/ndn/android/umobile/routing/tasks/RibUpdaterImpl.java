/**
 * @version 1.1
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class instantiates and manages all components
 * required to keep RIB updated.
 * @author Miguel Tavares (COPELABS/ULHT)
 */


package pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
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



public class RibUpdaterImpl implements Runnable, RibUpdater {

    /** This variable is used to debug RibUpdaterImpl class */
    private static final String TAG = RibUpdaterImpl.class.getSimpleName();

    /** This variable is used to set the time between RIB updates */
    private static final int SCHEDULING_TIME = 60 * 1000;

    /** This object references an instance of NeighborTableManager */
    private NeighborTableManager mNeighborTableManager;

    /** This object references an instance of OpportunisticDaemon */
    private OpportunisticDaemon.Binder mBinder;

    /** This object is used to communicate with Routing database */
    private RoutingEntryDao mRoutingEntryDao;

    /** This object is used to schedule RIB updates */
    private Handler mHandler = new Handler();

    /** This variable holds the state of this class */
    private boolean mStarted;

    /**
     * This method is the constructor of RibUpdaterImpl class
     * @param context Application context
     * @param neighborTableManager NeighborTableManager reference
     * @param binder OpportunisticDaemon reference
     */
    public RibUpdaterImpl(Context context, NeighborTableManager neighborTableManager, OpportunisticDaemon.Binder binder) {
        mRoutingEntryDao = new RoutingEntryDaoImpl(context);
        mNeighborTableManager = neighborTableManager;
        mBinder = binder;
    }

    /**
     * This method starts the RibUpdater
     */
    @Override
    public void start() {
        if(!mStarted) {
            mHandler.postDelayed(this, SCHEDULING_TIME);
            Log.i(TAG, "RibUpdater started");
            mStarted = true;
        }
    }

    /**
     * This method stops the RibUpdater
     */
    @Override
    public void stop() {
        if(mStarted) {
            mHandler.removeCallbacks(this);
            mNeighborTableManager.stop();
            Log.i(TAG, "RibUpdater stopped");
            mStarted = false;
        }
    }

    /**
     * This method updates the nested routing table
     * @param name name prefix
     * @param neighborUuid neighbor uuid
     * @param cost cost
     */
    @Override
    public void updateRoutingEntry(String name, String neighborUuid, long cost) {
        Log.i(TAG, "Updating routing entry");
        Log.i(TAG, "Uuid: " + neighborUuid + " name: " + name + " cost: " + cost);
        try {
            Neighbor neighbor = mNeighborTableManager.getNeighbor(neighborUuid);
            long faceId = mBinder.getFaceId(neighborUuid);
            long k1 = CostModels.computeK1(cost, neighbor.getI());
            long k2 = CostModels.computeK2(k1, neighbor.getC(), neighbor.getA(), neighbor.getT(name));
            RoutingEntry entry = mRoutingEntryDao.getRoutingEntry(name, faceId);
            entry.setCost(k2);
            storeInDatabase(entry);
            Log.i(TAG, "Routing entry has been updated");
        } catch (NeighborNotFoundException ex) {
            Log.e(TAG, "Neighbor " + neighborUuid + " not found");
        }

    }

    /**
     * This method stores routing entries in database
     * @param routingEntry RoutingEntry to be stored
     */
    private void storeInDatabase(RoutingEntry routingEntry) throws NeighborNotFoundException {
        if(mRoutingEntryDao.isRoutingEntryExists(routingEntry)) {
            mRoutingEntryDao.updateRoutingEntry(routingEntry);
        } else {
            mRoutingEntryDao.createRoutingEntry(routingEntry);
        }
    }

    private void updateRib() {
        List<RoutingEntry> mRoutingTable = mRoutingEntryDao.getAllEntries();
        Collections.sort(mRoutingTable);
        for(RoutingEntry entry : mRoutingTable) {
            mBinder.addRoute(entry.getPrefix(), entry.getFace(), entry.getOrigin(), entry.getCost(), entry.getFlag());
        }
        // insert 5 best routes in rib
        // mBinder.addRoute
        // mBinder.removeRoute
    }

    @Override
    public void run() {
        updateRib();
        mHandler.postDelayed(this, SCHEDULING_TIME);
    }

}