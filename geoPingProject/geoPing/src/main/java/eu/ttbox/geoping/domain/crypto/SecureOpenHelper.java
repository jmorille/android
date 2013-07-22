package eu.ttbox.geoping.domain.crypto;

import android.content.Context;
import android.util.Log;

import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;



public class SecureOpenHelper extends SQLiteOpenHelper {


    public static final String TAG = "CryptoOpenHelper";

    public static final String DATABASE_NAME = "secure.db";
    public static final int DATABASE_VERSION = 1;

    // ===========================================================
    // Table
    // ===========================================================


    private static final String CREATE_BDD = "CREATE TABLE " + SecureDatabase.TABLE_KEYSTORE //
            + "( " + SecureDatabase.SecureColumns.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " //
            // Encryption
            + ", " + SecureDatabase.SecureColumns.COL_ENCRYPTION_PUBKEY + " TEXT"//
            + ", " + SecureDatabase.SecureColumns.COL_ENCRYPTION_PRIVKEY + " TEXT"//
            + ", " + SecureDatabase.SecureColumns.COL_ENCRYPTION_REMOTE_PUBKEY + " TEXT"//
            + ", " + SecureDatabase.SecureColumns.COL_ENCRYPTION_REMOTE_TIME + " INTEGER"//
            + ", " + SecureDatabase.SecureColumns.COL_ENCRYPTION_REMOTE_WAY + " TEXT"//
            + " );";


    // ===========================================================
    // Index
    // ===========================================================



  //  private static final String INDEX_TRACK_POINT_AK = "IDX_TRACKPOINT_AK";
  //  private static final String CREATE_INDEX_AK = "CREATE INDEX IF NOT EXISTS " + INDEX_TRACK_POINT_AK + " on " + GeoTrackDatabase.TABLE_TRACK_POINT + "(" //
   //         + GeoTrackDatabase.GeoTrackColumns.COL_PHONE + ", " //
    //        + GeoTrackDatabase.GeoTrackColumns.COL_TIME //
    //        + ");";


    // ===========================================================
    // Constructors
    // ===========================================================

    public SecureOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Log.d(TAG, CREATE_BDD);
        db.execSQL(CREATE_BDD);
        // Log.d(TAG, CREATE_INDEX_AK);
     //   db.execSQL(CREATE_INDEX_AK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        // Update
    //    db.execSQL("DROP INDEX IF EXISTS " + INDEX_TRACK_POINT_AK + ";");
        db.execSQL("DROP TABLE IF EXISTS " + SecureDatabase.TABLE_KEYSTORE + ";");
        onCreate(db);
    }



}
