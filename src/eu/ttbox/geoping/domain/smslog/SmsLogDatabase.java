package eu.ttbox.geoping.domain.smslog;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;

public class SmsLogDatabase {

    @SuppressWarnings("unused")
    private static final String TAG = "SmsLogDatabase";

    public static final String TABLE_SMSLOG_FTS = "personFTS";

    public static class SmsLogColumns {

        public static final String COL_ID = BaseColumns._ID;
        public static final String COL_ACTION = "ACTION";
        public static final String COL_PHONE = "PHONE";
        public static final String COL_MSG = "MSG";
        public static final String COL_TIME = "TIME";

        // All Cols
        public static final String[] ALL_KEYS = new String[] { COL_ID, COL_TIME, COL_ACTION, COL_PHONE, COL_MSG };
        // Where Clause
        public static final String SELECT_BY_ENTITY_ID = String.format("%s = ?", "rowid");

    }

    private final SmsLogOpenHelper mDatabaseOpenHelper;
    private static final HashMap<String, String> mPersonColumnMap = buildUserColumnMap();

    public SmsLogDatabase(Context context) {
        mDatabaseOpenHelper = new SmsLogOpenHelper(context);
    }

    private static HashMap<String, String> buildUserColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        // Add Identity Column
        map.put(PersonColumns.COL_ID, "rowid AS " + BaseColumns._ID);
        for (String col : SmsLogColumns.ALL_KEYS) {
            if (!col.equals(SmsLogColumns.COL_ID)) {
                map.put(col, col);
            }
        }
        // Add Suggest Aliases
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, String.format("%s AS %s", SmsLogColumns.COL_ACTION, SearchManager.SUGGEST_COLUMN_TEXT_1));
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_2, String.format("%s AS %s", SmsLogColumns.COL_PHONE, SearchManager.SUGGEST_COLUMN_TEXT_2));
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        // Add Other Aliases
        return map;
    }

    public Cursor getEntityById(String rowId, String[] columns) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] { rowId };
        return queryEntities(columns, selection, selectionArgs, null);
    }

    /**
     * Returns a Cursor over all words that match the given query
     * @param projection
     *            The columns to include, if null then all are included
     * @param query
     *            The string to search for
     * 
     * @return Cursor over all words that match, or null if none found.
     */
    public Cursor getEntityMatches(String[] projection, String query, String order) {
        String selection = SmsLogColumns.COL_ACTION + " MATCH ?";
        String queryString = new StringBuilder(query).append("*").toString();
        String[] selectionArgs = new String[] { queryString };
        return queryEntities(projection, selection, selectionArgs, order);
    }

    public Cursor queryEntities(String[] projection, String selection, String[] selectionArgs, String order) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_SMSLOG_FTS);
        builder.setProjectionMap(mPersonColumnMap);
        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, order);
        return cursor;
    }

    public long insertEntity(ContentValues userValues) throws SQLException {
        long result = -1;
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.insertOrThrow(TABLE_SMSLOG_FTS, null, userValues);
                // commit
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
        return result;
    }

    public int deleteEntity(String selection, String[] selectionArgs) {
        int result = -1;
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.delete(TABLE_SMSLOG_FTS, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
        return result;
    }

    public int updateEntity(ContentValues values, String selection, String[] selectionArgs) {
        int result = -1;
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.update(TABLE_SMSLOG_FTS, values, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
        return result;
    }
}
