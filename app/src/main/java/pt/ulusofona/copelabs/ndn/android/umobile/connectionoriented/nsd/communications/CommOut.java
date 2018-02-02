package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications;


import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.HostInfo;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class CommOut {

    private static final String TAG = CommOut.class.getSimpleName();

    public void sendData(HostInfo host, Object object) {
        Log.i(TAG, "Attempting to send to " + host.toString());
        new SenderTask(host, object).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void broadcast(ArrayList<NsdInfo> hosts, Object object) {
        Log.i(TAG, "Broadcasting data");
        for(HostInfo host : hosts) {
            sendData(host, object);
        }
    }

    /** SenderTask is used to perform a transfer
     */
    private class SenderTask extends AsyncTask<Void, Void, Boolean> {

        private HostInfo mSender;
        private Object mPayload;


        SenderTask(HostInfo sender, Object payload) {
            mSender = sender;
            mPayload = payload;
        }

        /** Performs the actual transfer of the packet.
         * @param voids no parameters
         * @return boolean value reflecting whether the transfer is a success or not
         */
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                Socket connection = new Socket();
                connection.connect(new InetSocketAddress(mSender.getIpAddress(), mSender.getPort()));
                Log.d(TAG, "Connection established to " + connection.toString() + " ? " + connection.isConnected());
                ObjectOutputStream oos = new ObjectOutputStream(connection.getOutputStream());
                oos.writeObject(mPayload);
                oos.flush();
                oos.close();
                connection.close();
                Log.i(TAG, "Transfer succeed.");
            } catch (IOException e) {
                Log.e(TAG, "Transfer failed.");
                e.printStackTrace();
            }
            return null;
        }

    }

}
