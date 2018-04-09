/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-20
 * This class contains all methods related with RoutingEntry table
 * that are used to communicate with db
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.RoutingDatabase;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.RoutingEntryTable;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.RoutingEntry;


public class RoutingEntryDaoImpl implements RoutingEntryDao {

    /** This object is used to communicate with dabber's database */
    private SQLiteDatabase mDabberDatabase;

    /**
     * This method is the constructor of RoutingEntryDaoImpl class
     * @param context Application context
     */
    public RoutingEntryDaoImpl(Context context) {
        mDabberDatabase = RoutingDatabase.getInstance(context).getDbAccess();
    }

    /**
     * This method returns all entries from RoutingEntry table
     * @return all entries on RoutingEntry table
     */
    @Override
    public List<RoutingEntry> getAllEntries() {
        List<RoutingEntry> routingEntries = new ArrayList<>();
        Cursor cursor = mDabberDatabase.query(true, RoutingEntryTable.TABLE_NAME, null,
                null, null, null, null, null, null);
        for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            routingEntries.add(new RoutingEntry(
                    cursor.getString(cursor.getColumnIndex(RoutingEntryTable.COLUMN_PREFIX)),
                    cursor.getLong(cursor.getColumnIndex(RoutingEntryTable.COLUMN_FACE)),
                    cursor.getLong(cursor.getColumnIndex(RoutingEntryTable.COLUMN_COST))
                )
            );
        }
        cursor.close();
        return routingEntries;
    }

    /**
     * This method returns a specific entry from RoutingEntry table
     * @param prefix name prefix
     * @param faceId face id associated to name prefix
     * @return asked RoutingEntry
     */
    @Override
    public RoutingEntry getRoutingEntry(String prefix, long faceId) throws NeighborNotFoundException {
        RoutingEntry routingEntry = null;
        String where = RoutingEntryTable.getWhereByPrimaryKey(prefix, faceId);
        Cursor cursor = mDabberDatabase.query(RoutingEntryTable.TABLE_NAME, null, where,
                null, null, null, null, null);
        if(cursor.moveToFirst()) {
            routingEntry = new RoutingEntry(
                    cursor.getString(cursor.getColumnIndex(RoutingEntryTable.COLUMN_PREFIX)),
                    cursor.getLong(cursor.getColumnIndex(RoutingEntryTable.COLUMN_FACE)),
                    cursor.getLong(cursor.getColumnIndex(RoutingEntryTable.COLUMN_COST))
            );
        } else {
            throw new NeighborNotFoundException();
        }
        cursor.close();
        return routingEntry;
    }

    /**
     * This method inserts a new entry on RoutingEntry table
     * @param routingEntry entry to be inserted
     */
    @Override
    public void createRoutingEntry(RoutingEntry routingEntry) {
        ContentValues values = new ContentValues();
        values.put(RoutingEntryTable.COLUMN_PREFIX, routingEntry.getPrefix());
        values.put(RoutingEntryTable.COLUMN_FACE, routingEntry.getFace());
        values.put(RoutingEntryTable.COLUMN_COST, routingEntry.getCost());
        mDabberDatabase.insert(RoutingEntryTable.TABLE_NAME, null, values);
    }

    /**
     * This method updates an entry on RoutingEntry table
     * @param routingEntry entry to be updated
     */
    @Override
    public void updateRoutingEntry(RoutingEntry routingEntry) {
        String where = RoutingEntryTable.getWhereByPrimaryKey(routingEntry.getPrefix(), routingEntry.getFace());
        ContentValues values = new ContentValues();
        values.put(RoutingEntryTable.COLUMN_PREFIX, routingEntry.getPrefix());
        values.put(RoutingEntryTable.COLUMN_FACE, routingEntry.getFace());
        values.put(RoutingEntryTable.COLUMN_COST, routingEntry.getCost());
        mDabberDatabase.update(RoutingEntryTable.TABLE_NAME, values, where, null);
    }

    /**
     * This method deletes an entry that exists on RoutingEntry table
     * @param routingEntry entry to be updated
     */
    @Override
    public void deleteRoutingEntry(RoutingEntry routingEntry) {
        String where = RoutingEntryTable.getWhereByPrimaryKey(routingEntry.getPrefix(), routingEntry.getFace());
        mDabberDatabase.delete(RoutingEntryTable.TABLE_NAME, where, null);
    }

    /**
     * This method checks if an entry exists on RoutingEntry table
     * @param routingEntry entry to be checked
     * @return true if exists, false if not
     */
    @Override
    public boolean isRoutingEntryExists(RoutingEntry routingEntry) {
        try {
            return getRoutingEntry(routingEntry.getPrefix(), routingEntry.getFace()) != null;
        } catch (NeighborNotFoundException e) {
            return false;
        }
    }
}
