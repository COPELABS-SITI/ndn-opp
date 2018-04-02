package pt.ulusofona.copelabs.ndn.android.umobile.routing.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.RoutingDatabase;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.RoutingEntryTable;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.RoutingEntry;

/**
 * Created by miguel on 20-03-2018.
 */

public class RoutingEntryDaoImpl implements RoutingEntryDao {

    private SQLiteDatabase mDabberDatabase;

    public RoutingEntryDaoImpl(Context context) {
        mDabberDatabase = RoutingDatabase.getInstance(context).getDbAccess();
    }

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

    @Override
    public RoutingEntry getRoutingEntry(String prefix, long faceId) {
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
        }
        cursor.close();
        return routingEntry;
    }

    @Override
    public void createRoutingEntry(RoutingEntry routingEntry) {
        ContentValues values = new ContentValues();
        values.put(RoutingEntryTable.COLUMN_PREFIX, routingEntry.getPrefix());
        values.put(RoutingEntryTable.COLUMN_FACE, routingEntry.getFace());
        values.put(RoutingEntryTable.COLUMN_COST, routingEntry.getCost());
        mDabberDatabase.insert(RoutingEntryTable.TABLE_NAME, null, values);
    }

    @Override
    public void updateRoutingEntry(RoutingEntry routingEntry) {
        String where = RoutingEntryTable.getWhereByPrimaryKey(routingEntry.getPrefix(), routingEntry.getFace());
        ContentValues values = new ContentValues();
        values.put(RoutingEntryTable.COLUMN_PREFIX, routingEntry.getPrefix());
        values.put(RoutingEntryTable.COLUMN_FACE, routingEntry.getFace());
        values.put(RoutingEntryTable.COLUMN_COST, routingEntry.getCost());
        mDabberDatabase.update(RoutingEntryTable.TABLE_NAME, values, where, null);
    }

    @Override
    public void deleteRoutingEntry(RoutingEntry routingEntry) {
        String where = RoutingEntryTable.getWhereByPrimaryKey(routingEntry.getPrefix(), routingEntry.getFace());
        mDabberDatabase.delete(RoutingEntryTable.TABLE_NAME, where, null);
    }

    @Override
    public boolean isRoutingEntryExists(RoutingEntry routingEntry) {
        return getRoutingEntry(routingEntry.getPrefix(), routingEntry.getFace()) != null;
    }
}
