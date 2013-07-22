package eu.ttbox.geoping.domain.pairing;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.ttbox.geoping.domain.core.UpgradeDbHelper;
import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase.GeoFenceColumns;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
/**
 * <ul>
 * <li>Db version 5 : Geoping 0.1.5 (37)</li>
 * <li>Db version 6 : Geoping 0.1.6 (39)</li>
 * <li>Db version 7 : Geoping 0.2.0 (52)</li>
 * <li>Db version 8 : Geoping 0.2.2 (??)</li>
 * </ul>
 *  
 *
 */
public class PairingOpenHelper extends SQLiteOpenHelper {

    private static final String TAG = "PairingOpenHelper";

    public static final String DATABASE_NAME = "pairing.db";
    public static final int DATABASE_VERSION = 8;

    // ===========================================================
    // Table
    // ===========================================================

    private static final String FTS_TABLE_CREATE_PAIRING_V6 = "CREATE TABLE " + PairingDatabase.TABLE_PAIRING_FTS //
            + "( " + PairingColumns.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"//
            + ", " + PairingColumns.COL_NAME + " TEXT" //
            + ", " + PairingColumns.COL_PERSON_UUID + " TEXT" //
            + ", " + PairingColumns.COL_EMAIL + " TEXT" //
            // Phone
            + ", " + PairingColumns.COL_PHONE + " TEXT" //
            + ", " + PairingColumns.COL_PHONE_NORMALIZED + " TEXT" //
            + ", " + PairingColumns.COL_PHONE_MIN_MATCH + " TEXT" //
            + ", " + PairingColumns.COL_AUTHORIZE_TYPE + " INTEGER"//
            + ", " + PairingColumns.COL_SHOW_NOTIF + " INTEGER"//
            + ", " + PairingColumns.COL_PAIRING_TIME + " INTEGER"//
            + ", " + PairingColumns.COL_NOTIF_SHUTDOWN + " INTEGER"//
            + ", " + PairingColumns.COL_NOTIF_BATTERY_LOW + " INTEGER"//
            + ", " + PairingColumns.COL_NOTIF_SIM_CHANGE + " INTEGER"//
            + ", " + PairingColumns.COL_NOTIF_PHONE_CALL + " INTEGER"//
            // Encryption
            + ", " + PairingColumns.COL_ENCRYPTION_PUBKEY + " TEXT"//
            + ", " + PairingColumns.COL_ENCRYPTION_PRIVKEY + " TEXT"//
            + ", " + PairingColumns.COL_ENCRYPTION_REMOTE_PUBKEY + " TEXT"//
            + ", " + PersonColumns.COL_ENCRYPTION_REMOTE_TIME + " INTEGER"//
            + ", " + PersonColumns.COL_ENCRYPTION_REMOTE_WAY + " TEXT"//
            + ");";

    private static final String FTS_TABLE_CREATE_PAIRING_V5 = "CREATE VIRTUAL TABLE pairingFTS " + // PairingDatabase.TABLE_PAIRING_FTS
            " USING fts3 " //
            + "( " + PairingColumns.COL_NAME //
            + ", " + PairingColumns.COL_PHONE //
            + ", " + PairingColumns.COL_PHONE_NORMALIZED //
            + ", " + PairingColumns.COL_PHONE_MIN_MATCH //
            + ", " + PairingColumns.COL_AUTHORIZE_TYPE //
            + ", " + PairingColumns.COL_SHOW_NOTIF //
            + ", " + PairingColumns.COL_PAIRING_TIME //
            + ");";

    private static final String FTS_TABLE_CREATE_PAIRING = FTS_TABLE_CREATE_PAIRING_V6;

    // GeoFence
    private static final String CREATE_GEOFENCE_TABLE = "CREATE TABLE " + GeoFenceDatabase.TABLE_GEOFENCE //
            + "( " + GeoFenceColumns.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " // 
            + ", " + GeoFenceColumns.COL_REQUEST_ID + " TEXT NOT NULL " //   
            + ", " + GeoFenceColumns.COL_NAME + " TEXT " //   
            // Location
            + ", " + GeoFenceColumns.COL_LATITUDE_E6 + " INTEGER NOT NULL " //
            + ", " + GeoFenceColumns.COL_LONGITUDE_E6 + " INTEGER NOT NULL " //
            + ", " + GeoFenceColumns.COL_RADIUS + " INTEGER NOT NULL " //  
            // Config
            + ", " + GeoFenceColumns.COL_TRANSITION + " INTEGER NOT NULL " // 
            + ", " + GeoFenceColumns.COL_EXPIRATION_DATE + " INTEGER NOT NULL " //
            // Address
            + ", " + GeoFenceColumns.COL_ADDRESS  + " TEXT " //
            // Tracking Info
            + ", " + GeoFenceColumns.COL_VERSION_UPDATE_DATE + " INTEGER NOT NULL " //
            + " );";

