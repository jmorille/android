package eu.ttbox.geoping.domain.pairing;

import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.telephony.PhoneNumberUtils;

public class PairingDatabase {

    @SuppressWarnings("unused")
    private static final String TAG = "PairingDatabase";

    public static final String TABLE_PAIRING_FTS = "pairingFTS";

    public static class PairingColumns {

        public static final String COL_ID = BaseColumns._ID;
        public static final String COL_NAME = "NAME";
        public static final String COL_PHONE = "PHONE";
        public static final String COL_PHONE_NORMALIZED = "PHONE_NORMALIZED";
        public static final String COL_AUTHORIZE_TYPE = "AUTHORIZE_TYPE";
        public static final String COL_SHOW_NOTIF = "SHOW_NOTIF";
        // All Cols
        public static final String[] ALL_KEYS = new String[] { COL_ID, COL_NAME, COL_PHONE,  COL_AUTHORIZE_TYPE, COL_SHOW_NOTIF };
        // Where Clause
        public static final String SELECT_BY_ENTITY_ID = String.format("%s = ?", "rowid");
        public static final String SELECT_BY_PHONE_NUMBER = String.format("%s = ?", COL_PHONE);
       

    }

    private final PairingOpenHelper mDatabaseOpenHelper;
    private static final HashMap<String, String> mPairingColumnMap = buildUserColumnMap();

    public PairingDatabase(Context context) {
        mDatabaseOpenHelper = new PairingOpenHelper(context);
    }

    private static HashMap<String, String> buildUserColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        // Add Id
        map.put(PairingColumns.COL_ID, "rowid AS " + BaseColumns._ID);
        // Add Identity Column
        for (String col : PairingColumns.ALL_KEYS) {
            if (!col.equals(PairingColumns.COL_ID)) {
                map.put(col, col);
            }
        }
        // Add Suggest Aliases
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, String.format("%s AS %s", PairingColumns.COL_NAME, SearchManager.SUGGEST_COLUMN_TEXT_1));
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_2, String.format("%s AS %s", PairingColumns.COL_PHONE, SearchManager.SUGGEST_COLUMN_TEXT_2));
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
        String selection = PairingColumns.COL_NAME + " MATCH ?";
        String queryString = new StringBuilder(query).append("*").toString();
        String[] selectionArgs = new String[] { queryString };
        return queryEntities(projection, selection, selectionArgs, order);
    }

    public Cursor queryEntities(String[] projection, String selection, String[] selectionArgs, String order) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_PAIRING_FTS);
        builder.setProjectionMap(mPairingColumnMap);
        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, order);
        return cursor;
    }

    public long insertEntity(ContentValues values) throws SQLException {
        long result = -1;
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            parseForPhoneNormalize(values);
            db.beginTransaction();
            try {
                 result = db.insertOrThrow(TABLE_PAIRING_FTS, null, values);
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

    private void parseForPhoneNormalize(ContentValues values) {
        if (values.containsKey(PairingColumns.COL_PHONE)) {
            String phone = values.getAsString(PairingColumns.COL_PHONE);
            String normalizePhone = eu.ttbox.geoping.core.PhoneNumberUtils.normalizeNumber(phone);
            values.put(PairingColumns.COL_PHONE_NORMALIZED, normalizePhone);
        }
    }
    
    public int deleteEntity(String selection, String[] selectionArgs) {
        int result = -1;
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.delete(TABLE_PAIRING_FTS, selection, selectionArgs);
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
            parseForPhoneNormalize(values);
            db.beginTransaction();
            try {
                result = db.update(TABLE_PAIRING_FTS, values, selection, selectionArgs);
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
