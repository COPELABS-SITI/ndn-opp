/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class instantiates and manages all routing components
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks.RibUpdaterImpl;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;


public class RoutingManagerImpl implements RoutingManager, WifiP2pListener.WifiP2pConnectionStatus {

    /** This variable is used to debug RoutingManagerImpl class */
    private static final String TAG = RoutingManagerImpl.class.getSimpleName();

    /** This object is used to manage the neighbors */
    private NeighborTableManagerImpl mNeighborTableManager;

    /** This object holds a reference to NDN Opportunistic Daemon */
    private OpportunisticDaemon.Binder mBinder;

    /** This object is used to update RIB */
    private RibUpdaterImpl mRibUpdater;

    /** This variable holds the registration of Wi-Fi connection status */
    private boolean mRegistered = false;

    /** This variable holds the routing status, if is running or not */
    private boolean mStarted = false;

    /** This variable holds the application context */
    private Context mContext;


    /**
     * This method is the constructor of this class
     * @param binder daemon reference
     * @param context application context
     */
    public RoutingManagerImpl(OpportunisticDaemon.Binder binder, Context context) {
        mBinder = binder;
        mContext = context;
    }

    /**
     * This method starts the Routing Manager
     */
    @Override
    public synchronized void start() {
        if(!mRegistered) {
            Log.i(TAG, "Routing manager is registering to listen Wi-Fi P2P connections");
            WifiP2pListenerManager.registerListener(this);
            mRegistered = true;
            Log.i(TAG, "Routing Manager is registered to listen Wi-Fi P2P connections");
        }
    }

    /**
     * This method stops the Routing Manager
     */
    @Override
    public synchronized void stop() {
        if(mRegistered) {
            Log.i(TAG, "Routing manager is unregistering to listen Wi-Fi P2P connections");
            WifiP2pListenerManager.unregisterListener(this);
            stopRouting();
            mRegistered = false;
            Log.i(TAG, "Routing Manager is unregistered to listen Wi-Fi P2P connections");
        }
    }

    /**
     * This method is invoked when a Wi-Fi P2P connection is detected
     * @param intent intent
     */
    @Override
    public synchronized void onConnected(Intent intent) {
        Log.i(TAG, "Wi-Fi P2P connection detected");
        if(mRegistered && !mStarted) {
            mNeighborTableManager = new NeighborTableManagerImpl(mContext);
            mRibUpdater = new RibUpdaterImpl(mContext, mNeighborTableManager, mBinder);
            mNeighborTableManager.start();mRibUpdater.start();
            mStarted = true;
            Log.i(TAG, "Routing Manager started");
        }
    }

    /**
     * This method is invoked when a Wi-Fi P2P disconnection is detected
     * @param intent intent
     */
    @Override
    public void onDisconnected(Intent intent) {
        Log.i(TAG, "Wi-Fi P2P disconnection detected");
        stopRouting();
    }

    /**
     * This method stops the routing mechanism
     */
    private void stopRouting() {
        if(mRegistered && mStarted) {
            mNeighborTableManager.stop();
            mRibUpdater.stop();
            mStarted = false;
            Log.i(TAG, "Routing Manager stopped");
        }
    }

}
