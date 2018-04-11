/**
 * @version 1.1
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-04-02
 * This class instantiates and manages all components
 * required to compute the T values.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketManager;
import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketManagerImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.utilities.Utilities;


public class TManagerImpl implements TManager.Manager, PacketManager.Listener {

    /** This variable is used to debug TManagerImpl class */
    private static final String TAG = TManagerImpl.class.getSimpleName();

    /** These listeners are used to notify computed T values */
    private static List<TManager.Listener> sListeners = new ArrayList<>();

    /** This object is used to compute T values of each name and neighbor */
    private ConcurrentHashMap<String, Long> mInterestsSent = new ConcurrentHashMap<>();

    /** This variable holds the state of this class */
    private boolean mStarted;

    /**
     * This method registers a listener
     * @param listener listener to be registered
     */
    public static void registerListener(TManager.Listener listener) {
        sListeners.add(listener);
    }

    /**
     * This method unregister a listener
     * @param listener listener to be unregistered
     */
    public static void unregisterListener(TManager.Listener listener) {
        sListeners.remove(listener);
    }

    /**
     * This method stars the TManager
     */
    @Override
    public synchronized void start() {
        if(!mStarted) {
            PacketManagerImpl.registerListener(this);
            mStarted = true;
            Log.i(TAG, "TManager has been started");
        }
    }

    /**
     * This method stops the TManager
     */
    @Override
    public synchronized void stop() {
        if(mStarted) {
            PacketManagerImpl.unregisterListener(this);
            sListeners.clear();
            mStarted = false;
            Log.i(TAG, "TManager has been stopped");
        }
    }

    /**
     * This method is invoked when an interest packet is transferred
     * @param sender packet sender uuid
     * @param name packet name
     */
    @Override
    public void onInterestTransferred(String sender, String name) {
        String id = sender + name;
        Log.i(TAG, "Adding " + id);
        if(!mInterestsSent.contains(id))
            mInterestsSent.put(id, Utilities.getTimestampInSeconds());
    }

    /**
     * This method is invoked when a data packet is received
     * @param sender packet sender uuid
     * @param name packet name
     */
    @Override
    public void onDataReceived(String sender, String name) {
        String id = sender + name;
        if(mInterestsSent.get(id) != null) {
            long currentTime = Utilities.getTimestampInSeconds();
            long t = (currentTime - mInterestsSent.remove(id));
            Log.i(TAG, "For " + name + " its T is " + t + " seconds");
            notify(sender, name, t);
        }
    }

    /**
     * This method notifies all subscribed listeners with sender, packet name and its t
     * @param sender sender uuid
     * @param name packet name
     * @param t computed t value
     */
    @Override
    public void notify(String sender, String name, double t) {
        for(TManager.Listener listener : sListeners) {
            listener.onReceiveT(sender, name, t);
        }
    }

}
