/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class removes packet from the buffer and deliver them to NDN-OPP
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.buffering;


import android.os.Handler;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketObserver;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;

public class BufferOut implements Runnable {

    /** This variable is used to debug BufferOut class */
    private static final String TAG = BufferOut.class.getSimpleName();

    /** This variable holds the interval of time between pops */
    private static final int INTERVAL_BETWEEN_POP = 250;

    /** This object is used to schedule a new pop from the buffer */
    private Handler mHandler = new Handler();

    /** This interface is used to deliver the packets */
    private PacketObserver mNotifier;


    public BufferOut(PacketObserver notifier) {
        mNotifier = notifier;
        mHandler.post(this);
    }

    @Override
    public void run() {
        Packet packet = BufferData.pop();
        if(packet != null) {
            Log.i(TAG, "Buffering out one request");
            mNotifier.onPacketReceived(packet.getSender(), packet.getPayload());
        }
        mHandler.postDelayed(this, INTERVAL_BETWEEN_POP);
    }

    /**
     * This method is used to stop the buffering out
     */
    public void close() {
        mHandler.removeCallbacks(this);
        BufferData.clear();
    }

}
