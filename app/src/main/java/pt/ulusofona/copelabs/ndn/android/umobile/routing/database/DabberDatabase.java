package pt.ulusofona.copelabs.ndn.android.umobile.routing.database;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by miguel on 20-03-2018.
 */

public class DabberDatabase extends SQLiteOpenHelper {

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "dabber_db";

    private static DabberDatabase mInstance;
    private static SQLiteDatabase mDatabase;


    private DabberDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DabberDatabase getInstance(Context context) {
        if(mInstance == null) {
            mInstance = new DabberDatabase(context);
            mDatabase = mInstance.getWritableDatabase();
        }
        return mInstance;
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        // create notes table
        db.execSQL(RoutingEntryTable.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + RoutingEntryTable.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }

    public SQLiteDatabase getDbAccess() {
        return mDatabase;
    }

    public void close() {
        mDatabase.close();
    }

}