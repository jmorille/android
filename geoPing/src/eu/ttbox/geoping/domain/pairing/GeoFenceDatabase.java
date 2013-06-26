package eu.ttbox.geoping.domain.pairing;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import eu.ttbox.geoping.crypto.CryptoUtils;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.service.geofence.GeoFenceLocationService;

public class GeoFenceDatabase {

    public static final String TAG = "GeoFenceDatabase";
    public static final String TABLE_GEOFENCE = "geofence";
    private static final String CRITERIA_BY_ENTITY_ID = String.format("%s = ?", GeoFenceColumns.COL_ID);
    private static final String CRITERIA_BY_USER_ID = String.format("%s = ?", GeoFenceColumns.COL_REQUEST_ID);
    private static final HashMap<String, String> mCircleGeofenceColumnMap = buildCircleGeofenceColumnMap();
    private SQLiteDatabase bdd;
    private PairingOpenHelper mDatabaseOpenHelper;
    private GeoFenceLocationService mGeoFenceLocationService;

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

    public Cursor getEntityByRequestIds(String[] projection, String[] requestIds, String order) {
        String selection = makeWhereClause(GeoFenceColumns.COL_REQUEST_ID, requestIds);
        return queryEntities(projection, selection, requestIds, order);
    }

    private String makeWhereClause(String colName, String... requestIds) {
        StringBuilder sb = new StringBuilder();
        int requestIdSize = requestIds.length;
        if (requestIdSize == 1) {
            sb.append(colName).append('=').append('?');
        } else {
            sb.append(colName).append(" in (");
            for (int i = 0; i < requestIdSize; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append('?');
            }
            sb.append(')');
        }
        return sb.toString();
    }

