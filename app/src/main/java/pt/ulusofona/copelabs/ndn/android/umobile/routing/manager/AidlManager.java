/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This interface sets all the methods used to communicate
 * with contextual manager.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;


import android.os.RemoteException;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.ContextualManagerNotConnectedException;


public interface AidlManager {

    interface Manager {
        void start();
        void stop();
        boolean isBound();
        int getAvailability() throws RemoteException, ContextualManagerNotConnectedException;
        int[] getCentrality() throws RemoteException, ContextualManagerNotConnectedException;
    }

    interface Listener {
        void onContextualManagerConnected();
        void onContextualManagerDisconnected();
    }

}
