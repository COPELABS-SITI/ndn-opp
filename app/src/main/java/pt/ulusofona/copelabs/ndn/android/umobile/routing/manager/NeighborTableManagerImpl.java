/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This interface sets all the methods used by NeighborTableManager.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.ContextualManagerNotConnectedException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Neighbor;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.NeighborTable;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.cache.WifiP2pCache;

public class NeighborTableManagerImpl implements NeighborTableManager, AidlManager.Listener,
         WifiP2pListener.ServiceAvailable, TManager.Listener, Runnable {

    /** This variable is used to debug NeighborTableManagerImpl class */
    private static final String TAG = NeighborTableManagerImpl.class.getSimpleName();

    /** This holds the time between RIB updates */
    private static final int SCHEDULING_TIME = 30 * 1000;

    /** This object is a references the neighbor table */
    private NeighborTable mNeighborTable = new NeighborTable();

    /** This handler is used to schedule RIB updates */
    private Handler mHandler = new Handler();

    /** This object is used to communicate with contextual manager */
    private AidlManager.Manager mAidlManager;

    /** This object is used to fetch Ts */
    private TManager.Manager mTManager = new TManagerImpl();

    /** This variable holds the state of this class */
    private boolean mEnable = false;

    /** This object holds the application context */
    private Context mContext;

    /**
     * This method is the constructor of NeighborTableManagerImpl class
     * @param context Application context
     */
    NeighborTableManagerImpl(Context context) {
        mContext = context;
        mAidlManager = new AidlManagerImpl(context, this);
    }

    /**
     * This method starts the NeighborTableManager
     */
    @Override
    public void start() {
        if(!mEnable) {
            fetchCache();
            mTManager.start();
            mAidlManager.start();
            WifiP2pListenerManager.registerListener(this);
            TManagerImpl.registerListener(this);
            mEnable = true;
            Log.i(TAG, "NeighborTableManagerImpl started");
        }
    }

    /**
     * This method stops the NeighborTableManager
     */
    @Override
    public void stop() {
        if(mEnable) {
            mTManager.stop();
            mAidlManager.stop();
            WifiP2pListenerManager.unregisterListener(this);
            TManagerImpl.unregisterListener(this);
            mNeighborTable.clear();
            mEnable = false;
            Log.i(TAG, "NeighborTableManagerImpl stopped");
        }
    }

    /**
     * This method updates the neighbor table
     */
    @Override
    public void run() {
        if(mAidlManager.isBound()) {
            List<String> cmIdentifiers = mNeighborTable.getAllCmIdentifiers();
            try {
                Map<String, Double> availabilities = mAidlManager.getAvailability(cmIdentifiers);
                Map<String, Double> centralities = mAidlManager.getCentrality(cmIdentifiers);
                Map<String, Double> similarities = mAidlManager.getSimilarity(cmIdentifiers);
                if(availabilities != null && centralities != null && similarities != null) {
                    for(String cmIdentifier : cmIdentifiers) {
                        Neighbor neighbor = mNeighborTable.getNeighbor(cmIdentifier);
                        Log.i(TAG, "Updating " + neighbor.toString());
                        if (availabilities.containsKey(cmIdentifier)) {
                            if(availabilities.get(cmIdentifier) != null) {
                                double availability = availabilities.get(cmIdentifier);
                                Log.i(TAG, "Availability value for: " + neighbor.getUuid() + " is: " + availability);
                                neighbor.setA(availability);
                            }
                        }
                        if (centralities.containsKey(cmIdentifier)) {
                            if(centralities.get(cmIdentifier) != null) {
                                double centrality = centralities.get(cmIdentifier);
                                Log.i(TAG, "Centrality value for: " + neighbor.getUuid() + " is: " + centrality);
                                neighbor.setC(centrality);
                            }
                        }
                        if (similarities.containsKey(cmIdentifier)) {
                            if(similarities.get(cmIdentifier) != null) {
                                double similarity = similarities.get(cmIdentifier);
                                Log.i(TAG, "Similarity value for: " + neighbor.getUuid() + " is: " + similarity);
                                neighbor.setI(similarity);
                            }
                        }
                    }
                }
            } catch (NeighborNotFoundException e) {
                Log.e(TAG, "Neighbor with cmIdentifier: " + e.getMessage() + " not found");
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (ContextualManagerNotConnectedException e) {
                e.printStackTrace();
            }
            mHandler.postDelayed(this, SCHEDULING_TIME);
            Log.i(TAG, "A new update was scheduled in " + SCHEDULING_TIME + " milliseconds");
        }
    }

    /**
     * This method search and returns a Neighbor with a certain uuid
     * @param neighborUuid neighbor's uuid
     * @return Neighbor found
     * @throws NeighborNotFoundException this exception is triggered if the neighbor is not found
     */
    @Override
    public Neighbor getNeighbor(String neighborUuid) throws NeighborNotFoundException {
        return mNeighborTable.getNeighbor(neighborUuid);
    }

    @Override
    public Neighbor getNeighborByFaceUri(String faceUri) throws NeighborNotFoundException {
        return mNeighborTable.getNeighborByFaceUri(faceUri);
    }

    @Override
    public void onServiceAvailable(String instanceName, String registrationType, WifiP2pDevice srcDevice) {
        Log.i(TAG, "Service discovered with instance name: " + instanceName);
        String neighborUuid = instanceName.split("\\.")[0];
        mNeighborTable.addNeighborIfDoesntExist(new Neighbor(srcDevice.deviceAddress, neighborUuid));
    }

    private void fetchCache() {
        HashMap<String, String> devicesInfo = WifiP2pCache.getData(mContext);
        for(Map.Entry entry : devicesInfo.entrySet()) {
            addNeighbor(entry.getKey().toString(), entry.getValue().toString());
        }
    }

    private void addNeighbor(String uuid, String mac) {
        mNeighborTable.addNeighborIfDoesntExist(new Neighbor(mac, uuid));
    }

    /**
     * This method provides T information required by dabber
     * @param sender sender of data
     * @param name packet name
     * @param t computed T
     */
    @Override
    public void onReceiveT(String sender, String name, double t) {
        Log.i(TAG, "Received T :" + t + " from: " + sender + " relative to: " + name);
        try {
            Neighbor neighbor = mNeighborTable.getNeighbor(sender);
            neighbor.setT(name, t);
        } catch (NeighborNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is invoked when the connection with contextual manager is established
     */
    @Override
    public synchronized void onContextualManagerConnected() {
        mHandler.postDelayed(this, SCHEDULING_TIME);
        Log.i(TAG, "Connected to Contextual Manager");
    }

    /**
     * This method is invoked when the connection with contextual manager goes down
     */
    @Override
    public synchronized void onContextualManagerDisconnected() {
        mHandler.postDelayed(this, SCHEDULING_TIME);
        Log.i(TAG, "Disconnected from Contextual Manager");
    }

}
