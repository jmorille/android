package eu.ttbox.geoping.domain.geotrack;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import eu.ttbox.geoping.domain.GeoTrack;

public class GeoTrackDatabase {

    public static final String TABLE_TRACK_POINT = "table_track_point";

    public static class GeoTrackColumns {
        public static final String COL_ID = BaseColumns._ID;
        public static final String COL_PHONE_NUMBER = "PHONE_NUMBER";
        public static final String COL_TIME = "TIME";
        public static final String COL_PROVIDER = "PROVIDER";
        public static final String COL_LATITUDE_E6 = "LAT_E6";
        public static final String COL_LONGITUDE_E6 = "LNG_E6";
        public static final String COL_ACCURACY = "ACCURACY";
        public static final String COL_ALTITUDE = "ALT";
        public static final String COL_BEARING = "BEARING";
        public static final String COL_SPEED = "SPEED";
        public static final String COL_ADDRESS = "ADDRESS";
        public static final String[] ALL_COLS = new String[] { COL_ID, COL_PHONE_NUMBER, COL_TIME, COL_PROVIDER, COL_LATITUDE_E6, COL_LONGITUDE_E6, COL_ACCURACY, COL_ALTITUDE, COL_BEARING, COL_SPEED,
                COL_ADDRESS };

    }

    private static final String CRITERIA_BY_ENTITY_ID = String.format("%s = ?", GeoTrackColumns.COL_ID);
    private static final String CRITERIA_BY_USER_ID = String.format("%s = ?", GeoTrackColumns.COL_PHONE_NUMBER);

    private SQLiteDatabase bdd;

    private GeoTrackOpenHelper mDatabaseOpenHelper;

    private static final HashMap<String, String> mGeoTrackColumnMap = buildGeoTrackColumnMap();

    public GeoTrackDatabase(Context context) {
        mDatabaseOpenHelper = new GeoTrackOpenHelper(context);
    }

