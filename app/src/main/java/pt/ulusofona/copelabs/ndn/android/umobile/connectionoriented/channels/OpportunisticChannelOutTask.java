package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.channels;


import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketObserver;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;

public class OpportunisticChannelOutTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = OpportunisticChannelOutTask.class.getSimpleName();
    private PacketObserver mObservingContext;
    private String mHost;
    private int mPort;
    private Packet mPacket;

    public OpportunisticChannelOutTask(PacketObserver observingContext, String host, int port, Packet packet) {
        mHost = host;
        mPort = port;
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
            connection.connect(new InetSocketAddress(mHost, mPort));
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
