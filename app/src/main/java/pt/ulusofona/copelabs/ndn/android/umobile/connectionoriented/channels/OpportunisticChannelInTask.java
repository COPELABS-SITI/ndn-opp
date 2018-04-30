/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-01-31
 * This class is used to receive NDN packets from other devices.
 * It creates a new socket to allow multithreading
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.channels;


import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketManager;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;

public class OpportunisticChannelInTask extends Thread {

    /** This variable is used to debug OpportunisticChannelInTask class */
    private static final String TAG = OpportunisticChannelInTask.class.getSimpleName();

    /** This interface is used to notify the packet reception */
    private PacketManager.Observer mPacketManagerObserver;

    /** This socket is used to receive connections requests from other devices */
    private ServerSocket mServerSocket;

    /** This variable holds the state of this class, if is running or not */
    private boolean mEnabled;

    OpportunisticChannelInTask(ServerSocket socket, PacketManager.Observer packetManagerObserver) {
        mServerSocket = socket;
        mPacketManagerObserver = packetManagerObserver;
    }

    @Override
    public void run() {
        mEnabled = true;
        Log.d(TAG, "Accepting on " + mServerSocket.toString());

        while (mEnabled) {
            try {
                // Accept the next connection.
                Socket socket = mServerSocket.accept();


                Log.d(TAG, "Connection from " + socket.toString());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                Packet packet = (Packet) in.readObject();
                in.close();
                if (packet != null) {
                    Log.i(TAG, "Packet received from " + packet.getSender() + " with size of " + packet.getPayloadSize());
                    mPacketManagerObserver.onPacketReceived(packet.getSender(), packet.getPayload());

                    //BufferData.push(packet);
                }

                //new BufferIn(mServerSocket.accept()).startCheckFib();
            } catch (IOException e) {
                Log.e(TAG, "Connection went WRONG.");
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        Log.i(TAG, "Exiting thread");
    }

    /**
     * This method stops from receiving data other devices
     */
    public void close() {
        try {
            mEnabled = false;
            mServerSocket.close();
        } catch (IOException e) {
            Log.w(TAG, "Failure to close the ServerSocket " + e.getMessage());
        }
    }
}
