package eu.ttbox.geoping.domain.pairing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.service.geofence.GeoFenceLocationService;

public class GeoFenceDatabase {

    public static final String TAG = "GeoFenceDatabase";

    public static final String TABLE_GEOFENCE = "geofence";

    public static class GeoFenceColumns {
        public static final String COL_ID = BaseColumns._ID;
        public static final String COL_NAME = "NAME";
        // Phone
        public static final String COL_REQUEST_ID = "REQUEST_ID";
        // Location
        public static final String COL_LATITUDE_E6 = "LAT_E6";
        public static final String COL_LONGITUDE_E6 = "LNG_E6";
        public static final String COL_RADIUS = "RADIUS";
        public static final String COL_TRANSITION = "TRANSITION";
        public static final String COL_EXPIRATION = "EXPIRATION";

        public static final String[] ALL_COLS = new String[]{COL_ID, COL_REQUEST_ID, COL_NAME//
                , COL_LATITUDE_E6, COL_LONGITUDE_E6, COL_RADIUS, COL_TRANSITION, COL_EXPIRATION // 
        };
        // Where Clause
        public static final String SELECT_BY_ENTITY_ID = String.format("%s = ?", COL_ID);

    }

    private static final String CRITERIA_BY_ENTITY_ID = String.format("%s = ?", GeoFenceColumns.COL_ID);
    private static final String CRITERIA_BY_USER_ID = String.format("%s = ?", GeoFenceColumns.COL_REQUEST_ID);

    private SQLiteDatabase bdd;

    private PairingOpenHelper mDatabaseOpenHelper;

    private GeoFenceLocationService mGeoFenceLocationService;

    private static final HashMap<String, String> mCircleGeofenceColumnMap = buildCircleGeofenceColumnMap();

    public GeoFenceDatabase(Context context) {
        mDatabaseOpenHelper = new PairingOpenHelper(context);
        mGeoFenceLocationService = new GeoFenceLocationService(context);
    }

