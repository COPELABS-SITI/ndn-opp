package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;


import android.content.Context;
import android.util.Log;

import net.named_data.jndn.Data;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

import org.apache.commons.lang3.SerializationUtils;

import java.util.List;

import pt.ulusofona.copelabs.ndn.android.models.FibEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.LsdbDao;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.LsdbDaoImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
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

    private LsdbDaoImpl mLsdbImpl;

    private RibUpdaterImpl mRibUpdater;

    private SyncManagerImpl mSyncMnger;


    @Override
    public void OnNewPlsa(Plsa plsa) {
        mLsdbImpl.insertPlsa(plsa);
        mRibUpdater.updateRoutingEntry(plsa.getName(),plsa.getNeighbor(),plsa.getCost());
    }

    public interface NamePrefixManagerInterface{
        void onNewNamePrefix(Plsa plsa);
    }

    private NamePrefixManagerInterface mInterface;

    public NamePrefixManager(OpportunisticDaemon.Binder binder, Context context, RibUpdaterImpl ribUpdater, SyncManagerImpl syncManager){
        mBinder=binder;
        mContext = context;
        mSyncMnger = syncManager;
        mLsdbImpl = new LsdbDaoImpl(context);
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
                                        if(!mLsdbImpl.existsName(fibEntry.getPrefix(),mBinder.getUmobileUuid())) {
                                            Plsa plsa = new Plsa(fibEntry.getPrefix(), Utilities.getTimestampInSeconds(), mBinder.getUmobileUuid());
                                            mLsdbImpl.insertPlsa(plsa);
                                            sendData(plsa);
                                        }
                                    }
                            }
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                    }
                }

        }).start();

    }

    private void sendData(Plsa plsa){
        if(mSyncMnger!=null) {
            if(mSyncMnger.isChronoSyncOn()) {
                Data data = new Data(new Name(mSyncMnger.getmApplicationDataPrefix() + "/" + mSyncMnger.getSequence()));
                Blob blob = new Blob(SerializationUtils.serialize(plsa));
                data.setContent(blob);
                mSyncMnger.senData(data);
            }
        }
    }
    }
