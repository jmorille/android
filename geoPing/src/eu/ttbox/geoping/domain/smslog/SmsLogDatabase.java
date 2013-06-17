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
import android.text.TextUtils;
import android.util.Log;
import eu.ttbox.geoping.core.PhoneNumberUtils;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;

public class SmsLogDatabase {
 
    private static final String TAG = "SmsLogDatabase";

    public static final String TABLE_SMSLOG_FTS = "personFTS";

    public static final String SMSLOG_SORT_DEFAULT = String.format("%s DESC", SmsLogColumns.COL_TIME );

	
    public static class SmsLogColumns {

        public static final String COL_ID = BaseColumns._ID;
        public static final String COL_ACTION = "ACTION";
        public static final String COL_PHONE = "PHONE"; 
        public static final String COL_PHONE_MIN_MATCH = "PHONE_MIN_MATCH";
        public static final String COL_MESSAGE = "MSG";
        public static final String COL_MESSAGE_PARAMS = "MSG_PARAMS";
        public static final String COL_SMSLOG_TYPE = "SMSLOG_TYPE"; // @see SmsLogTypeEnum
        public static final String COL_SMS_SIDE = "SMS_SIDE";  // @see SmsLogSideEnum
        public static final String COL_TIME = "TIME";
        public static final String COL_PARENT_ID = "PARENT_ID";
        public static final String COL_SMS_WEIGHT = "SMS_WEIGHT";
        // Acknowledge
        public static final String COL_IS_SEND_TIME = "IS_SEND_TIME";
        public static final String COL_IS_DELIVERY_TIME = "IS_DELIVERY_TIME";
        // Geofence
        public static final String COL_REQUEST_ID = "REQUEST_ID";

        // All Cols
        public static final String[] ALL_COLS = new String[] { //
            COL_ID, COL_TIME, COL_ACTION, COL_PHONE, COL_PHONE_MIN_MATCH,  COL_SMSLOG_TYPE,  COL_MESSAGE , COL_MESSAGE_PARAMS  //
            ,COL_SMS_SIDE, COL_SMS_WEIGHT,COL_PARENT_ID //
            , COL_IS_SEND_TIME, COL_IS_DELIVERY_TIME //Acknowledge
            , COL_REQUEST_ID // Geofence
        };
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
        for (String col : SmsLogColumns.ALL_COLS) {
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
        String selection = SmsLogColumns.COL_ACTION + " = ?";
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
    

    public Cursor searchForPhoneNumber(String number, String[] _projection, String pSelection, String[] pSelectionArgs, String sortOrder) {
        String[] projection = _projection == null ? SmsLogColumns.ALL_COLS : _projection;
        // Normalise For search
        String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
        String minMatch = PhoneNumberUtils.toCallerIDMinMatch(normalizedNumber);
        // Prepare Query
        String selection = null;
        String[] selectionArgs = null;
        if (TextUtils.isEmpty(pSelection)) {
            selection = String.format("%s = ?", SmsLogColumns.COL_PHONE_MIN_MATCH);
            selectionArgs = new String[] { minMatch };
            Log.d(TAG, "selection : " + selection);
            Log.d(TAG, "selectionArgs :  " + selectionArgs[0] );
        } else {
            selection = String.format("%s = ? and (%s)", SmsLogColumns.COL_PHONE_MIN_MATCH, pSelection);
            int pSelectionArgSize = pSelectionArgs.length;
            selectionArgs = new String[pSelectionArgSize + 1];
            System.arraycopy(pSelectionArgs, 0, selectionArgs, 1, pSelectionArgSize);
            selectionArgs[0] = minMatch;
            Log.d(TAG, "selection : " + selection);
            Log.d(TAG, "selectionArgs :  " + selectionArgs[0]+ ", " +  selectionArgs[1]);
        }
        return queryEntities(projection, selection, selectionArgs, sortOrder);
    }

    private void fillNormalizedNumber(ContentValues values) {
        // No NUMBER? Also ignore NORMALIZED_NUMBER
        if (!values.containsKey(SmsLogColumns.COL_PHONE)) { 
            values.remove(SmsLogColumns.COL_PHONE_MIN_MATCH);
            return;
        }

        // NUMBER is given. Try to extract NORMALIZED_NUMBER from it, unless it
        // is also given
        String number = values.getAsString(SmsLogColumns.COL_PHONE);

        // final String newNumberE164 =
        // PhoneNumberUtils.formatNumberToE164(number,
        // mDbHelper.getCurrentCountryIso());
        if (!TextUtils.isEmpty(number)) {
            String normalizedNumber = PhoneNumberUtils.normalizeNumber(number);
            if (!TextUtils.isEmpty(normalizedNumber)) {
                String minMatch = PhoneNumberUtils.toCallerIDMinMatch(normalizedNumber); 
                values.put(SmsLogColumns.COL_PHONE_MIN_MATCH, minMatch);
            }
        }

    }

    
    public long insertEntity(ContentValues values) throws SQLException {
        long result = -1;
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            fillNormalizedNumber(values);
            db.beginTransaction();
            try {
                result = db.insertOrThrow(TABLE_SMSLOG_FTS, null, values);
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
    
//    private void manageContentValues(ContentValues values) {
//        String action = values.getAsString(SmsLogColumns.COL_ACTION);
//        if (action!=null && action)
//    }
    

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
            fillNormalizedNumber(values);
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
