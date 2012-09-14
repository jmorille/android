package eu.ttbox.geoping.domain.geotrack;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;

public class GeoTrackOpenHelper extends SQLiteOpenHelper {
    

    public static final String DATABASE_NAME = "smstracking.db";
    public static final int DATABASE_VERSION = 1;
    
    private static final String CREATE_BDD = new StringBuffer("CREATE TABLE ").append(GeoTrackDatabase.TABLE_TRACK_POINT).append(" (")//
            .append(GeoTrackColumns.COL_ID).append(" INTEGER PRIMARY KEY AUTOINCREMENT, ")//
            .append(GeoTrackColumns.COL_USERID).append(" TEXT NOT NULL, ")// /
            .append(GeoTrackColumns.COL_PROVIDER).append(" TEXT NOT NULL, ")// /
            .append(GeoTrackColumns.COL_TIME).append(" INTEGER NOT NULL, ")// /
            .append(GeoTrackColumns.COL_LATITUDE_E6).append(" INTEGER NOT NULL, ")// /
            .append(GeoTrackColumns.COL_LONGITUDE_E6).append(" INTEGER NOT NULL, ")// /
            .append(GeoTrackColumns.COL_ACCURACY).append(" REAL NOT NULL, ")// /
            .append(GeoTrackColumns.COL_ALTITUDE).append(" REAL, ")// /
            .append(GeoTrackColumns.COL_BEARING).append(" REAL, ")// /
            .append(GeoTrackColumns.COL_SPEED).append(" REAL ")// /
            .append(" );").toString();

    // Index
    private static final String INDEX_TRACK_POINT_AK = "IDX_TRACKPOINT_AK";
    private static final String CREATE_INDEX_AK = "CREATE INDEX " + INDEX_TRACK_POINT_AK + " on " + GeoTrackDatabase.TABLE_TRACK_POINT + "(" //
            + GeoTrackColumns.COL_USERID + ", " //
            + GeoTrackColumns.COL_TIME //
            + ");";

  
    public GeoTrackOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION); 
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_BDD);
        // db.execSQL(CREATE_INDEX_AK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE " + GeoTrackDatabase.TABLE_TRACK_POINT + ";");
        // db.execSQL("DROP INDEX " + INDEX_TRACK_POINT_AK + ";");

        onCreate(db);
    }

}
