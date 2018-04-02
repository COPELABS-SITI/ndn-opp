package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import android.os.Handler;
import android.util.Log;

import java.util.concurrent.ConcurrentHashMap;

import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketManager;
import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketManagerImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.utilities.Utilities;

/**
 * Created by miguel on 28-03-2018.
 */

public class TManagerImpl implements TManager.Manager, PacketManager.Listener {

    private static final String TAG = TManagerImpl.class.getSimpleName();
    private ConcurrentHashMap<String, Long> mInterestsSent = new ConcurrentHashMap<>();
    private TManager.Listener mListener;

    public TManagerImpl(TManager.Listener listener) {
        mListener = listener;
    }

    @Override
    public void start() {
        PacketManagerImpl.registerListener(this);
    }

    @Override
    public void stop() {
        PacketManagerImpl.unregisterListener(this);
    }

    @Override
    public void onInterestTransferred(String sender, String name) {
        Log.i(TAG, "Adding " + name);
        if(!mInterestsSent.contains(name))
            mInterestsSent.put(name, Utilities.getTimestampInSeconds());
    }

    @Override
    public void onDataReceived(String sender, String name) {
        if(mInterestsSent.get(name) != null) {
            long currentTime = Utilities.getTimestampInSeconds();
            long t = currentTime - mInterestsSent.get(name);
            Log.i(TAG, "For " + name + " its T is " + t + " seconds");
            mListener.onReceiveT(sender, name, t);
        }
    }

}
