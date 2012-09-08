package eu.ttbox.geoping.domain.person;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;

public class PersonDatabase {

    @SuppressWarnings("unused")
    private static final String TAG = "PersonDatabase";

    public static final String TABLE_PERSON_FTS = "personFTS";

    public static class PersonColumns {

        public static final String KEY_ID = BaseColumns._ID;
        public static final String KEY_NAME = "NAME";
        public static final String KEY_PHONE = "PHONE";

        public static final String[] ALL_KEYS = new String[] { KEY_ID, KEY_NAME, KEY_PHONE };

    }
    
    private final PersonOpenHelper mDatabaseOpenHelper;
    private static final HashMap<String, String> mPersonColumnMap = buildUserColumnMap();
    
    public PersonDatabase(Context context) {
        mDatabaseOpenHelper = new PersonOpenHelper(context);
    }
  
    private static HashMap<String, String> buildUserColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        // Add Id
        map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
        // Add Identity Column
        for (String col : PersonColumns.ALL_KEYS) {
            if (!col.equals(PersonColumns.KEY_ID)) {
                map.put(col, col);
            }
        }
        // Add Suggest Aliases
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, String.format("%s AS %s", PersonColumns.KEY_NAME,  SearchManager.SUGGEST_COLUMN_TEXT_1));
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_2, String.format("%s AS %s", PersonColumns.KEY_PHONE, SearchManager.SUGGEST_COLUMN_TEXT_2));
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        // Add Other Aliases
        return map;
    }
    
    public Cursor getPerson(String rowId, String[] columns) {
        String selection = "rowid = ?";
        String[] selectionArgs = new String[] { rowId };
        return queryPerson(selection, selectionArgs, columns, null);
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
    public Cursor getPersonMatches(String query, String[] columns, String order) {
        String selection = PersonColumns.KEY_NAME + " MATCH ?";
        String queryString = new StringBuilder(query).append("*").toString();
        String[] selectionArgs = new String[] { queryString };
        return queryPerson(selection, selectionArgs, columns, order);
    }
    
    
    public Cursor queryPerson(String selection, String[] selectionArgs, String[] columns, String order) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_PERSON_FTS);
        builder.setProjectionMap(mPersonColumnMap);
        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns, selection, selectionArgs, null, null, order);
        // Manage Cursor
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }

    public long insertPerson( ContentValues userValues) throws SQLException {
        long result = -1;
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
         result = db.insertOrThrow(TABLE_PERSON_FTS, null, userValues);
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
                result = db.delete(TABLE_PERSON_FTS, selection, selectionArgs);
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
                result = db.update(TABLE_PERSON_FTS, values, selection, selectionArgs);
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
