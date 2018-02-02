package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.buffering;

import android.util.Log;

import java.util.concurrent.ArrayBlockingQueue;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;

abstract class BufferData {

    private static final String TAG = BufferData.class.getSimpleName();
    private static final int SEQUENCER_BUFFER_SIZE = 5000;
    private static ArrayBlockingQueue<Packet> mData = new ArrayBlockingQueue<>(SEQUENCER_BUFFER_SIZE);

    static void push(Packet packet) {
        try {
            Log.i(TAG, "Inserting " + packet.toString());
            mData.put(packet);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void clear() {
        mData.clear();
    }

    static Packet pop() {
        return mData.poll();
    }

}
