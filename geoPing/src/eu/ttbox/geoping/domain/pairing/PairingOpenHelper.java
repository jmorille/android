package eu.ttbox.geoping.domain.pairing;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;

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
			+ ", " + PairingColumns.COL_NOTIF_PHONE_RECEIVE + " INTEGER"//
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
			// Temp new
			+ ", " + PairingColumns.COL_NOTIF_SHUTDOWN //
			+ ", " + PairingColumns.COL_NOTIF_BATTERY_LOW //
			+ ", " + PairingColumns.COL_NOTIF_SIM_CHANGE //
			+ ", " + PairingColumns.COL_NOTIF_PHONE_CALL //
			+ ", " + PairingColumns.COL_NOTIF_PHONE_RECEIVE //
			+ ");";

	private static final String FTS_TABLE_CREATE_PAIRING = FTS_TABLE_CREATE_PAIRING_V6;
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
		// new PairingDbBootstrap(mHelperContext, mDatabase).loadDictionary();
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
		// Do a memory copy of old table
		ArrayList<ContentValues> oldRows = null;
		if (oldVersion == 5) {
			String[] stringColums = new String[] { //
			PairingColumns.COL_NAME, PairingColumns.COL_PHONE, PairingColumns.COL_PHONE_NORMALIZED, PairingColumns.COL_PHONE_MIN_MATCH //
			};
			String[] intColums = new String[] { //
			PairingColumns.COL_AUTHORIZE_TYPE, PairingColumns.COL_SHOW_NOTIF // init
			};
			String[] longColums = new String[] { PairingColumns.COL_PAIRING_TIME };
			oldRows = copyTable(db, "pairingFTS", PairingDatabase.TABLE_PAIRING_FTS, stringColums, intColums, longColums);
		}
		// Create the new Table
		db.execSQL("DROP TABLE IF EXISTS " + PairingDatabase.TABLE_PAIRING_FTS);
		onCreate(db);
		// Insert data in new table
		if (oldRows != null && !oldRows.isEmpty()) {
			try {
				ContentResolver cr = context.getContentResolver();
				for (ContentValues values : oldRows) {
					cr.insert(PairingProvider.Constants.CONTENT_URI, values);
				}
			} catch (RuntimeException e) {
				Log.e(TAG, "Error in inserting the copy in Table : " + e.getMessage(), e);
			}
		}
	}

	private ArrayList<ContentValues> copyTable(SQLiteDatabase db, String oldTable, String table, String[] stringColums, String[] intColums, String[] longColums) {
		ArrayList<ContentValues> allRows = null;
		try {
			// Init Columns Arrays
			int stringColumSize = stringColums.length;
			int intColumSize = intColums.length;
			int longColumSize = longColums.length;
			String[] columns = new String[stringColumSize + intColumSize + longColumSize];
			int dstPos = 0;
			for (String[] colArray : new String[][] { stringColums, intColums, longColums }) {
				System.arraycopy(stringColums, 0, columns, dstPos, colArray.length);
				dstPos += colArray.length;
			}
			// Do copye Table
			Cursor cursor = db.query(table, columns, null, null, null, null, null);
			// ContentResolver cr = context.getContentResolver();
			allRows = new ArrayList<ContentValues>(cursor.getCount());
			while (cursor.moveToNext()) {
				Log.d(TAG, "Read pairing ");
				ContentValues values = new ContentValues(columns.length);
				// Read String
				for (String colName : stringColums) {
					String colValue = cursor.getString(cursor.getColumnIndex(colName));
					values.put(colName, colValue);
				}
				// Read Int
				for (String colName : intColums) {
					int colValue = cursor.getInt(cursor.getColumnIndex(colName));
					values.put(colName, colValue);
				}
				// Read Long
				for (String colName : longColums) {
					long colValue = cursor.getLong(cursor.getColumnIndex(colName));
					values.put(colName, colValue);
				}
				// Insert Data
				allRows.add(values);
				// cr.insert(PairingProvider.Constants.CONTENT_URI, values);
			}
		} catch (RuntimeException e) {
			Log.e(TAG, "Error in memory copy of Table " + oldTable + " : " + e.getMessage(), e);
			// allRows = null;
		}
		return allRows;
	}
}
