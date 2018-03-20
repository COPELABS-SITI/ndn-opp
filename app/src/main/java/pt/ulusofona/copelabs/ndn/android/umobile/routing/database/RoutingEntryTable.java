package pt.ulusofona.copelabs.ndn.android.umobile.routing.database;

/**
 * Created by miguel on 20-03-2018.
 */

public abstract class RoutingEntryTable {

    public static final String TABLE_NAME = "routing_table";
    public static final String COLUMN_PREFIX = "prefix";
    public static final String COLUMN_COST = "cost";
    public static final String COLUMN_FACE = "face";

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_PREFIX + " TEXT, "
                    + COLUMN_FACE + " TEXT, "
                    + COLUMN_COST + " INTEGER, "
                    + "PRIMARY KEY (" + COLUMN_PREFIX + ", " + COLUMN_FACE + ")"
                    + ");";

    public static String getWhereByPrimaryKey(String prefix, long faceId) {
        return COLUMN_PREFIX + "='" + prefix + "'" + "AND " + COLUMN_FACE + "='" + faceId + "'";
    }

}
