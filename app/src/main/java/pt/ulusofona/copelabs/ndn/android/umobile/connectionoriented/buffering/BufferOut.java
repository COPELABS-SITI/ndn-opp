package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.buffering;


import android.os.Handler;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketObserver;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;

public class BufferOut implements Runnable {

    private static final String TAG = BufferOut.class.getSimpleName();
    private static final int INTERVAL_BETWEEN_POP = 250;
    private Handler mHandler = new Handler();
    private PacketObserver mNotifier;

    public BufferOut(PacketObserver notifier) {
        mNotifier = notifier;
        mHandler.post(this);
    }

    @Override
    public void run() {
        Packet packet = BufferData.pop();
        if(packet != null) {
            Log.e(TAG, "Buffering out one request");
            mNotifier.onPacketReceived(packet.getSender(), packet.getPayload());
        }
        mHandler.postDelayed(this, INTERVAL_BETWEEN_POP);
    }

    public void close() {
        mHandler.removeCallbacks(this);
        BufferData.clear();
    }

}
