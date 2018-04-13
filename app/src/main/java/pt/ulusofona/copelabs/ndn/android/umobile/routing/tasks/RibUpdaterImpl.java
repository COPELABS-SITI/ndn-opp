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
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.LsdbDao;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.LsdbDaoImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.RoutingEntryDao;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.RoutingEntryDaoImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.manager.NeighborTableManager;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Neighbor;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Plsa;
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

    /** This object is used to communicate with LSDB database */
    private LsdbDao mLsdbDao;

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
        mNeighborTableManager = neighborTableManager;
        mBinder = binder;
        mRoutingEntryDao = new RoutingEntryDaoImpl(context);
        mLsdbDao = new LsdbDaoImpl(context);
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
        Log.i(TAG, "Uuid: " + neighborUuid + " name: " + name + " cost: " + cost);
        if(!name.contains("broadcast")) {
            Log.i(TAG, "Updating routing entry");
            try {
                Neighbor neighbor = mNeighborTableManager.getNeighbor(neighborUuid);
                long faceId = mBinder.getFaceId(neighborUuid);
                if (faceId != -1) {
                    long k1 = CostModels.computeK1(cost, neighbor.getI());
                    double k2 = CostModels.computeK2(k1, neighbor.getC(), neighbor.getA(), neighbor.getT(name));
                    Log.i(TAG, "neighbor, " + neighborUuid + " k1, " + k1 + " k2, " + k2);
                    long computedCost = CostModels.computeCost(k2);
                    Log.i(TAG, "neighbor, " + neighborUuid + " cost, " + computedCost);
                    RoutingEntry entry = new RoutingEntry(name, faceId, computedCost);
                    storeInDatabase(entry);
                    Log.i(TAG, "Routing entry has been updated");
                }
            } catch (NeighborNotFoundException ex) {
                Log.e(TAG, "Neighbor " + neighborUuid + " not found");
            }
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

    private void updateDatabase() {
        Log.i(TAG, "Updating Database");
        List<Plsa> plsas = mLsdbDao.getAllEntries();
        for(Plsa plsa : plsas) {
            if(!mBinder.getUmobileUuid().equalsIgnoreCase(plsa.getNeighbor())) {
                Log.i(TAG, "Neighbor, " + plsa.getNeighbor() + " cost, " + plsa.getCost());
                updateRoutingEntry(plsa.getName(), plsa.getNeighbor(), plsa.getCost());
            }
        }
    }

    private void updateRib() {
        Log.i(TAG, "Updating NDN-OPP RIB");
        List<RoutingEntry> mRoutingTable = mRoutingEntryDao.getAllEntries();
        Collections.sort(mRoutingTable);
        for(RoutingEntry entry : mRoutingTable) {
            Log.i(TAG, "Face, " + entry.getFace() + " name, " + entry.getPrefix() + " cost: " + entry.getCost());
            mBinder.addRoute(entry.getPrefix(), entry.getFace(), entry.getOrigin(), entry.getCost(), entry.getFlag());
        }
    }

    @Override
    public void run() {
        updateDatabase();
        updateRib();
        mHandler.postDelayed(this, SCHEDULING_TIME);
    }

}
