package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class implements the methods needed to send notifications
 * from SyncManagerImpl class to the listeners.
 * @author Omar Aponte(COPELABS/ULHT)
 */
import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Plsa;


public abstract class SyncManagerListeners {

    private static List<SyncManagerImpl.SyncManagerInterface> listeners = new ArrayList<>();

    public static void registerSyncMnagerListener(SyncManagerImpl.SyncManagerInterface listener){
        listeners.add(listener);
    }

    public static void unRegisterSyncMnagerListener(SyncManagerImpl.SyncManagerInterface listener){
        listeners.remove(listener);
    }

    public static void notifyListeners (Plsa plsa){
        for(SyncManagerImpl.SyncManagerInterface listener : listeners){
            if(listener instanceof SyncManagerImpl.SyncManagerInterface)
                listener.OnNewPlsa(plsa);
        }
    }
}

