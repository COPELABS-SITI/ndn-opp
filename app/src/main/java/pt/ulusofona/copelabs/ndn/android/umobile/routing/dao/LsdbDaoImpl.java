package pt.ulusofona.copelabs.ndn.android.umobile.routing.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.LsTable;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.RoutingDatabase;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.database.RoutingEntryTable;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Plsa;

/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class contains all methods related with LSDB table
 * that are used to communicate with db.
 * @author Omar Aponte(COPELABS/ULHT)
 */

public class LsdbDaoImpl implements LsdbDao {

    private SQLiteDatabase mDabberDatabase;

    public LsdbDaoImpl(Context context) {
        mDabberDatabase = RoutingDatabase.getInstance(context).getDbAccess();
    }

    /**
     * THis method inserts a new PLSA in the database.
     * @param plsa Plsa to be inserted.
     */
    @Override
    public void insertPlsa(Plsa plsa) {
        ContentValues values = new ContentValues();
        values.put(LsTable.COLUMN_NAME, plsa.getName());
        values.put(LsTable.COLUMN_COST, plsa.getCost());
        values.put(LsTable.COLUMN_NEIGHBOR, plsa.getNeighbor());
        mDabberDatabase.insert(LsTable.TABLE_NAME, null, values);
    }

    /**
     * This method deletes a PLSA object from the database.
     * @param plsa Plsa to be delete.
     */
    @Override
    public void deletePlsa(Plsa plsa) {
        String where = LsTable.getWhereByPrimaryKey(plsa.getName(), plsa.getNeighbor());
        mDabberDatabase.delete(RoutingEntryTable.TABLE_NAME, where, null);
    }

    /**
     * This method updates a PLSA in the database.
     * @param plsa Plsa to e updated.
     */
    @Override
    public void updatePlsa(Plsa plsa) {
        String where = LsTable.getWhereByPrimaryKey(plsa.getName(), plsa.getNeighbor());
        ContentValues values = new ContentValues();
        values.put(LsTable.COLUMN_NAME, plsa.getName());
        values.put(LsTable.COLUMN_COST, plsa.getCost());
        values.put(LsTable.COLUMN_NEIGHBOR, plsa.getNeighbor());
        mDabberDatabase.update(LsTable.TABLE_NAME, values, where, null);
    }

    /**
     * This method is used to confirm that a PLSA exists.
     * @param name name of the plsa.
     * @param neighbor naighbor of the plsa.
     * @return True value if exists the PLAS otherwise false.
     */
    @Override
    public boolean existsName(String name, String neighbor) {
        try {
            return getPlsa(name, neighbor) != null;
        } catch (NeighborNotFoundException e) {
            return false;
        }
    }

    /**
     * This method get a plsa based on name and neighbor identifier.
     * @param name name of the plsa
     * @param neighbor neighbor of the plsa
     * @return Plsa object.
     * @throws NeighborNotFoundException
     */
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
            cursor.close();
            throw new NeighborNotFoundException();
        }
        cursor.close();
        return plsa;

    }

    /**
     * This method returns all entries from PLSA table
     * @return all entries on PLSA table
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