    // ===========================================================
    // Index
    // ===========================================================

    private static final String INDEX_PAIRING_PHONE_AK = "IDX_PAIRING_PHONE_AK";
    private static final String CREATE_INDEX_PAIRING_PHONE_AK = "CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_PAIRING_PHONE_AK + " on " + PairingDatabase.TABLE_PAIRING_FTS + "(" //
            + PairingColumns.COL_PHONE_MIN_MATCH + ");";

    // GeoFence
    private static final String INDEX_GEOFENCE_REQUEST_ID = "IDX_GEOFENCE_REQUEST_ID";
    private static final String CREATE_INDEX_GEOFENCE_REQUEST_ID = "CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_GEOFENCE_REQUEST_ID + " on " + GeoFenceDatabase.TABLE_GEOFENCE + "(" //
            + GeoFenceColumns.COL_REQUEST_ID  
            + ");";
     

    
    // ===========================================================
    // Constructors
    // ===========================================================

    private SQLiteDatabase mDatabase;
    private Context context;

    PairingOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mDatabase = db;
        // Table
        mDatabase.execSQL(FTS_TABLE_CREATE_PAIRING);
        db.execSQL(CREATE_GEOFENCE_TABLE);
        // Index
        mDatabase.execSQL(CREATE_INDEX_PAIRING_PHONE_AK);
        db.execSQL(CREATE_INDEX_GEOFENCE_REQUEST_ID);
        // new PairingDbBootstrap(mHelperContext, mDatabase).loadDictionary();
    }

    private void onLocalDrop(SQLiteDatabase db) {
        // Index
        db.execSQL("DROP INDEX IF EXISTS " + INDEX_PAIRING_PHONE_AK);
        db.execSQL("DROP INDEX IF EXISTS " + INDEX_GEOFENCE_REQUEST_ID + ";");
        // Table
        db.execSQL("DROP TABLE IF EXISTS " + PairingDatabase.TABLE_PAIRING_FTS);
        db.execSQL("DROP TABLE IF EXISTS " + GeoFenceDatabase.TABLE_GEOFENCE  + ";");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        // Read previous values
        // ----------------------
        ArrayList<ContentValues> oldPairingRows = null;
        if (oldVersion <= 5) {
            String[] stringColums = new String[] { //
            PairingColumns.COL_NAME, PairingColumns.COL_PHONE, PairingColumns.COL_PHONE_NORMALIZED, PairingColumns.COL_PHONE_MIN_MATCH //
            };
            String[] intColums = new String[] { //
            PairingColumns.COL_AUTHORIZE_TYPE, PairingColumns.COL_SHOW_NOTIF // init
            };
            String[] longColums = new String[] { PairingColumns.COL_PAIRING_TIME };
            oldPairingRows = UpgradeDbHelper.copyTable(db, "pairingFTS", stringColums, intColums, longColums);
            // Drop All Table
            db.execSQL("DROP TABLE IF EXISTS pairingFTS");
        } else {
            oldPairingRows = UpgradeDbHelper.copyTable(db, PairingDatabase.TABLE_PAIRING_FTS, PairingColumns.ALL_COLS, new String[0], new String[0]);
         }
        // Create the new Table
        // ----------------------
        onLocalDrop(db);
        onCreate(db);
        Log.i(TAG, "Upgrading database : Create TABLE  : " + PairingDatabase.TABLE_PAIRING_FTS);

        // Insert data in new table
        // ----------------------
        if (oldPairingRows != null && !oldPairingRows.isEmpty()) {
            List<String> validColumns = Arrays.asList(PairingColumns.ALL_COLS);
            UpgradeDbHelper.insertOldRowInNewTable(db, oldPairingRows, PairingDatabase.TABLE_PAIRING_FTS, validColumns);
        }
    }

}
