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
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;

public class GeoTrackDatabase {

    public static final String TABLE_TRACK_POINT = "table_track_point";

    public static class GeoTrackColumns {
        public static final String COL_ID = BaseColumns._ID;
        public static final String COL_USERID = "USERID";
        public static final String COL_TIME = "TIME";
        public static final String COL_PROVIDER = "PROVIDER";
        public static final String COL_LATITUDE_E6 = "LAT_E6";
        public static final String COL_LONGITUDE_E6 = "LNG_E6";
        public static final String COL_ACCURACY = "ACCURACY";
        public static final String COL_ALTITUDE = "ALT";
        public static final String COL_BEARING = "BEARING";
        public static final String COL_SPEED = "SPEED";
        private static final String[] ALL_COLS = new String[] { COL_ID, COL_USERID, COL_TIME, COL_PROVIDER, COL_LATITUDE_E6, COL_LONGITUDE_E6, COL_ACCURACY, COL_ALTITUDE, COL_BEARING, COL_SPEED };

    }

    private static final int NUM_COL_ID = 0;
    private static final int NUM_COL_USERID = 1;
    private static final int NUM_COL_TIME = 2;
    private static final int NUM_COL_PROVIDER = 3;
    private static final int NUM_COL_LATITUDE_E6 = 4;
    private static final int NUM_COL_LONGITUDE_E6 = 5;
    private static final int NUM_COL_ACCURACY = 6;
    private static final int NUM_COL_ALTITUDE = 7;
    private static final int NUM_COL_BEARING = 8;
    private static final int NUM_COL_SPEED = 9;

    private static final String CRITERIA_BY_ENTITY_ID = String.format("%s = ?", GeoTrackColumns.COL_ID);
    
    private SQLiteDatabase bdd;

    private GeoTrackOpenHelper mDatabaseOpenHelper;

    private static final HashMap<String, String> mPersonColumnMap = buildUserColumnMap();

    public GeoTrackDatabase(Context context) {
        mDatabaseOpenHelper = new GeoTrackOpenHelper(context);
    }

