package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.channels;


import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.buffering.BufferIn;

public class OpportunisticChannelInTask extends Thread {

    private static final String TAG = OpportunisticChannelInTask.class.getSimpleName();
    private ServerSocket mAcceptingSocket;
    private boolean mEnabled;

    OpportunisticChannelInTask(ServerSocket socket) {
        mAcceptingSocket = socket;
    }

    @Override
    public void run() {
        mEnabled = true;
        Log.d(TAG, "Accepting on " + mAcceptingSocket.toString());

        while (mEnabled) {
            try {
                // Accept the next connection.
                new BufferIn(mAcceptingSocket.accept()).start();
            } catch (IOException e) {
                Log.e(TAG, "Connection went WRONG. IOException");
                e.printStackTrace();

            }
        }
        Log.i(TAG, "Exiting thread");
    }

    public void close() {
        try {
            mEnabled = false;
            mAcceptingSocket.close();
        } catch (IOException e) {
            Log.w(TAG, "Failure to close the ServerSocket " + e.getMessage());
        }
    }
}
