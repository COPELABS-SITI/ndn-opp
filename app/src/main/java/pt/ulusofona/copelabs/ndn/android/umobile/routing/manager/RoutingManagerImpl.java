package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import android.content.Context;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks.RibUpdaterImpl;

/**
 * Created by miguel on 07-03-2018.
 */

public class RoutingManagerImpl implements RoutingManager {

    private static final String TAG = RoutingManagerImpl.class.getName();
    private NeighborTableManagerImpl mNeighborTableManager;
    private RibUpdaterImpl mRibUpdater;
    private boolean mStarted = false;

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
