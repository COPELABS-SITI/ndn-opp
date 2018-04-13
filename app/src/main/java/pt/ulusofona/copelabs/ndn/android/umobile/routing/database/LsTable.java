package pt.ulusofona.copelabs.ndn.android.umobile.routing.database;

/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class contains the information to create the Plsa table.
 * @author Omar Aponte(COPELABS/ULHT)
 */
public abstract class LsTable {

    /** This variable holds hte Table name. */
    public static final String TABLE_NAME = "plsa";

    /** Ths variable holds the attribute name. */
    public static final String COLUMN_NAME = "name";

    /** This variable holds the attribute cost. */
    public static final String COLUMN_COST = "cost";

    /** This variable holds the attribute neighbor */
    public static final String COLUMN_NEIGHBOR = "neighbor";

    /** This variable holds the script to create a PLSA table. */
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
