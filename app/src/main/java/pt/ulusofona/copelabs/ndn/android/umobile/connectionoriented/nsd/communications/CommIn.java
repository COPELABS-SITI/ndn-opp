/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class is used to receive NSD requests
 * @author Miguel Tavares (COPELABS/ULHT)
 */

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

    /** This variable is used to debug CommIn class */
    private static final String TAG = CommIn.class.getSimpleName();

    /** This interface is used to notify when data arrives */
    private DiscovererListener.Discoverer mListener;

    /** This socket is used to receive connections from other devices */
    private ServerSocket mServerSocket;

    /** This variable is used to hold this class status */
    private boolean mEnabled = false;

    /** This object is used to hold host info. IP Address and Port */
    private HostInfo mMyInfo;

    public CommIn(DiscovererListener.Discoverer listener, HostInfo myInfo) {
        mListener = listener;
        mMyInfo = myInfo;
    }

    /**
     * This method is used to start receiving communications
     */
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

    /**
     * This method is to stop receiving communications
     */
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
