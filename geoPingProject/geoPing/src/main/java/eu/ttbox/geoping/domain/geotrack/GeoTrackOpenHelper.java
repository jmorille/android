package eu.ttbox.geoping.domain.geotrack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
/**
 * <ul>
 * <li>Db version 6 : Geoping 0.1.5 (37)</li>
 * <li>Db version 8 : Geoping 0.1.6 (39)</li>
 * </ul>
 *  
 *
 */
public class GeoTrackOpenHelper extends SQLiteOpenHelper {

	public static final String TAG = "GeoTrackOpenHelper";

	public static final String DATABASE_NAME = "geoping.db";
	public static final int DATABASE_VERSION = 8;

	// ===========================================================
	// Table
	// ===========================================================

	private static final String CREATE_BDD = "CREATE TABLE " + GeoTrackDatabase.TABLE_TRACK_POINT //
			+ "( " + GeoTrackColumns.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " //
			+ ", " + GeoTrackColumns.COL_PERSON_ID + " INTEGER   NULL " //  
			+ ", " + GeoTrackColumns.COL_REQUESTER_PERSON + " TEXT " //  
			+ ", " + GeoTrackColumns.COL_PHONE + " TEXT NOT NULL " //  
			+ ", " + GeoTrackColumns.COL_PHONE_MIN_MATCH + " TEXT NOT NULL " //
			+ ", " + GeoTrackColumns.COL_TIME_MIDNIGHT + " INTEGER NOT NULL " //
			// Location
			+ ", " + GeoTrackColumns.COL_TIME + " INTEGER NOT NULL " //
			+ ", " + GeoTrackColumns.COL_PROVIDER + " TEXT NOT NULL " //
			+ ", " + GeoTrackColumns.COL_LATITUDE_E6 + " INTEGER NOT NULL " //
			+ ", " + GeoTrackColumns.COL_LONGITUDE_E6 + " INTEGER NOT NULL " //
			+ ", " + GeoTrackColumns.COL_ACCURACY + " INTEGER NOT NULL " //
			+ ", " + GeoTrackColumns.COL_ALTITUDE + " INTEGER " //
			+ ", " + GeoTrackColumns.COL_BEARING + " INTEGER " //
			+ ", " + GeoTrackColumns.COL_SPEED + " INTEGER " //
			+ ", " + GeoTrackColumns.COL_ADDRESS + " TEXT " //
			// Other
			+ ", " + GeoTrackColumns.COL_BATTERY_LEVEL + " INTEGER " //
			// Event
			+ ", " + GeoTrackColumns.COL_EVT_TIME + " INTEGER " //
			+ ", " + GeoTrackColumns.COL_EVT_TYPE + " TEXT " // 
			+ " );";

	// ===========================================================
	// Index
	// ===========================================================

	private static final String INDEX_TRACK_POINT_AK = "IDX_TRACKPOINT_AK";
	private static final String CREATE_INDEX_AK = "CREATE INDEX IF NOT EXISTS " + INDEX_TRACK_POINT_AK + " on " + GeoTrackDatabase.TABLE_TRACK_POINT + "(" //
			+ GeoTrackColumns.COL_PHONE + ", " //
			+ GeoTrackColumns.COL_TIME //
			+ ");";

	// ===========================================================
	// Constructors
	// ===========================================================

	public GeoTrackOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Log.d(TAG, CREATE_BDD);
		db.execSQL(CREATE_BDD);
		// Log.d(TAG, CREATE_INDEX_AK);
		db.execSQL(CREATE_INDEX_AK);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
	    // Update
		db.execSQL("DROP INDEX IF EXISTS " + INDEX_TRACK_POINT_AK + ";");
		db.execSQL("DROP TABLE IF EXISTS " + GeoTrackDatabase.TABLE_TRACK_POINT + ";");
		onCreate(db);
	}

}
