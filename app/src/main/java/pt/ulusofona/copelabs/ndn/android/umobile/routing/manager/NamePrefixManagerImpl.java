package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import net.named_data.jndn.Data;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

import org.apache.commons.lang3.SerializationUtils;

import pt.ulusofona.copelabs.ndn.android.models.FibEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.LsdbDaoImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.RoutingEntryDao;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.dao.RoutingEntryDaoImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Plsa;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks.RibUpdaterImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.utilities.Utilities;

/**
 * This class is used to check when ther is a new plsa or when a new prefix was registered by
 * an external application.
 * @author Omar Aponte (COPELABS/ULHT)
 * @version 1.0
 *          COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 02/14/18
 */

public class NamePrefixManagerImpl implements SyncManagerImpl.SyncManagerInterface,NamePrefixManager{

    /**
     * Variable used for debug.
     */
    private String TAG = NamePrefixManagerImpl.class.getSimpleName();
    /**
     * Binder to communicate with ndn.
     */
    private OpportunisticDaemon.Binder mBinder;

    /**
     * This object contains the functions to communicate with database.
     */
    private LsdbDaoImpl mLsdbImpl;

    /**
     * Rib updater, objedt used to know when a new prefix is registered.
     */
    private RibUpdaterImpl mRibUpdater;

    /**
     * SyncManager used to send plsa using chronosync.
     */
    private SyncManagerImpl mSyncMnger;

    private RoutingEntryDao mRoutingEntryDao;

    private  Context mContext;

    private Boolean mCheckFib=false;
    @Override
    public void OnNewPlsa(Plsa plsa) {
        Log.d(TAG,"OnNewPLSA uuid " + plsa.getNeighbor());
        mLsdbImpl.insertPlsa(plsa);
        mRibUpdater.updateRoutingEntry(plsa.getName(),plsa.getNeighbor(),plsa.getCost());
        Toast.makeText(mContext,"NewPLSA",Toast.LENGTH_LONG).show();
    }

    /**
     * Constructor of NamePrefixManagerImpl class.
     * @param binder binder to communicate with ndn.
     * @param context context of the application.
     * @param ribUpdater ribupdater.
     * @param syncManager syncmanager.
     */
    public NamePrefixManagerImpl(OpportunisticDaemon.Binder binder, Context context, RibUpdaterImpl ribUpdater, SyncManagerImpl syncManager){
        mBinder=binder;
        mSyncMnger = syncManager;
        mRoutingEntryDao = new RoutingEntryDaoImpl(context);
        mLsdbImpl = new LsdbDaoImpl(context);
        mRibUpdater = ribUpdater;
        mContext=context;
        startCheckFib();
    }


    /**
     * This method starts a thread which takes care of send a plsa when an external application
     * registers a prefix.
     */
    public void startCheckFib(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                        try {
                            Thread.sleep(30000);
                            if (mCheckFib) {
                                for (FibEntry fibEntry : mBinder.getForwardingInformationBase()) {
                                    //Log.d(TAG, "Prefix: "+ fibEntry.getNextHops());
                                    //if(!fibEntry.getPrefix().contains("localhost") || fibEntry.getPrefix().equals("/") || !fibEntry.getPrefix().contains("dabber")){
                                    if (!fibEntry.getPrefix().contains("localhost") && !fibEntry.getPrefix().contains("dabber") && !fibEntry.getPrefix().equals("/")) {
                                        Log.d(TAG, "Prefix: " + fibEntry.getPrefix());
                                        Log.d(TAG, "Exists: " + mLsdbImpl.existsName(fibEntry.getPrefix(), mBinder.getUmobileUuid()));
                                        //if(!mLsdbImpl.existsName(fibEntry.getPrefix(), mBinder.getUmobileUuid())) {
                                        if(!mRoutingEntryDao.isRoutingEntryExists(fibEntry.getPrefix(), mBinder.getUmobileUuid())) {
                                            Plsa plsa = new Plsa(fibEntry.getPrefix(), Utilities.getTimestampInSeconds(), mBinder.getUmobileUuid());
                                            mLsdbImpl.insertPlsa(plsa);
                                            Log.d(TAG, "Sending packet");
                                            sendData(plsa);
                                        }
                                    }
                                }
                            }
                        } catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                }

        }).start();

    }

    /**
     * This method is used to send a plsa using SyncManager functions.
     * @param plsa plsa to be sent.
     */
    private void sendData(Plsa plsa){
        if(mSyncMnger!=null) {
            if(mSyncMnger.isChronoSyncOn()) {
                Data data = new Data(new Name(mSyncMnger.getmApplicationDataPrefix() + "/" + mSyncMnger.getSequence()));
                Blob blob = new Blob(SerializationUtils.serialize(plsa));
                data.setContent(blob);
                mSyncMnger.sendData(data);
                Log.d(TAG, "PACKET SENT");
            }
        }
    }

    @Override
    public void start() {
        SyncManagerListeners.registerSyncMnagerListener(this);
        mCheckFib=true;
    }

    @Override
    public void stop() {
        SyncManagerListeners.unRegisterSyncMnagerListener(this);
        mCheckFib=false;
    }
}
