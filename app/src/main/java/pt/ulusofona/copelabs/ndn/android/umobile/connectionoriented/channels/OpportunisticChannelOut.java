/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * Implementation of the Opportunistic CommOut encapsulating the actual communication scheme
 * used to transmit the bytes.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.channels;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketObserver;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;

public class OpportunisticChannelOut {

    private static final String TAG = OpportunisticChannelOut.class.getSimpleName();
    private PacketObserver mObservingContext;
    private final String mHost;
    private final int mPort;

    /**
     * Main constructor
     *
     * @param host IP address of the corresponding device
     * @param port Port number of the corresponding device
     */
    public OpportunisticChannelOut(Context context, String host, int port) {
        Log.d(TAG, "Creating OpportunisticChannelOut for " + host + ":" + port);
        mObservingContext = (PacketObserver) context;
        mHost = host;
        mPort = port;
    }

    public void sendPacket(Packet packet) {
        Log.d(TAG, "Attempting to send " + packet.getPayloadSize() + " bytes through UUID " + packet.getSender() + " to " + mHost + ":" + mPort);
        OpportunisticChannelOutTask task = new OpportunisticChannelOutTask(mObservingContext, mHost, mPort, packet);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
