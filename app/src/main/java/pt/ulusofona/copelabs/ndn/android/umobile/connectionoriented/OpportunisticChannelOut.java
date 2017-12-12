/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * Implementation of the Opportunistic Channel encapsulating the actual communication scheme
 * used to transmit the bytes.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketObserver;

public class OpportunisticChannelOut {
    private static final String TAG = OpportunisticChannelOut.class.getSimpleName();

    private PacketObserver mObservingContext;
    private final String mHost;
    private final int mPort;


    /** Main constructor
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
        new ConnectionTask(mHost, mPort, packet).execute();
    }


    /** ConnectionTask is used to perform a transfer
     */
    private class ConnectionTask extends AsyncTask<Void, Void, Boolean> {
        private String mHost;
        private int mPort;
        private Packet mPacket;

        ConnectionTask(String host, int port, Packet packet) {
            mHost = host;
            mPort = port;
            mPacket = packet;
        }

        /** Performs the actual transfer of the packet.
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
                transferSucceeded = true;
            } catch (IOException e) {
                Log.d(TAG, "Transfer failed.");
                //mOppFaceManager.bringDownFace(mUuid);
                e.printStackTrace();
                transferSucceeded = false;
            }
            return transferSucceeded;
        }

        /** After the task completes, used to callback into the NDN Opportunistic Daemon to notify the
         * requesting Face of the outcome of the transfer.
         * @param transferSucceeded success result of the transfer
         */
        @Override
        protected void onPostExecute(Boolean transferSucceeded) {
            if(transferSucceeded)
                mObservingContext.onPacketTransferred(mPacket.getSender(), mPacket.getId());
        }
    }

}
