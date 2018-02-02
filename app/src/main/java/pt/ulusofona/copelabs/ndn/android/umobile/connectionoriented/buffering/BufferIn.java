package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.buffering;

import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;


public class BufferIn extends Thread{

    private static final String TAG = BufferIn.class.getSimpleName();
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
