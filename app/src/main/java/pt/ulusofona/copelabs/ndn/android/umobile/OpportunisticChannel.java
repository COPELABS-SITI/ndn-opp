/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * //TODO: Description.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;

// @TODO: make this asynchronous ...
class OpportunisticChannel {
    private Socket mConnection;
    private SocketChannel mChannel;

    public OpportunisticChannel(String host, int port) {
        mConnection = new Socket();
        try {
            mConnection.connect(new InetSocketAddress(host, port));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