    private static HashMap<String, String> buildCircleGeofenceColumnMap() {
        HashMap<String, String> map = new HashMap<String, String>();
        // Add Id
        for (String col : GeoFenceColumns.ALL_COLS) {
            map.put(col, col);
        }
        // Add Suggest Aliases
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_1, String.format("%s AS %s", GeoFenceColumns.COL_LATITUDE_E6, SearchManager.SUGGEST_COLUMN_TEXT_1));
        map.put(SearchManager.SUGGEST_COLUMN_TEXT_2, String.format("%s AS %s", GeoFenceColumns.COL_LONGITUDE_E6, SearchManager.SUGGEST_COLUMN_TEXT_2));
        map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
        map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS " + SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
        // Add Other Aliases
        return map;
    }

    public Cursor getEntityById(String rowId, String[] projection) {
        String[] selectionArgs = new String[]{rowId};
        return queryEntities(projection, CRITERIA_BY_ENTITY_ID, selectionArgs, null);
    }

    public Cursor getEntityByRequestIds( String[] projection, String[] requestIds, String order) {
        String selection = makeWhereClause(GeoFenceColumns.COL_REQUEST_ID, requestIds);
        return queryEntities(projection, selection, requestIds, order);
    }

    private String makeWhereClause(String colName,String... requestIds) {
        StringBuilder sb = new StringBuilder();
        int requestIdSize = requestIds.length;
        if (requestIdSize== 1) {
            sb.append(colName).append('=').append('?');
        } else {
            sb.append(colName).append( " in (");
            for (int i = 0 ; i <requestIdSize; i ++) {
                sb.append('?');
                if (i != 0) {
                    sb.append(',');
                }
            }
            sb.append(')');
        }
        return sb.toString();
    }

    public Cursor queryEntities(String[] _projection, String selection, String[] selectionArgs, String order) {
        String[] projection = _projection == null? GeoFenceColumns.ALL_COLS :_projection;
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        builder.setTables(TABLE_GEOFENCE);
        builder.setProjectionMap(mCircleGeofenceColumnMap);
        Cursor cursor = builder.query(mDatabaseOpenHelper.getReadableDatabase(), projection, selection, selectionArgs, null, null, order);
        return cursor;
    }

    public long insertEntity(ContentValues values) throws SQLException {
        long result = -1;
        // Complete Data
        fillRequestId(values);
        // Register in LocationServices
        CircleGeofence circleGeofence =  GeoFenceHelper.getEntityFromContentValue(values);
        Geofence geofence =  circleGeofence.toGeofence();
        mGeoFenceLocationService.addGeofences(geofence);
        // Save in Db
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.insertOrThrow(TABLE_GEOFENCE, null, values);
                // Check Insert Name
                if (result>-1 && TextUtils.isEmpty(values.getAsString(GeoFenceColumns.COL_NAME))) {
                    ContentValues updateName = new ContentValues(1);
                    // TODO Internationalize Zone xx
                    updateName.put(GeoFenceColumns.COL_NAME, String.format("Zone %s", result));
                    long resultUpdateName =  db.update(TABLE_GEOFENCE, updateName, GeoFenceColumns.SELECT_BY_ENTITY_ID , new String[] { String.valueOf(result) });
                }
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

    public void fillRequestId(ContentValues values) {
        String requestId = values.getAsString(GeoFenceColumns.COL_REQUEST_ID);
        if (TextUtils.isEmpty(requestId)) {
            String value = UUID.randomUUID().toString();
            values.put(GeoFenceColumns.COL_REQUEST_ID, value);
            Log.d(TAG, "Add COL_REQUEST_ID : " + value);
        }
    }

    public int updateEntity(ContentValues values, String selection, String[] selectionArgs) {
        int result = -1;
        // Complete Data
        fillRequestId(values);
        // Register in LocationServices
        CircleGeofence circleGeofence =  GeoFenceHelper.getEntityFromContentValue(values);
        Geofence geofence =  circleGeofence.toGeofence();
        mGeoFenceLocationService.addGeofences(geofence);
        // Save in Db
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
         try {
            db.beginTransaction();
            try {
                result = db.update(TABLE_GEOFENCE, values, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
        return result;
    }

    private List<String> getRequestIds(SQLiteDatabase db, String selection, String[] selectionArgs) {
       ArrayList<String> result = new ArrayList<String>();
        // Read RequestId
        String[] projection = new String[] {GeoFenceColumns.COL_REQUEST_ID};
        Cursor cursor = queryEntities(projection, selection, selectionArgs, null);
        try {
            while (cursor.moveToNext()) {
                String requestId = cursor.getString(0);
                result.add(requestId);
            }
        } finally {
            if (cursor!=null) {
                cursor.close();
            }
        }
        return result;
    }

    public int deleteEntityByRequestIds( String[] requestArraysIds) {
        int result = -1;
        if (requestArraysIds==null || requestArraysIds.length <1) {
            Log.w(TAG, "deleteEntityByRequestIds with no requestArraysIds : "  + requestArraysIds);
            return  result;
        }
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                List<String> requestIds = Arrays.asList(requestArraysIds);
                if (requestIds!=null && !requestIds.isEmpty()) {
                    mGeoFenceLocationService.removeGeofencesById(requestIds);
                }
                // Delete Data
                String selection = makeWhereClause(GeoFenceColumns.COL_REQUEST_ID, requestArraysIds);
                result = db.delete(TABLE_GEOFENCE, selection, requestArraysIds);
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
                List<String> requestIds = getRequestIds(db, selection, selectionArgs);
                if (requestIds!=null && !requestIds.isEmpty()) {
                    mGeoFenceLocationService.removeGeofencesById(requestIds);
                }
                // Delete Data
                result = db.delete(TABLE_GEOFENCE, selection, selectionArgs);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } finally {
            db.close();
        }
        return result;
    }

    public List<CircleGeofence> getTrakPointForToday(String userId) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear(Calendar.HOUR);
        calendar.clear(Calendar.HOUR_OF_DAY);
        calendar.clear(Calendar.MINUTE);
        calendar.clear(Calendar.SECOND);
        calendar.clear(Calendar.MILLISECOND);
        long pointDate = calendar.getTimeInMillis();
        String whereClause = GeoFenceColumns.COL_REQUEST_ID + " = ? and " + GeoFenceColumns.COL_EXPIRATION + '>' + pointDate;
        Cursor c = bdd.query(TABLE_GEOFENCE, GeoFenceColumns.ALL_COLS, whereClause, new String[]{userId}, null, null, GeoFenceColumns.COL_EXPIRATION);
        return cursorToLivre(c);
    }

    public List<CircleGeofence> getTrakPointWithTitre(String userId) {
        Cursor c = bdd.query(TABLE_GEOFENCE, GeoFenceColumns.ALL_COLS, CRITERIA_BY_USER_ID, new String[]{userId}, null, null, GeoFenceColumns.COL_EXPIRATION);
        return cursorToLivre(c);
    }

    private List<CircleGeofence> cursorToLivre(Cursor c) {
        List<CircleGeofence> points = new ArrayList<CircleGeofence>(c.getCount());
        if (c.getCount() == 0)
            return points;

        if (c.moveToFirst()) {
            GeoFenceHelper helper = new GeoFenceHelper().initWrapper(c);
            while (c.moveToNext()) {
                CircleGeofence point = helper.getEntity(c);
                points.add(point);
            }
        }

        // On ferme le cursor
        c.close();

        // On retourne le livre
        return points;
    }

}
