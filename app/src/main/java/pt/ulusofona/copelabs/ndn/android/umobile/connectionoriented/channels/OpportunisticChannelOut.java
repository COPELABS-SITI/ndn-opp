/**
 * @version 1.1
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * Implementation of the Opportunistic CommOut encapsulating the actual communication scheme
 * used to transmit the bytes.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.channels;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketObserver;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.HostInfo;

public class OpportunisticChannelOut {

    /** This variable is used to debug OpportunisticChannelOut class */
    private static final String TAG = OpportunisticChannelOut.class.getSimpleName();

    /** This interface is used to notify the transfer status */
    private PacketObserver mObservingContext;

    /** This object is used to hold the host info. IP Address and Port */
    private HostInfo mHost;

    /**
     * Main constructor
     * @param context application context
     * @param host IP address of the corresponding device
     * @param port Port number of the corresponding device
     */
    public OpportunisticChannelOut(Context context, String host, int port) {
        Log.d(TAG, "Creating OpportunisticChannelOut for " + host + ":" + port);
        mObservingContext = (PacketObserver) context;
        mHost = new HostInfo(host, port);
    }

    public void sendPacket(Packet packet) {
        Log.d(TAG, "Attempting to send " + packet.getPayloadSize() + " bytes through UUID " + packet.getSender() + " to " + mHost.toString());
        OpportunisticChannelOutTask task = new OpportunisticChannelOutTask(mObservingContext, mHost, packet);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
