package eu.ttbox.geoping.domain.pairing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.ttbox.geoping.domain.core.UpgradeDbHelper;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;

public class PairingOpenHelper extends SQLiteOpenHelper {

	private static final String TAG = "PairingOpenHelper";

	public static final String DATABASE_NAME = "pairing.db";
	public static final int DATABASE_VERSION = 6;

	// ===========================================================
	// Table
	// ===========================================================

	private static final String FTS_TABLE_CREATE_PAIRING_V6 = "CREATE TABLE " + PairingDatabase.TABLE_PAIRING_FTS //
			+ "( " + PairingColumns.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT"//
			+ ", " + PairingColumns.COL_NAME + " TEXT" //
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
	

    // ===========================================================
    // Index
    // ===========================================================

    private static final String INDEX_PAIRING_PHONE_AK = "IDX_PAIRING_PHONE_AK";
    private static final String CREATE_INDEX_PAIRING_PHONE_AK = "CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_PAIRING_PHONE_AK + " on " + PairingDatabase.TABLE_PAIRING_FTS + "(" //
            + PairingColumns.COL_PHONE_MIN_MATCH  
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
		mDatabase.execSQL(FTS_TABLE_CREATE_PAIRING);
		mDatabase.execSQL(CREATE_INDEX_PAIRING_PHONE_AK);
		// new PairingDbBootstrap(mHelperContext, mDatabase).loadDictionary();
	}
	
	private void onLocalDrop(SQLiteDatabase db) {
	    db.execSQL("DROP INDEX IF EXISTS " + INDEX_PAIRING_PHONE_AK  );
	    db.execSQL("DROP TABLE IF EXISTS " + PairingDatabase.TABLE_PAIRING_FTS); 
	}
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		// Do a memory copy of old table
		ArrayList<ContentValues> oldRows = null;
		if (oldVersion <= 5) {
			String[] stringColums = new String[] { //
			PairingColumns.COL_NAME, PairingColumns.COL_PHONE, PairingColumns.COL_PHONE_NORMALIZED, PairingColumns.COL_PHONE_MIN_MATCH //
			};
			String[] intColums = new String[] { //
			PairingColumns.COL_AUTHORIZE_TYPE, PairingColumns.COL_SHOW_NOTIF // init
			};
			String[] longColums = new String[] { PairingColumns.COL_PAIRING_TIME };
			oldRows = UpgradeDbHelper.copyTable(db, "pairingFTS", stringColums, intColums, longColums);
			// Drop All Table
			db.execSQL("DROP TABLE IF EXISTS pairingFTS");
		}
		// Create the new Table
		Log.d(TAG, "Upgrading database : DROP TABLE  : " + PairingDatabase.TABLE_PAIRING_FTS);
		onLocalDrop(db);
		onCreate(db);
		// Insert data in new table
		if (oldVersion <= 5 && oldRows != null) {
			List<String> validColumns = Arrays.asList(PairingColumns.ALL_COLS);
			UpgradeDbHelper.insertOldRowInNewTable(db, oldRows, PairingDatabase.TABLE_PAIRING_FTS, validColumns);
		}
	}

}
