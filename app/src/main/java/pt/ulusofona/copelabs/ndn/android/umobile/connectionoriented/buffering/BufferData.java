/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-08-31
 * This class stores the buffer data until to be sent
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.buffering;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;

abstract class BufferData {

    /** This variable is used to debug BufferData class */
    private static final String TAG = BufferData.class.getSimpleName();

    /** This variable is used to set the buffer size */
    private static final int SEQUENCER_BUFFER_SIZE = 5000;

    /** This object is used to store buffer's data */
    private static ArrayBlockingQueue<Packet> mData = new ArrayBlockingQueue<>(SEQUENCER_BUFFER_SIZE);

    /**
     * This method inserts data into the buffer
     * @param packet packet to be inserted
     */
    static void push(Packet packet) {
        try {
            Log.i(TAG, "Inserting " + packet.toString());
            mData.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method clears the buffer's data
     */
    static void clear() {
        mData.clear();
    }

    /**
     * This method retrieves the next packet from the buffer
     * @return next packet stored
     */
    static Packet pop() {
        Log.i(TAG, "There is " + mData.size() + " packets remaining");
        return mData.poll();
    }

}