    public Cursor queryEntities(String[] _projection, String selection, String[] selectionArgs, String order) {
        String[] projection = _projection == null ? GeoFenceColumns.ALL_COLS : _projection;
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
        CircleGeofence circleGeofence = GeoFenceHelper.getEntityFromContentValue(values);
        Geofence geofence = circleGeofence.toGeofence();
        mGeoFenceLocationService.addGeofences(geofence);
        // Save in Db
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                result = db.insertOrThrow(TABLE_GEOFENCE, null, values);
                // Check Insert Name
                if (result > -1 && TextUtils.isEmpty(values.getAsString(GeoFenceColumns.COL_NAME))) {
                    ContentValues updateName = new ContentValues(1);
                    // TODO Internationalize Zone xx
                    updateName.put(GeoFenceColumns.COL_NAME, String.format("Zone %s", result));
                    long resultUpdateName = db.update(TABLE_GEOFENCE, updateName, GeoFenceColumns.SELECT_BY_ENTITY_ID, new String[]{String.valueOf(result)});
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

    public boolean fillRequestId(ContentValues values) {
        boolean isGeofenceValues = false;
        if (values.containsKey(GeoFenceColumns.COL_LATITUDE_E6)
                || values.containsKey(GeoFenceColumns.COL_LONGITUDE_E6)
                || values.containsKey(GeoFenceColumns.COL_RADIUS)
                || values.containsKey(GeoFenceColumns.COL_TRANSITION)
                || values.containsKey(GeoFenceColumns.COL_EXPIRATION)
                ) {
            isGeofenceValues = true;
            // So need to add request Id
            String requestId = values.getAsString(GeoFenceColumns.COL_REQUEST_ID);
            if (TextUtils.isEmpty(requestId)) {
                String value = CryptoUtils.generateUniqueId();
                values.put(GeoFenceColumns.COL_REQUEST_ID, value);
                Log.d(TAG, "Add COL_REQUEST_ID : " + value);
            }
            isGeofenceValues = true;
        }
        return isGeofenceValues;
    }

    public Geofence fillForGeofenceRequester(SQLiteDatabase db, ContentValues values, String selection, String[] selectionArgs) {
        Geofence geofence = null;
        // Check For Requester Keys
        boolean isContaintKey = false;
        boolean isContainAllKey = true;
        ArrayList<String> missingCOls = new ArrayList<String>(GeoFenceColumns.ALL_GEOFENCE_REQUESTER_COLS.length);
        for (String col : GeoFenceColumns.ALL_GEOFENCE_REQUESTER_COLS) {
            if (values.containsKey(col)) {
                isContaintKey = true;
            } else {
                isContainAllKey = false;
                missingCOls.add(col);
            }
        }

        // Complete Datas
        ContentValues missingValues = null;
        if (isContaintKey) {
            if (isContainAllKey) {
                geofence = GeoFenceHelper.getEntityGeoFenceFromContentValue(values);
            } else {
                int missingCOlSize = missingCOls.size();
                missingValues = new ContentValues(missingCOlSize);
                Cursor cursor = queryEntities(missingCOls.toArray(new String[missingCOlSize]), selection, selectionArgs, null);
                try {
                    if (cursor.getCount() > 1) {
                        throw new IllegalArgumentException("Found multi request for Selection " + selection + " / Args =  " + Arrays.toString(selectionArgs));
                    }
                    if (cursor.moveToNext()) {
                        for (String col : missingCOls) {
                            int colIdx = cursor.getColumnIndex(col);
                            if (GeoFenceColumns.COL_REQUEST_ID.equals(col)) {
                                String colVal = cursor.getString(colIdx);
                                missingValues.put(col, colVal);
                            } else {
                                int colVal = cursor.getInt(colIdx);
                                missingValues.put(col, colVal);
                            }
                        }
                    }
                } finally {
                    cursor.close();
                }
                // Compute result
                ContentValues result = new ContentValues(missingCOlSize + values.size());
                result.putAll(values);
                result.putAll(missingValues);
                geofence = GeoFenceHelper.getEntityGeoFenceFromContentValue(result);

            }
        }
        return geofence;
    }

    public int updateEntity(ContentValues values, String selection, String[] selectionArgs) {
        int result = -1;
         // Save in Db
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        Geofence geofence = fillForGeofenceRequester(db, values, selection, selectionArgs);
        if (geofence !=null) {
            // Register in LocationServices
            mGeoFenceLocationService.addGeofences(geofence);
        }
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
        String[] projection = new String[]{GeoFenceColumns.COL_REQUEST_ID};
        Cursor cursor = queryEntities(projection, selection, selectionArgs, null);
        try {
            while (cursor.moveToNext()) {
                String requestId = cursor.getString(0);
                result.add(requestId);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    public int deleteEntityByRequestIds(String[] requestArraysIds) {
        int result = -1;
        if (requestArraysIds == null || requestArraysIds.length < 1) {
            Log.w(TAG, "deleteEntityByRequestIds with no requestArraysIds : " + requestArraysIds);
            return result;
        }
        SQLiteDatabase db = mDatabaseOpenHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            try {
                List<String> requestIds = Arrays.asList(requestArraysIds);
                if (requestIds != null && !requestIds.isEmpty()) {
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

    public static class GeoFenceColumns {
        public static final String COL_ID = BaseColumns._ID;
        public static final String COL_NAME = "NAME";
        // Phone
        public static final String COL_REQUEST_ID = "REQUEST_ID";
        // Location
        public static final String COL_LATITUDE_E6 = "LAT_E6";
        public static final String COL_LONGITUDE_E6 = "LNG_E6";
        public static final String COL_ADDRESS = "ADDRESS";
        public static final String COL_RADIUS = "RADIUS";
        public static final String COL_TRANSITION = "TRANSITION";
        public static final String COL_EXPIRATION = "EXPIRATION";
        public static final String[] ALL_COLS = new String[]{COL_ID, COL_REQUEST_ID, COL_NAME//
                , COL_LATITUDE_E6, COL_LONGITUDE_E6, COL_RADIUS, COL_TRANSITION, COL_EXPIRATION //
                , COL_ADDRESS //
        };
        public static final String[] ALL_GEOFENCE_REQUESTER_COLS = new String[]{COL_REQUEST_ID//
                , COL_LATITUDE_E6, COL_LONGITUDE_E6, COL_RADIUS, COL_TRANSITION, COL_EXPIRATION //
        };
        // Where Clause
        public static final String SELECT_BY_ENTITY_ID = String.format("%s = ?", COL_ID);

    }

}
