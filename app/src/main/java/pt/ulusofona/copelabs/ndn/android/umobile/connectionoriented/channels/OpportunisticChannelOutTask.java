/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class is used to receive NDN packets from other devices.
 * It creates a new socket to allow multithreading
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.channels;


import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketObserver;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.HostInfo;

public class OpportunisticChannelOutTask extends AsyncTask<Void, Void, Boolean> {

    /** This variable is used to debug OpportunisticChannelOutTask class */
    private static final String TAG = OpportunisticChannelOutTask.class.getSimpleName();

    /** This interface is used to notify the transfer status */
    private PacketObserver mObservingContext;

    /** This object is used to hold host info. IP Address and Port */
    private HostInfo mHost;

    /** This object is used to encapsulate a packet */
    private Packet mPacket;

    OpportunisticChannelOutTask(PacketObserver observingContext, HostInfo host, Packet packet) {
        mHost = host;
        mPacket = packet;
        mObservingContext = observingContext;
    }

    /**
     * Performs the actual transfer of the packet.
     *
     * @param voids no parameters
     * @return boolean value reflecting whether the transfer is a success or not
     */
    @Override
    protected Boolean doInBackground(Void... voids) {
        boolean transferSucceeded;
        try {
            Socket connection = new Socket();
            connection.connect(new InetSocketAddress(mHost.getIpAddress(), mHost.getPort()));
            Log.d(TAG, "Connection established to " + connection.toString() + " ? " + connection.isConnected());
            ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
            oos.writeObject(mPacket);
            oos.flush();
            oos.close();
            connection.close();
            transferSucceeded = true;
        } catch (IOException e) {
            Log.d(TAG, "Transfer failed.");
            e.printStackTrace();
            transferSucceeded = false;
        }
        return transferSucceeded;
    }

    /**
     * After the task completes, used to callback into the NDN Opportunistic Daemon to notify the
     * requesting Face of the outcome of the transfer.
     *
     * @param transferSucceeded success result of the transfer
     */
    @Override
    protected void onPostExecute(Boolean transferSucceeded) {
        if (transferSucceeded)
            mObservingContext.onPacketTransferred(mPacket.getSender(), mPacket.getId());
    }

}
