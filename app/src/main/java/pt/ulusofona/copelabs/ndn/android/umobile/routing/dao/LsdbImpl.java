package pt.ulusofona.copelabs.ndn.android.umobile.routing.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.named_data.jndn.Data;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

import org.apache.commons.lang3.SerializationUtils;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.LsTable;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.RoutingDatabase;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.RoutingEntryTable;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.manager.SyncManager;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.manager.SyncManagerImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Plsa;

/**
 * Created by copelabs on 09/04/2018.
 */

public class LsdbImpl implements Lsdb {

    private SQLiteDatabase mDabberDatabase;


    private SyncManagerImpl mSyncMngr;

    public LsdbImpl(Context context, String UUID) {
        mDabberDatabase = RoutingDatabase.getInstance(context).getDbAccess();
        mSyncMngr= new SyncManagerImpl(UUID,context);
    }

    @Override
    public void insertPlsa(Plsa plsa) {
        ContentValues values = new ContentValues();
        values.put(LsTable.COLUMN_NAME, plsa.getName());
        values.put(LsTable.COLUMN_COST, plsa.getCost());
        values.put(LsTable.COLUMN_NEIGHBOR, plsa.getNeighbor());
        mDabberDatabase.insert(LsTable.TABLE_NAME, null, values);
        sendData(plsa);

    }

    @Override
    public void deletePlsa(Plsa plsa) {
        String where = LsTable.getWhereByPrimaryKey(plsa.getName(), plsa.getNeighbor());
        mDabberDatabase.delete(RoutingEntryTable.TABLE_NAME, where, null);
    }

    @Override
    public void updatePlsa(Plsa plsa) {
        String where = LsTable.getWhereByPrimaryKey(plsa.getName(), plsa.getNeighbor());
        ContentValues values = new ContentValues();
        values.put(LsTable.COLUMN_NAME, plsa.getName());
        values.put(LsTable.COLUMN_COST, plsa.getCost());
        values.put(LsTable.COLUMN_NEIGHBOR, plsa.getNeighbor());
        mDabberDatabase.update(LsTable.TABLE_NAME, values, where, null);
    }

    @Override
    public boolean existsName(String name, String neighbor) {
        try {
            return getPlsa(name, neighbor) != null;
        } catch (NeighborNotFoundException e) {
            return false;
        }
    }

    @Override
    public Plsa getPlsa(String name, String neighbor)  throws NeighborNotFoundException {
        Plsa plsa= null;
        String where = LsTable.getWhereByPrimaryKey(name, neighbor);
        Cursor cursor = mDabberDatabase.query(LsTable.TABLE_NAME, null, where,
                null, null, null, null, null);
        if(cursor.moveToFirst()) {
            plsa = new Plsa(
                    cursor.getString(cursor.getColumnIndex(LsTable.COLUMN_NAME)),
                    cursor.getLong(cursor.getColumnIndex(LsTable.COLUMN_COST)),
                    cursor.getString(cursor.getColumnIndex(LsTable.COLUMN_NEIGHBOR))
            );
        } else {
            throw new NeighborNotFoundException();
        }
        cursor.close();
        return plsa;

    }

    private void sendData(Plsa plsa){
        if(mSyncMngr!=null) {
            if(mSyncMngr.isChronoSyncOn()) {
                Data data = new Data(new Name(mSyncMngr.getmApplicationDataPrefix() + "/" + mSyncMngr.getSequence()));
                Blob blob = new Blob(SerializationUtils.serialize(plsa));
                data.setContent(blob);
                mSyncMngr.senData(data);
            }
        }
    }
}
