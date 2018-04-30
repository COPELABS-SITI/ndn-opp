/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-20
 * This class implements the singleton design patter
 * in order to manages dabber's database.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class RoutingDatabase extends SQLiteOpenHelper {

    /** This variable holds the database version */
    private static final int DATABASE_VERSION = 3;

    /** This variable holds the database name */
    private static final String DATABASE_NAME = "dabber_db";

    /** This object is used to implement the singleton pattern */
    private static RoutingDatabase mInstance;

    /** This object holds a reference to the database */
    private static SQLiteDatabase mDatabase;


    /**
     * This method is the constructor of RoutingDatabase class.
     * @param context
     */
    private RoutingDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This method returns an instance of RoutingDatabase
     * @param context Application context
     * @return Instance of RoutingDatabase
     */
    public static RoutingDatabase getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new RoutingDatabase(context);
            mDatabase = mInstance.getWritableDatabase();
        }
        return mInstance;
    }

    /**
     * This method provides a reference to database
     * @return database reference
     */
    public SQLiteDatabase getDbAccess() {
        return mDatabase;
    }

    /**
     * This method is invoked when the database is being created
     * @param db database reference
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creation of RoutingEntry table
        db.execSQL(RoutingEntryTable.CREATE_TABLE);
        db.execSQL(LsTable.CREATE_PLSA_TABLE);
    }

    /**
     * This method is invoked when the database is being upgraded
     * @param db database reference
     * @param oldVersion int with old version number
     * @param newVersion int with new version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + RoutingEntryTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + LsTable.TABLE_NAME);
        // Create tables again
        onCreate(db);
    }

    public void clearDatabase() {
        mDatabase.delete(RoutingEntryTable.TABLE_NAME, null, null);
        mDatabase.delete(LsTable.TABLE_NAME, null, null);
    }

    /**
     * This method closes the database connection
     */
    public void close() {
        mDatabase.close();
    }

}