    private static HashMap<String, String> buildUserColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        // Add Id
        map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
        // Add Identity Column
        for (String col : PersonColumns.ALL_KEYS) {
            if (!col.equals(PersonColumns.KEY_ID)) {
                map.put(col, col);
            }
        }
        // Add Suggest Aliases
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, String.format("%s AS %s", GeoTrackColumns.COL_LATITUDE_E6, SearchManager.SUGGEST_COLUMN_TEXT_1));
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_2, String.format("%s AS %s", GeoTrackColumns.COL_LONGITUDE_E6, SearchManager.SUGGEST_COLUMN_TEXT_2));
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        // Add Other Aliases
        return map;
    }

    

    public Cursor getGeoTrack(String rowId, String[] columns) { 
        String[] selectionArgs = new String[] { rowId };
        return queryGeoTrack(CRITERIA_BY_ENTITY_ID, selectionArgs, columns, null);
    }

    public Cursor queryGeoTrack(String selection, String[] selectionArgs, String[] columns, String order) {
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_TRACK_POINT);
        builder.setProjectionMap(mPersonColumnMap);
        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), columns, selection, selectionArgs, null, null, order);
        // Manage Cursor
        if (cursor == null) {
            return null;
        } else if (!cursor.moveToFirst()) {
            cursor.close();
            return null;
        }
        return cursor;
    }
    

    public long insert( ContentValues userValues) throws SQLException {
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

    public int delete(String selection, String[] selectionArgs) {
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

    public int update(ContentValues values, String selection, String[] selectionArgs) {
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
    
    
    @Deprecated
    public void open() {
        // on ouvre la BDD en �criture
        bdd = mDatabaseOpenHelper.getWritableDatabase();
    }

    @Deprecated
    public void close() {
        // on ferme l'acc�s � la BDD
        bdd.close();
    }
//
//    @Deprecated
//    public SQLiteDatabase getBDD() {
//        return bdd;
//    }
//
//    private SQLiteDatabase getWritableDatabase() {
//        return mDatabaseOpenHelper.getWritableDatabase();
//    }
//
//    public SQLiteDatabase getReadableDatabase() {
//        return mDatabaseOpenHelper.getReadableDatabase();
//    }
//
//    public SQLiteDatabase beginTransaction() {
//        SQLiteDatabase bdd = mDatabaseOpenHelper.getWritableDatabase();
//        bdd.beginTransaction();
//        return bdd;
//    }
//
//    public void commit(SQLiteDatabase bdd) {
//        bdd.setTransactionSuccessful();
//        bdd.endTransaction();
//    }
//
//    public void endTransaction(SQLiteDatabase bdd) {
//        bdd.endTransaction();
//    }
//
//    public long insertTrackPoint(GeoTrack geoPoint) {
//        // Cr�ation d'un ContentValues (fonctionne comme une HashMap)
//        ContentValues values = convertAsContentValues(geoPoint);
//        // on ins�re l'objet dans la BDD via le ContentValues
//        return bdd.insert(TABLE_TRACK_POINT, null, values);
//    }
//
//    private ContentValues convertAsContentValues(GeoTrack point) {
//        // on lui ajoute une valeur associ� � une cl� (qui est le nom de la
//        // colonne dans laquelle on veut mettre la valeur)
//        ContentValues values = new ContentValues();
//        values.put(GeoTrackColumns.COL_TIME, point.getTime());
//        values.put(GeoTrackColumns.COL_USERID, point.getUserId());
//        values.put(GeoTrackColumns.COL_PROVIDER, point.getProvider());
//        values.put(GeoTrackColumns.COL_LATITUDE_E6, point.getLatitudeE6());
//        values.put(GeoTrackColumns.COL_LONGITUDE_E6, point.getLongitudeE6());
//        values.put(GeoTrackColumns.COL_ACCURACY, point.getAccuracy());
//        values.put(GeoTrackColumns.COL_ALTITUDE, point.getAltitude());
//        values.put(GeoTrackColumns.COL_BEARING, point.getBearing());
//        values.put(GeoTrackColumns.COL_SPEED, point.getSpeed());
//        return values;
//    }
//
//    public int updateTrackPoint(int id, GeoTrack point) {
//        // La mise � jour d'un livre dans la BDD fonctionne plus ou moins comme
//        // une insertion
//        // il faut simple pr�ciser quelle livre on doit mettre � jour gr�ce �
//        // l'ID
//        ContentValues values = convertAsContentValues(point);
//        return bdd.update(TABLE_TRACK_POINT, values, GeoTrackColumns.COL_ID + " = " + id, null);
//    }
//
//    public int removeTrakPointWithID(int id) {
//        // Suppression d'un livre de la BDD gr�ce � l'ID
//        return bdd.delete(TABLE_TRACK_POINT, GeoTrackColumns.COL_ID + " = " + id, null);
//    }
//
    public List<GeoTrack> getTrakPointForToday(String userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        long pointDate = calendar.getTimeInMillis();
        String whereClause = GeoTrackColumns.COL_USERID + " = ? and " + GeoTrackColumns.COL_TIME + '>' + pointDate;
        Cursor c = bdd.query(TABLE_TRACK_POINT, GeoTrackColumns.ALL_COLS, whereClause, new String[] { userId }, null, null, GeoTrackColumns.COL_TIME);
        return cursorToLivre(c);
    }

    public List<GeoTrack> getTrakPointWithTitre(String userId) {
        Cursor c = bdd.query(TABLE_TRACK_POINT, GeoTrackColumns.ALL_COLS, GeoTrackColumns.COL_USERID + " = ?", new String[] { userId }, null, null, GeoTrackColumns.COL_TIME);
        return cursorToLivre(c);
    }

//    // Cette m�thode permet de convertir un cursor en un livre
    private List<GeoTrack> cursorToLivre(Cursor c) {
        // si aucun �l�ment n'a �t� retourn� dans la requ�te, on renvoie null
        List<GeoTrack> points = new ArrayList<GeoTrack>(c.getCount());
        if (c.getCount() == 0)
            return points;

        // Sinon on se place sur le premier �l�ment
        // c.moveToFirst();
        while (c.moveToNext()) {
            // On cr�� un livre
            GeoTrack point = new GeoTrack();
            // on lui affecte toutes les infos gr�ce aux infos contenues dans le
            // Cursor
            point.setId(c.getInt(NUM_COL_ID));
            point.setUserId(c.getString(NUM_COL_USERID));
            point.setTime(c.getLong(NUM_COL_TIME));
            point.setProvider(c.getString(NUM_COL_PROVIDER));
            point.setLatitudeE6(c.getInt(NUM_COL_LATITUDE_E6));
            point.setLongitudeE6(c.getInt(NUM_COL_LONGITUDE_E6));
            point.setAccuracy(c.getFloat(NUM_COL_ACCURACY));
            point.setAltitude(c.getDouble(NUM_COL_ALTITUDE));
            point.setBearing(c.getFloat(NUM_COL_BEARING));
            point.setSpeed(c.getFloat(NUM_COL_SPEED));
            points.add(point);
        }

        // On ferme le cursor
        c.close();

        // On retourne le livre
        return points;
    }

}
