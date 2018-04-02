/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-20
 * This class contains all information related with RoutingEntry table
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.database;


public abstract class RoutingEntryTable {

    /** This variable holds the table name */
    public static final String TABLE_NAME = "routing_table";

    /** This variable holds the attribute name prefix */
    public static final String COLUMN_PREFIX = "prefix";

    /** This variable holds the attribute name cost */
    public static final String COLUMN_COST = "cost";

    /** This variable holds the attribute name face */
    public static final String COLUMN_FACE = "face";

    /** This variable holds the SQL string used to create this database table */
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_PREFIX + " TEXT, "
                    + COLUMN_FACE + " TEXT, "
                    + COLUMN_COST + " INTEGER, "
                    + "PRIMARY KEY (" + COLUMN_PREFIX + ", " + COLUMN_FACE + ")"
                    + ");";

    /** This method returns SQL command used to find an entry on this table */
    public static String getWhereByPrimaryKey(String prefix, long faceId) {
        return COLUMN_PREFIX + "='" + prefix + "'" + "AND " + COLUMN_FACE + "='" + faceId + "'";
    }

}
