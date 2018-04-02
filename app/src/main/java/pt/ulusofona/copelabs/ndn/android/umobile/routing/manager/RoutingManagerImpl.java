/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class instantiates and manages all routing components
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import android.content.Context;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks.RibUpdaterImpl;


public class RoutingManagerImpl implements RoutingManager {

    /** This variable is used to debug RoutingManagerImpl class */
    private static final String TAG = RoutingManagerImpl.class.getName();

    /** This object is used to manage the neighbors */
    private NeighborTableManagerImpl mNeighborTableManager;

    /** This object is used to update RIB */
    private RibUpdaterImpl mRibUpdater;

    /** This variable holds the status of this class */
    private boolean mStarted = false;


    /**
     * This method stars the Routing Manager
     * @param binder daemon reference
     * @param context application context
     */
    @Override
    public synchronized void start(OpportunisticDaemon.Binder binder, Context context) {
        if(!mStarted) {
            mNeighborTableManager = new NeighborTableManagerImpl(context);
            mRibUpdater = new RibUpdaterImpl(context, mNeighborTableManager, binder);
            mNeighborTableManager.start();
            mRibUpdater.start();
            mStarted = true;
            Log.i(TAG, "Routing Manager started");
        }
    }

    /**
     * This method stops the Routing Manager
     */
    @Override
    public synchronized void stop() {
        if(mStarted) {
            mNeighborTableManager.stop();
            mRibUpdater.stop();
            mStarted = false;
            Log.i(TAG, "Routing Manager stopped");
        }
    }

}
