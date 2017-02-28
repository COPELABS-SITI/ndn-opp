/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * //TODO: Description.
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

    private final String mHost;
    private final int mPort;

    OpportunisticChannel(String host, int port) {
        Log.d(TAG, "Creating OpportunisticChannel for " + host + ":" + port);
        mHost = host;
        mPort = port;
    }

    // Called by the c++ code to send a packet.
    void send(byte[] buffer) {
        Log.d(TAG, "Attempting to send " + buffer.length + " bytes to " + mHost + ":" + mPort);
        ConnectionTask ct = new ConnectionTask(mHost, mPort, buffer);
        ct.execute();
    }

    private class ConnectionTask extends AsyncTask<Void, Void, Void> {
        private String mHost;
        private int mPort;
        private byte[] mBuffer;

        ConnectionTask(String h, int p, byte[] buf) {
            mHost = h;
            mPort = p;
            mBuffer = buf;
        }

        private String hex(byte[] buf, int limit) {
            StringBuilder hexStr = new StringBuilder();
            for(int i = 0; i < limit; i++)
                hexStr.append(Integer.toHexString(buf[i] & 0xFF)).append(" ");
            return hexStr.toString();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Socket connection = new Socket();
                connection.connect(new InetSocketAddress(mHost, mPort));
                Log.d(TAG, "Connection established to " + connection.toString() + " ? " + connection.isConnected());
                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
                dos.write(mBuffer, 0, mBuffer.length);
                Log.d(TAG, "Payload: " + hex(mBuffer, mBuffer.length));
                dos.flush();
                dos.close();
            } catch (IOException e) {
                Log.d(TAG, "Transfer failed.");
            }
            return null;
        }
    }
}