    private static HashMap<String, String> buildGeoTrackColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        // Add Id
        for (String col : GeoTrackColumns.ALL_COLS) {
            map.put(col, col);
        }
        // Add Suggest Aliases
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, String.format("%s AS %s", GeoTrackColumns.COL_LATITUDE_E6, SearchManager.SUGGEST_COLUMN_TEXT_1));
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_2, String.format("%s AS %s", GeoTrackColumns.COL_LONGITUDE_E6, SearchManager.SUGGEST_COLUMN_TEXT_2));
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        // Add Other Aliases
        return map;
    }

    public Cursor getEntityById(String rowId, String[] columns) {
        String[] selectionArgs = new String[] { rowId };
        return queryEntities(columns, CRITERIA_BY_ENTITY_ID, selectionArgs, null);
    }

    public Cursor queryEntities(String[] columns, String selection, String[] selectionArgs, String order) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_TRACK_POINT);
        builder.setProjectionMap(mGeoTrackColumnMap);
        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns, selection, selectionArgs, null, null, order);
        // Manage Cursor
        // if (cursor == null) {
        // return null;
        // } else if (!cursor.moveToFirst()) {
        // cursor.close();
        // return null;
        // }
        return cursor;
    }

    public long insertEntity(ContentValues userValues) throws SQLException {
        long result = -1;
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.insertOrThrow(TABLE_TRACK_POINT, null, userValues);
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

    public int deleteEntity(String selection, String[] selectionArgs) {
        int result = -1;
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.delete(TABLE_TRACK_POINT, selection, selectionArgs);
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
            db.beginTransaction();
            try {
                result = db.update(TABLE_TRACK_POINT, values, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
        return result;
    }

    // @Deprecated
    // public void open() {
    // // on ouvre la BDD en �criture
    // bdd = mDatabaseOpenHelper.getWritableDatabase();
    // }
    //
    // @Deprecated
    // public void close() {
    // // on ferme l'acc�s � la BDD
    // bdd.close();
    // }

    //
    // @Deprecated
    // public SQLiteDatabase getBDD() {
    // return bdd;
    // }
    //
    // private SQLiteDatabase getWritableDatabase() {
    // return mDatabaseOpenHelper.getWritableDatabase();
    // }
    //
    // public SQLiteDatabase getReadableDatabase() {
    // return mDatabaseOpenHelper.getReadableDatabase();
    // }
    //
    // public SQLiteDatabase beginTransaction() {
    // SQLiteDatabase bdd = mDatabaseOpenHelper.getWritableDatabase();
    // bdd.beginTransaction();
    // return bdd;
    // }
    //
    // public void commit(SQLiteDatabase bdd) {
    // bdd.setTransactionSuccessful();
    // bdd.endTransaction();
    // }
    //
    // public void endTransaction(SQLiteDatabase bdd) {
    // bdd.endTransaction();
    // }
    //
    // public long insertTrackPoint(GeoTrack geoPoint) {
    // // Cr�ation d'un ContentValues (fonctionne comme une HashMap)
    // ContentValues values = convertAsContentValues(geoPoint);
    // // on ins�re l'objet dans la BDD via le ContentValues
    // return bdd.insert(TABLE_TRACK_POINT, null, values);
    // }
    //
    // private ContentValues convertAsContentValues(GeoTrack point) {
    // // on lui ajoute une valeur associ� � une cl� (qui est le nom de la
    // // colonne dans laquelle on veut mettre la valeur)
    // ContentValues values = new ContentValues();
    // values.put(GeoTrackColumns.COL_TIME, point.getTime());
    // values.put(GeoTrackColumns.COL_USERID, point.getUserId());
    // values.put(GeoTrackColumns.COL_PROVIDER, point.getProvider());
    // values.put(GeoTrackColumns.COL_LATITUDE_E6, point.getLatitudeE6());
    // values.put(GeoTrackColumns.COL_LONGITUDE_E6, point.getLongitudeE6());
    // values.put(GeoTrackColumns.COL_ACCURACY, point.getAccuracy());
    // values.put(GeoTrackColumns.COL_ALTITUDE, point.getAltitude());
    // values.put(GeoTrackColumns.COL_BEARING, point.getBearing());
    // values.put(GeoTrackColumns.COL_SPEED, point.getSpeed());
    // return values;
    // }
    //
    // public int updateTrackPoint(int id, GeoTrack point) {
    // // La mise � jour d'un livre dans la BDD fonctionne plus ou moins comme
    // // une insertion
    // // il faut simple pr�ciser quelle livre on doit mettre � jour gr�ce �
    // // l'ID
    // ContentValues values = convertAsContentValues(point);
    // return bdd.update(TABLE_TRACK_POINT, values, GeoTrackColumns.COL_ID +
    // " = " + id, null);
    // }
    //
    // public int removeTrakPointWithID(int id) {
    // // Suppression d'un livre de la BDD gr�ce � l'ID
    // return bdd.delete(TABLE_TRACK_POINT, GeoTrackColumns.COL_ID + " = " + id,
    // null);
    // }
    //
    public List<GeoTrack> getTrakPointForToday(String userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        long pointDate = calendar.getTimeInMillis();
        String whereClause = GeoTrackColumns.COL_PHONE_NUMBER + " = ? and " + GeoTrackColumns.COL_TIME + '>' + pointDate;
        Cursor c = bdd.query(TABLE_TRACK_POINT, GeoTrackColumns.ALL_COLS, whereClause, new String[] { userId }, null, null, GeoTrackColumns.COL_TIME);
        return cursorToLivre(c);
    }

    public List<GeoTrack> getTrakPointWithTitre(String userId) {
        Cursor c = bdd.query(TABLE_TRACK_POINT, GeoTrackColumns.ALL_COLS, CRITERIA_BY_USER_ID, new String[] { userId }, null, null, GeoTrackColumns.COL_TIME);
        return cursorToLivre(c);
    }

    private List<GeoTrack> cursorToLivre(Cursor c) {
        List<GeoTrack> points = new ArrayList<GeoTrack>(c.getCount());
        if (c.getCount() == 0)
            return points;

        if (c.moveToFirst()) {
            ;
            GeoTrackHelper helper = new GeoTrackHelper().initWrapper(c);
            while (c.moveToNext()) {
                // On cr�� un livre
                GeoTrack point = helper.getEntity(c);
                points.add(point);
            }
        }

        // On ferme le cursor
        c.close();

        // On retourne le livre
        return points;
    }

}
