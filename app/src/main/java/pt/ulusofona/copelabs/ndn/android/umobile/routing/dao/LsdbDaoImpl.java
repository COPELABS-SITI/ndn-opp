package pt.ulusofona.copelabs.ndn.android.umobile.routing.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import net.named_data.jndn.Data;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

import org.apache.commons.lang3.SerializationUtils;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.LsTable;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.RoutingDatabase;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.RoutingEntryTable;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.manager.SyncManagerImpl;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.manager.SyncManagerListeners;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Plsa;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.RoutingEntry;

/**
 * Created by copelabs on 09/04/2018.
 */

public class LsdbDaoImpl implements LsdbDao {

    private SQLiteDatabase mDabberDatabase;

    public LsdbDaoImpl(Context context) {
        mDabberDatabase = RoutingDatabase.getInstance(context).getDbAccess();
    }

    @Override
    public void insertPlsa(Plsa plsa) {
        ContentValues values = new ContentValues();
        values.put(LsTable.COLUMN_NAME, plsa.getName());
        values.put(LsTable.COLUMN_COST, plsa.getCost());
        values.put(LsTable.COLUMN_NEIGHBOR, plsa.getNeighbor());
        mDabberDatabase.insert(LsTable.TABLE_NAME, null, values);
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

    /**
     * This method returns all entries from RoutingEntry table
     * @return all entries on RoutingEntry table
     */
    @Override
    public List<Plsa> getAllEntries() {
        List<Plsa> plsaEntries = new ArrayList<>();
        Cursor cursor = mDabberDatabase.query(true, LsTable.TABLE_NAME, null,
                null, null, null, null, null, null);
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            plsaEntries.add(new Plsa(
                    cursor.getString(cursor.getColumnIndex(LsTable.COLUMN_NAME)),
                    cursor.getLong(cursor.getColumnIndex(LsTable.COLUMN_COST)),
                    cursor.getString(cursor.getColumnIndex(LsTable.COLUMN_NEIGHBOR))
                )
            );
        }
        cursor.close();
        return plsaEntries;
    }
}
