package pt.ulusofona.copelabs.ndn.android.umobile.routing.database;

/**
 * Created by copelabs on 04/04/2018.
 */

public abstract class LsTable {

    public static final String TABLE_NAME = "plsa";

    public static final String COLUMN_NAME = "name";

    public static final String COLUMN_COST = "cost";

    public static final String COLUMN_NEIGHBOR = "neighbor";

    public static final String CREATE_PLSA_TABLE = "CREATE TABLE "
            + TABLE_NAME
            + "("
            + COLUMN_NAME + " TEXT, "
            + COLUMN_COST + " INTEGER, "
            + COLUMN_NEIGHBOR + " TEXT, "
            + "PRIMARY KEY (" + COLUMN_NAME + ", " + COLUMN_NEIGHBOR + ")"
            + ");";

    /** This method returns SQL command used to find an entry on this table */
    public static String getWhereByPrimaryKey(String name, String neighbor) {
        return COLUMN_NAME + "='" + name + "'" + "AND " + COLUMN_NEIGHBOR + "='" + neighbor + "'";
    }
}
