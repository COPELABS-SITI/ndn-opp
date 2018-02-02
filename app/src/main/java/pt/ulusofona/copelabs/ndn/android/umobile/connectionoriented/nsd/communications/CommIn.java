package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.communications;


import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.HostInfo;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class CommIn extends AsyncTask<Void, Void, Void> {

    private static final String TAG = CommIn.class.getSimpleName();
    private DiscovererListener.Discoverer mListener;
    private ServerSocket mServerSocket;
    private boolean mEnabled = false;
    private HostInfo mMyInfo;

    public CommIn(DiscovererListener.Discoverer listener, HostInfo myInfo) {
        mListener = listener;
        mMyInfo = myInfo;
    }

    public synchronized void start() {
        if(!mEnabled) {
            try {
                Log.v(TAG, "Enabling ServerSocket on " + mMyInfo.toString());
                mServerSocket = new ServerSocket();
                mServerSocket.bind(new InetSocketAddress(mMyInfo.getIpAddress(), mMyInfo.getPort()));
                mEnabled = true;
                executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                mListener.onStartDiscoveringSuccess();
            } catch (IOException e) {
                Log.e(TAG, "Failed to open listening socket");
                mListener.onStartDiscoveringFailed();
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Log.d(TAG, "Accepting on " + mServerSocket.toString());
        while (mEnabled) {
            try {
                // @TODO: multi-threaded receiver.
                // Accept the next connection.
                Socket connection = mServerSocket.accept();
                Log.d(TAG, "Connection from " + connection.toString());
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
                Object object = in.readObject();
                if(object instanceof NsdInfo) {
                    NsdInfo nsdInfo = (NsdInfo) object;
                    Log.i(TAG, "Detected " + nsdInfo.toString());
                    mListener.onPeerDetected(nsdInfo);
                } else if (object instanceof ArrayList) {
                    Log.i(TAG, "Detected updated list");
                    ArrayList<NsdInfo> updatedPeerList = (ArrayList<NsdInfo>) object;
                    mListener.onReceivePeerList(updatedPeerList);
                }
                // Cleanup
                in.close();
                // Close connection
                connection.close();
            } catch (IOException e) {
                Log.e(TAG, "Connection went WRONG.");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void close() {
        if(!mServerSocket.isClosed()) {
            try {
                mServerSocket.close();
                mEnabled = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
