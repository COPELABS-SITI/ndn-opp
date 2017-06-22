/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * Implementation of the Opportunistic Channel encapsulating the actual communication scheme
 * used to transmit the bytes.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile;

import android.os.AsyncTask;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

class OpportunisticChannel {
    private static final String TAG = OpportunisticChannel.class.getSimpleName();

    private final ForwardingDaemon.DaemonBinder mDaemon;
    private final OpportunisticFaceManager mOppFaceManager;
    private final long mFaceId;
    private final String mUuid;
    private final String mHost;
    private final int mPort;

    /** Main constructor
     * @param daemon Binder to the ForwardingDaemon
     * @param oppFaceMgr Opportunistic Face Manager
     * @param uuid UUID of the corresponding device
     * @param faceId FaceId of the Face this channel will be attached to
     * @param host IP address of the corresponding device
     * @param port Port number of the corresponding device
     */
    OpportunisticChannel(ForwardingDaemon.DaemonBinder daemon, OpportunisticFaceManager oppFaceMgr, String uuid, long faceId, String host, int port) {
        Log.d(TAG, "Creating OpportunisticChannel for " + host + ":" + port);
        mDaemon = daemon;
        mOppFaceManager = oppFaceMgr;
        mFaceId = faceId;
        mUuid = uuid;
        mHost = host;
        mPort = port;
    }

    // Called by the c++ code to send a packet.
    void send(byte[] buffer) {
        Log.d(TAG, "Attempting to send " + buffer.length + " bytes through UUID " + mUuid + " to " + mHost + ":" + mPort);
        ConnectionTask ct = new ConnectionTask(mHost, mPort, buffer);
        ct.execute();
    }

    /** ConnectionTask is used to perform a transfer with
     */
    private class ConnectionTask extends AsyncTask<Void, Void, Boolean> {
        private String mHost;
        private int mPort;
        private byte[] mBuffer;

        ConnectionTask(String host, int port, byte[] buffer) {
            mHost = host;
            mPort = port;
            mBuffer = buffer;
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
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                dos.write(mBuffer, 0, mBuffer.length);
                dos.flush();
                dos.close();
                transferSucceeded = true;
            } catch (IOException e) {
                Log.d(TAG, "Transfer failed.");
                mOppFaceManager.bringDownFace(mUuid);
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
            mDaemon.sendComplete(mFaceId, transferSucceeded);
        }
    }
}
