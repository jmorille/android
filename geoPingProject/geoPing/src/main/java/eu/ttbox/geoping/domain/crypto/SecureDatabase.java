package eu.ttbox.geoping.domain.crypto;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import android.provider.BaseColumns;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteQueryBuilder;

import java.util.HashMap;

import eu.ttbox.geoping.domain.EncryptionColumns;

public class SecureDatabase {

    public static final String TAG = "SecureDatabase";

    public static final String TABLE_KEYSTORE = "keystore";

    public static class SecureColumns {

        public static final String COL_ID = BaseColumns._ID;
        public static final String COL_ENCRYPTION_PUBKEY = EncryptionColumns.COL_ENCRYPTION_PUBKEY;
        public static final String COL_ENCRYPTION_PRIVKEY = EncryptionColumns.COL_ENCRYPTION_PRIVKEY;
        // Remote
        public static final String COL_ENCRYPTION_REMOTE_PUBKEY = EncryptionColumns.COL_ENCRYPTION_REMOTE_PUBKEY;
        public static final String COL_ENCRYPTION_REMOTE_TIME = EncryptionColumns.COL_ENCRYPTION_REMOTE_TIME;
        public static final String COL_ENCRYPTION_REMOTE_WAY = EncryptionColumns.COL_ENCRYPTION_REMOTE_WAY;

        // All Cols
        public static final String[] ALL_COLS = new String[]{COL_ID, COL_ENCRYPTION_PUBKEY, COL_ENCRYPTION_PRIVKEY //
                , COL_ENCRYPTION_REMOTE_PUBKEY, COL_ENCRYPTION_REMOTE_TIME, COL_ENCRYPTION_REMOTE_WAY //

        };
    }

    private static final String CRITERIA_BY_ENTITY_ID = String.format("%s = ?", SecureColumns.COL_ID);
    private static final HashMap<String, String> mGeoTrackColumnMap = buildGeoTrackColumnMap();


    // ===========================================================
    // Constructors
    // ===========================================================


    private SecureOpenHelper mDatabaseOpenHelper;
    private  String password = "kdkdpdk";


    public SecureDatabase(Context context, String password) {
        this.mDatabaseOpenHelper = new SecureOpenHelper(context);
        this.password= password;
    }

    private static HashMap<String, String> buildGeoTrackColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        // Add Id
        for (String col : SecureColumns.ALL_COLS) {
            map.put(col, col);
        }

        return map;
    }

    // ===========================================================
    // Secured Accessor
    // ===========================================================


    private SQLiteDatabase getReadableDatabase() {
        return mDatabaseOpenHelper.getReadableDatabase(password);
    }

    private SQLiteDatabase getWritableDatabase() {
        return mDatabaseOpenHelper.getWritableDatabase(password);
    }

    // ===========================================================
    // Query
    // ===========================================================


    public Cursor getEntityById(String rowId, String[] projection) {
        String[] selectionArgs = new String[]{rowId};
        return queryEntities(projection, CRITERIA_BY_ENTITY_ID, selectionArgs, null);
    }

    public Cursor queryEntities(String[] projection, String selection, String[] selectionArgs, String order) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_KEYSTORE);
        builder.setProjectionMap(mGeoTrackColumnMap);
        Cursor cursor = builder.query( getReadableDatabase(), projection, selection, selectionArgs, null, null, order);
        return cursor;
    }

    public long insertEntity(ContentValues values) throws SQLException {
        long result = -1;
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.insertOrThrow(TABLE_KEYSTORE, null, values);
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

    public int updateEntity(ContentValues values, String selection, String[] selectionArgs) {
        int result = -1;
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.update(TABLE_KEYSTORE, values, selection, selectionArgs);
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
        SQLiteDatabase db = getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.delete(TABLE_KEYSTORE, selection, selectionArgs);
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
