package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;


import android.os.RemoteException;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.ContextualManagerNotConnectedException;

/**
 * Created by miguel on 07-03-2018.
 */

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
