package eu.ttbox.geoping.domain.geofence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.ttbox.geoping.domain.geofence.GeoFenceDatabase.CircleGeofenceColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase;
/**
 * <ul>
 * <li>Db version 6 : Geoping 0.1.5 (37)</li>
 * <li>Db version 8 : Geoping 0.1.6 (39)</li>
 * </ul>
 *  
 *
 */
public class GeofenceOpenHelper extends SQLiteOpenHelper {

	public static final String TAG = "GeofenceOpenHelper";

	public static final String DATABASE_NAME = "geoping.db";
	public static final int DATABASE_VERSION = 8;

	// ===========================================================
	// Table
	// ===========================================================

	private static final String CREATE_BDD = "CREATE TABLE " + GeoTrackDatabase.TABLE_TRACK_POINT //
			+ "( " + CircleGeofenceColumns.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT " // 
			+ ", " + CircleGeofenceColumns.COL_REQUEST_ID + " TEXT NOT NULL " //   
			// Location
			+ ", " + CircleGeofenceColumns.COL_LATITUDE_E6 + " INTEGER NOT NULL " //
			+ ", " + CircleGeofenceColumns.COL_LONGITUDE_E6 + " INTEGER NOT NULL " //
			+ ", " + CircleGeofenceColumns.COL_RADIUS + " INTEGER NOT NULL " //  
			+ ", " + CircleGeofenceColumns.COL_TRANSITION + " INTEGER " // 
            + ", " + CircleGeofenceColumns.COL_EXPIRATION + " INTEGER NOT NULL " // 
			 
			+ " );";

	// ===========================================================
	// Index
	// ===========================================================

	 

	// ===========================================================
	// Constructors
	// ===========================================================

	public GeofenceOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Log.d(TAG, CREATE_BDD);
		db.execSQL(CREATE_BDD);
		// Log.d(TAG, CREATE_INDEX_AK);
//		db.execSQL(CREATE_INDEX_AK);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
	    // Update
//		db.execSQL("DROP INDEX IF EXISTS " + INDEX_TRACK_POINT_AK + ";");
		db.execSQL("DROP TABLE IF EXISTS " + GeoTrackDatabase.TABLE_TRACK_POINT + ";");
		onCreate(db);
	}

}
