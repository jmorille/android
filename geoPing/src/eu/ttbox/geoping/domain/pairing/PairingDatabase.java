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
import android.text.TextUtils;
import eu.ttbox.geoping.core.PhoneNumberUtils;

public class PairingDatabase {

    @SuppressWarnings("unused")
    private static final String TAG = "PairingDatabase";

    public static final String TABLE_PAIRING_FTS = "pairingFTS";

    public static class PairingColumns {

        public static final String COL_ID = BaseColumns._ID;
        public static final String COL_NAME = "NAME";
        public static final String COL_PHONE = "PHONE";
        public static final String COL_PHONE_NORMALIZED = "PHONE_NORMALIZED";
        public static final String COL_PHONE_MIN_MATCH = "PHONE_MIN_MATCH";
        public static final String COL_AUTHORIZE_TYPE = "AUTHORIZE_TYPE";
        public static final String COL_SHOW_NOTIF = "SHOW_NOTIF";
        public static final String COL_PAIRING_TIME = "COL_PAIRING_TIME";

        // Notification
        public static final String COL_NOTIF_SHUTDOWN = "COL_NOTIF_SHUTDOWN";
        public static final String COL_NOTIF_BATTERY_LOW = "COL_NOTIF_BATTERY_LOW";
        public static final String COL_NOTIF_SIM_CHANGE = "COL_NOTIF_SIM_CHANGE";
        public static final String COL_NOTIF_PHONE_CALL = "COL_NOTIF_PHONE_CALL";
        public static final String COL_NOTIF_PHONE_RECEIVE = "COL_NOTIF_PHONE_RECEIVE";
        
        // All Cols
        public static final String[] ALL_COLS = new String[] { COL_ID, COL_NAME, COL_PHONE, COL_PHONE_NORMALIZED, COL_PHONE_MIN_MATCH, COL_AUTHORIZE_TYPE, COL_SHOW_NOTIF, COL_PAIRING_TIME };
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
        for (String col : PairingColumns.ALL_COLS) {
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
        fillNormalizedNumber(values);
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
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

    public Cursor searchForPhoneNumber(String number, String[] _projection, String pSelection, String[] pSelectionArgs, String sortOrder) {
        String[] projection = _projection == null ? PairingColumns.ALL_COLS : _projection;
        // Normalise For search
        String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
        String minMatch = PhoneNumberUtils.toCallerIDMinMatch(normalizedNumber);
        // Prepare Query
        String selection = null;
        String[] selectionArgs = null;
        if (TextUtils.isEmpty(pSelection)) {
            selection = String.format("%s = ?", PairingColumns.COL_PHONE_MIN_MATCH);
            selectionArgs = new String[] { minMatch };
        } else {
            selection = String.format("%s = ? and (%s)", PairingColumns.COL_PHONE_MIN_MATCH, pSelection);
            int pSelectionArgSize = pSelectionArgs.length;
            selectionArgs = new String[pSelectionArgSize + 1];
            System.arraycopy(pSelectionArgs, 0, selectionArgs, 1, pSelectionArgSize);
            selectionArgs[0] = minMatch;
        }
        return queryEntities(projection, selection, selectionArgs, sortOrder);
    }

    private void fillNormalizedNumber(ContentValues values) {
        // No NUMBER? Also ignore NORMALIZED_NUMBER
        if (!values.containsKey(PairingColumns.COL_PHONE)) {
            values.remove(PairingColumns.COL_PHONE_NORMALIZED);
            values.remove(PairingColumns.COL_PHONE_MIN_MATCH);
            return;
        }

        // NUMBER is given. Try to extract NORMALIZED_NUMBER from it, unless it
        // is also given
        String number = values.getAsString(PairingColumns.COL_PHONE);
        if (!TextUtils.isEmpty(number)) {
            // final String newNumberE164 =
            // PhoneNumberUtils.formatNumberToE164(number,
            // mDbHelper.getCurrentCountryIso());
            String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
            if (!TextUtils.isEmpty(normalizedNumber)) {
                String minMatch = PhoneNumberUtils.toCallerIDMinMatch(normalizedNumber);
                values.put(PairingColumns.COL_PHONE_NORMALIZED, normalizedNumber);
                values.put(PairingColumns.COL_PHONE_MIN_MATCH, minMatch);
            }
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
        fillNormalizedNumber(values);
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
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
