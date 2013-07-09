package eu.ttbox.geoping.domain;

import android.app.backup.BackupManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase;
import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase.GeoFenceColumns;

public class GeoFenceProvider extends ContentProvider {

    private static final String TAG = "GeoFenceProvider";

    // MIME types used for searching words or looking up a single definition
    public static final String GEOFENCES_LIST_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/ttbox.geoping.geofence";

    // Constante
    public static final String GEOFENCE_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/ttbox.geoping.geofence";


    public static class Constants {
        public static final String AUTHORITY = "eu.ttbox.geoping.GeoFenceProvider";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/geofence");
        public static final Uri CONTENT_URI_REQUEST_IDS = Uri.parse("content://" + AUTHORITY + "/geofenceRequestIds");

        public static final Uri getContentUri(long entityId) {
            Uri entityUri = Uri.withAppendedPath(GeoFenceProvider.Constants.CONTENT_URI, String.format("/%s", entityId));
            return entityUri;
        }


    }


    // UriMatcher stuff
    private static final int GEOFENCES = 0;
    private static final int GEOFENCE_ID = 1;
    private static final int GEOFENCE_REQUEST_IDS = 2;
    private static final UriMatcher sURIMatcher = buildUriMatcher();
    private GeoFenceDatabase geofenceDatabase;

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh
     * queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(Constants.AUTHORITY, "geofence", GEOFENCES);
        matcher.addURI(Constants.AUTHORITY, "geofence/#", GEOFENCE_ID);
        matcher.addURI(Constants.AUTHORITY, "geofenceRequestIds", GEOFENCE_REQUEST_IDS);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        geofenceDatabase = new GeoFenceDatabase(getContext());
        return true;
    }

    /**
     * Handles all the dictionary searches and suggestion queries from the
     * Search Manager. When requesting a specific word, the uri alone is
     * required. When searching all of the dictionary for matches, the
     * selectionArgs argument must carry the search query as the first element.
     * All other arguments are ignored.
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query for uri : " + uri);
        // Use the UriMatcher to see what kind of query we have and format the
        // db query accordingly
        switch (sURIMatcher.match(uri)) {
            case GEOFENCES:
                return geofenceDatabase.queryEntities(projection, selection, selectionArgs, sortOrder);
            case GEOFENCE_ID:
                return getGeoFence(uri);
            case GEOFENCE_REQUEST_IDS:
                if (!TextUtils.isEmpty(selection)) {
                    throw new IllegalArgumentException("UnSupported query GEOFENCE_REQUEST_IDS with :  Uri: " + uri + " and selection="+selection);
                }
                return geofenceDatabase.getEntityByRequestIds(projection, selectionArgs, sortOrder);
            default:
                throw new IllegalArgumentException("Unknown query Uri: " + uri);
        }
    }

    private Cursor getGeoFence(Uri uri) {
        String rowId = uri.getLastPathSegment();
        String[] columns = GeoFenceColumns.ALL_COLS;
        return geofenceDatabase.getEntityById(rowId, columns);
    }

    /**
     * This method is required in order to query the supported types. It's also
     * useful in our own query() method to determine the type of Uri received.
     */
    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
            case GEOFENCES:
                return GEOFENCES_LIST_MIME_TYPE;
            case GEOFENCE_REQUEST_IDS:
                return GEOFENCES_LIST_MIME_TYPE;
            case GEOFENCE_ID:
                return GEOFENCE_MIME_TYPE;
            default:
                throw new IllegalArgumentException("Unknown Type URL " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (sURIMatcher.match(uri)) {
            case GEOFENCES:
                long geofenceId = geofenceDatabase.insertEntity(values);
                Uri geofenceUri = null;
                if (geofenceId > -1) {
                    geofenceUri = Constants.getContentUri(geofenceId);
                    getContext().getContentResolver().notifyChange(uri, null);
                    Log.i(TAG, String.format("Insert GeoFence %s : %s", geofenceUri, values));
                    // Backup
                    BackupManager.dataChanged(getContext().getPackageName());
                }
                return geofenceUri;
            default:
                throw new IllegalArgumentException("Unknown insert Uri: " + uri);
        }
    }

    // Other required implementations...

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (sURIMatcher.match(uri)) {
            case GEOFENCE_ID:
                String entityId = uri.getLastPathSegment();
                String[] args = new String[]{entityId};
                count = geofenceDatabase.deleteEntity(GeoFenceColumns.SELECT_BY_ENTITY_ID, args);
                break;
            case GEOFENCES:
                count = geofenceDatabase.deleteEntity(selection, selectionArgs);
                break;
            case GEOFENCE_REQUEST_IDS:
                if (!TextUtils.isEmpty(selection)) {
                    throw new IllegalArgumentException("UnSupported delete GEOFENCE_REQUEST_IDS with :  Uri: " + uri + " and selection="+selection);
                }
                count = geofenceDatabase.deleteEntityByRequestIds(selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown delete Uri: " + uri);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            // Backup
            BackupManager.dataChanged(getContext().getPackageName());
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (sURIMatcher.match(uri)) {
            case GEOFENCE_ID:
                String entityId = uri.getLastPathSegment();
                String[] args = new String[]{entityId};
                count = geofenceDatabase.updateEntity(values, GeoFenceColumns.SELECT_BY_ENTITY_ID, args);
                break;
            case GEOFENCES:
                count = geofenceDatabase.updateEntity(values, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown update Uri: " + uri);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
            // Backup
            BackupManager.dataChanged(getContext().getPackageName());
        }
        return count;
    }

}
