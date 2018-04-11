package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;


import android.content.Context;
import android.util.Log;

import net.named_data.jndn.encoding.EncodingException;

import java.io.IOException;

import pt.ulusofona.copelabs.ndn.android.models.FibEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.common.PacketManager;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.LsdbImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Plsa;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks.RibUpdaterImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.utilities.Utilities;


/**
 * Created by copelabs on 20/03/2018.
 */

public class NamePrefixManager implements SyncManagerImpl.SyncManagerInterface{


    private String TAG = NamePrefixManager.class.getSimpleName();

    private OpportunisticDaemon.Binder mBinder;

    private Context mContext;

    private LsdbImpl mLsdbImpl;

    private RibUpdaterImpl mRibUpdater;

    @Override
    public void OnNewPlsa(Plsa plsa) {
        mRibUpdater.updateRoutingEntry(plsa.getName(),plsa.getNeighbor(),plsa.getCost());
    }

    public interface NamePrefixManagerInterface{
        void onNewNamePrefix(Plsa plsa);
    }

    private NamePrefixManagerInterface mInterface;

    public NamePrefixManager(OpportunisticDaemon.Binder binder, Context context, RibUpdaterImpl ribUpdater){
        mBinder=binder;
        mContext = context;
        mLsdbImpl = new LsdbImpl(context, binder.getUmobileUuid());
        mRibUpdater = ribUpdater;
        SyncManagerListeners.registerSyncMnagerListener(this);
        start();

    }

    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                        try {
                            Thread.sleep(5000);
                            for (FibEntry fibEntry : mBinder.getForwardingInformationBase()){
                                //Log.d(TAG, "Prefix: "+ fibEntry.getNextHops());
                                //if(!fibEntry.getPrefix().contains("localhost") || fibEntry.getPrefix().equals("/") || !fibEntry.getPrefix().contains("dabber")){
                                if(!fibEntry.getPrefix().contains("localhost") && !fibEntry.getPrefix().contains("dabber") && !fibEntry.getPrefix().equals("/")){
                                    Log.d(TAG, "Prefix: " + fibEntry.getPrefix());
                                    Log.d(TAG, "Exists: " + mLsdbImpl.existsName(fibEntry.getPrefix(), mBinder.getUmobileUuid()));
                                    if(!mLsdbImpl.existsName(fibEntry.getPrefix(),mBinder.getUmobileUuid()))
                                        mLsdbImpl.insertPlsa(new Plsa(fibEntry.getPrefix(), Utilities.getTimestampInSeconds(),mBinder.getUmobileUuid()));
                                }
                            }
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }

        }).start();

    }
    }
