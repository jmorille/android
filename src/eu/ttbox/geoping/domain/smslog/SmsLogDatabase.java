package eu.ttbox.geoping.domain.smslog;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;

public class SmsLogDatabase {

    @SuppressWarnings("unused")
    private static final String TAG = "SmsLogDatabase";

    public static final String TABLE_SMSLOG_FTS = "personFTS";

    public static class SmsLogColumns {

        public static final String COL_ID = "rowid";
        public static final String COL_ACTION = "ACTION";
        public static final String COL_PHONE = "PHONE";
        public static final String COL_MSG = "MSG";
        public static final String COL_TIME = "TIME";

        public static final String[] ALL_KEYS = new String[] { COL_ID, COL_TIME, COL_ACTION, COL_PHONE, COL_MSG };

    }

    private final SmsLogOpenHelper mDatabaseOpenHelper;
    private static final HashMap<String, String> mPersonColumnMap = buildUserColumnMap();

    public SmsLogDatabase(Context context) {
        mDatabaseOpenHelper = new SmsLogOpenHelper(context);
    }

    private static HashMap<String, String> buildUserColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        // Add Identity Column
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
        return queryEntities(selection, selectionArgs, columns, null);
    }

    /**
     * Returns a Cursor over all words that match the given query
     * 
     * @param query
     *            The string to search for
     * @param columns
     *            The columns to include, if null then all are included
     * @return Cursor over all words that match, or null if none found.
     */
    public Cursor getEntityMatches(String query, String[] columns, String order) {
        String selection = SmsLogColumns.COL_ACTION + " MATCH ?";
        String queryString = new StringBuilder(query).append("*").toString();
        String[] selectionArgs = new String[] { queryString };
        return queryEntities(selection, selectionArgs, columns, order);
    }

    public Cursor queryEntities(String selection, String[] selectionArgs, String[] columns, String order) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_SMSLOG_FTS);
        builder.setProjectionMap(mPersonColumnMap);
        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns, selection, selectionArgs, null, null, order);
        return cursor;
    }

    public long insert(ContentValues userValues) throws SQLException {
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

    public int delete(String selection, String[] selectionArgs) {
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

    public int update(ContentValues values, String selection, String[] selectionArgs) {
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
