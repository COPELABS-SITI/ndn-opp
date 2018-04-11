package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Plsa;

/**
 * Created by copelabs on 11/04/2018.
 */

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

