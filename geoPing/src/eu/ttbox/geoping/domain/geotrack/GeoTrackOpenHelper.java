package eu.ttbox.geoping.domain.geotrack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;

public class GeoTrackOpenHelper extends SQLiteOpenHelper {
    
    public static final String TAG = "GeoTrackOpenHelper";
    
    public static final String DATABASE_NAME = "geoping.db";
    public static final int DATABASE_VERSION = 6;

    // ===========================================================
    // Table
    // ===========================================================

    private static final String CREATE_BDD = new StringBuffer("CREATE TABLE ").append(GeoTrackDatabase.TABLE_TRACK_POINT).append(" (")//
            .append(GeoTrackColumns.COL_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")//
            .append(GeoTrackColumns.COL_PERSON_ID).append(" INTEGER   NULL, ")// /
            .append(GeoTrackColumns.COL_REQUESTER_PERSON).append(" TEXT, ")// /
            .append(GeoTrackColumns.COL_PHONE).append(" TEXT NOT NULL, ")// /
            .append(GeoTrackColumns.COL_PHONE_MIN_MATCH).append(" TEXT NOT NULL, ")// /
            .append(GeoTrackColumns.COL_PROVIDER).append(" TEXT NOT NULL, ")// /
            .append(GeoTrackColumns.COL_TIME).append(" INTEGER NOT NULL, ")// /
            .append(GeoTrackColumns.COL_TIME_MIDNIGHT).append(" INTEGER NOT NULL, ")// /
            .append(GeoTrackColumns.COL_LATITUDE_E6).append(" INTEGER NOT NULL, ")// /
            .append(GeoTrackColumns.COL_LONGITUDE_E6).append(" INTEGER NOT NULL, ")// /
            .append(GeoTrackColumns.COL_ACCURACY).append(" INTEGER NOT NULL, ")// /
            .append(GeoTrackColumns.COL_ALTITUDE).append(" INTEGER, ")// /
            .append(GeoTrackColumns.COL_BEARING).append(" INTEGER, ")// /
            .append(GeoTrackColumns.COL_SPEED).append(" INTEGER, ")// /
            .append(GeoTrackColumns.COL_BATTERY_LEVEL).append(" INTEGER, ")// /
            .append(GeoTrackColumns.COL_ADDRESS).append(" TEXT ")// /
            .append(" );").toString();

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
//        Log.d(TAG, CREATE_BDD);
        db.execSQL(CREATE_BDD);
//        Log.d(TAG, CREATE_INDEX_AK);
         db.execSQL(CREATE_INDEX_AK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { 
        db.execSQL("DROP INDEX IF EXISTS " + INDEX_TRACK_POINT_AK + ";");
        db.execSQL("DROP TABLE IF EXISTS " + GeoTrackDatabase.TABLE_TRACK_POINT + ";"); 
        onCreate(db);
    }

}
