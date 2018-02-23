/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class receives data from a socket and insert it into a buffer
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.buffering;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;


public class BufferIn extends Thread {

    /** This variable is used to debug BufferIn class */
    private static final String TAG = BufferIn.class.getSimpleName();

    /** This socket is used to receive the data */
    private Socket mSocket;

    public BufferIn(Socket socket) {
        mSocket = socket;
    }

    public void run() {
        try {
            Log.d(TAG, "Connection from " + mSocket.toString());
            ObjectInputStream in = new ObjectInputStream(mSocket.getInputStream());
            Packet packet = (Packet) in.readObject();
            in.close();
            if (packet != null) {
                Log.i(TAG, "Packet received from " + packet.getSender() + " with size of " + packet.getPayloadSize());
                BufferData.push(packet);
            }
        } catch (IOException e) {
            Log.e(TAG, "Connection went WRONG.");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}
