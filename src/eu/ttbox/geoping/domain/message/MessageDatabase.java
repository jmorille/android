package eu.ttbox.geoping.domain.message;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import eu.ttbox.geoping.domain.person.PersonOpenHelper;

public class MessageDatabase {

    @SuppressWarnings("unused")
    private static final String TAG = "MessageDatabase";

    public static final String TABLE_MESSAGE_FTS = "messageFTS";

    public static class MessageColumns {

        public static final String COL_ID = BaseColumns._ID;
        public static final String COL_NAME = "NAME";
        public static final String COL_PHONE = "PHONE";
        public static final String COL_COLOR = "COLOR";
        // All Cols
        public static final String[] ALL_KEYS = new String[] { COL_ID, COL_NAME, COL_PHONE, COL_COLOR };
        // Where Clause
        public static final String SELECT_BY_ENTITY_ID = String.format("%s = ?", "rowid");
        public static final String SELECT_BY_PHONE_NUMBER = String.format("%s = ?", COL_PHONE);

    }

    private final PersonOpenHelper mDatabaseOpenHelper;
    private static final HashMap<String, String> mPairingColumnMap = buildUserColumnMap();

    public MessageDatabase(Context context) {
        mDatabaseOpenHelper = new PersonOpenHelper(context);
    }

    private static HashMap<String, String> buildUserColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        // Add Id
        map.put(MessageColumns.COL_ID, "rowid AS " + BaseColumns._ID);
        // Add Identity Column
        for (String col : MessageColumns.ALL_KEYS) {
            if (!col.equals(MessageColumns.COL_ID)) {
                map.put(col, col);
            }
        }
        // Add Suggest Aliases
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, String.format("%s AS %s", MessageColumns.COL_NAME, SearchManager.SUGGEST_COLUMN_TEXT_1));
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_2, String.format("%s AS %s", MessageColumns.COL_PHONE, SearchManager.SUGGEST_COLUMN_TEXT_2));
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

    public Cursor getEntityMatches(String[] projection, String query, String order) {
        String selection = MessageColumns.COL_NAME + " MATCH ?";
        String queryString = new StringBuilder(query).append("*").toString();
        String[] selectionArgs = new String[] { queryString };
        return queryEntities(projection, selection, selectionArgs, order);
    }

    public Cursor queryEntities(String[] projection, String selection, String[] selectionArgs, String order) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_MESSAGE_FTS);
        builder.setProjectionMap(mPairingColumnMap);
        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, order);
        // Manage Cursor
        // if (cursor == null) {
        // return null;
        // } else if (!cursor.moveToFirst()) {
        // cursor.close();
        // return null;
        // }
        return cursor;
    }

    public long insertEntity(ContentValues userValues) throws SQLException {
        long result = -1;
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.insertOrThrow(TABLE_MESSAGE_FTS, null, userValues);
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
                result = db.delete(TABLE_MESSAGE_FTS, selection, selectionArgs);
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
                result = db.update(TABLE_MESSAGE_FTS, values, selection, selectionArgs);
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
