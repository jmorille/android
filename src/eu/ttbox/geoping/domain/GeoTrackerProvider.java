package eu.ttbox.geoping.domain;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;

public class GeoTrackerProvider extends ContentProvider {

    private final static String TAG = "GeoTrackerContentProvider";

    // Constante
    private static final String SELECT_BY_ENTITY_ID = String.format("%s = ?", PersonColumns.KEY_ID);

    // Instance
    private GeoTrackDatabase database;

    private static final int GEO_TRACKS = 0;
    private static final int GEOTRACK_ID = 1;
    private static final int SEARCH_SUGGEST = 2;
    private static final int REFRESH_SHORTCUT = 3;

    private static final UriMatcher sURIMatcher;

    public static class Constants {
        public static String AUTHORITY = "eu.ttbox.geoping.GeoTrackerProvider";

        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

        // MIME types used for searching words or looking up a single definition
        public static final String COLLECTION_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ttbox.geoTrackPoint";
        public static final String ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ttbox.geoTrackPoint";

    }

    static {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(Constants.AUTHORITY, "geoTrackPoints", GEO_TRACKS);
        matcher.addURI(Constants.AUTHORITY, "geoTrackPoint/#", GEOTRACK_ID);
        // to get suggestions...
        matcher.addURI(Constants.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(Constants.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

        /*
         * The following are unused in this implementation, but if we include
         * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our
         * suggestions table, we could expect to receive refresh queries when a
         * shortcutted suggestion is displayed in Quick Search Box, in which
         * case, the following Uris would be provided and we would return a
         * cursor with a single item representing the refreshed suggestion data.
         */
        // matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT,
        // REFRESH_SHORTCUT);
        // matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT +
        // "/*", REFRESH_SHORTCUT);
        // Define
        sURIMatcher = matcher;
    }

    @Override
    public boolean onCreate() {
        database = new GeoTrackDatabase(getContext());

        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
        case GEO_TRACKS:
            return Constants.COLLECTION_MIME_TYPE;
        case GEOTRACK_ID:
            return Constants.ITEM_MIME_TYPE;
        case SEARCH_SUGGEST:
            return SearchManager.SUGGEST_MIME_TYPE;
        case REFRESH_SHORTCUT:
            return SearchManager.SHORTCUT_MIME_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query for uri : " + uri);
        switch (sURIMatcher.match(uri)) {

        default:
            throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        // return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long personId = database.insert(values);
        Uri personUri = null;
        if (personId > -1) {
            personUri = Uri.withAppendedPath(Constants.CONTENT_URI, "/" + personId);
            getContext().getContentResolver().notifyChange(personUri, null);
             Log.d(TAG, "insert geoTrack Uri : " + uri);
             // Notify in broadcast
// TODO sendBroadcast
             
        }
        return personUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsAffected = 0;
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
        case GEOTRACK_ID:
            String entityId = uri.getLastPathSegment();
            rowsAffected = database.delete(SELECT_BY_ENTITY_ID, new String[] { entityId });
            Log.d(TAG, String.format("delete %s geoTrack Uri : ", rowsAffected, uri));
            break;
        case GEO_TRACKS:
            rowsAffected = database.delete(selection, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown or Invalid URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);

        return rowsAffected;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int rowsAffected = 0;
        switch (sURIMatcher.match(uri)) {
        case GEOTRACK_ID:
            String entityId = uri.getLastPathSegment();
            rowsAffected = database.update(values, SELECT_BY_ENTITY_ID, new String[] { entityId });
            break;
        case GEO_TRACKS:
            rowsAffected = database.update(values, selection, selectionArgs);
            Log.d(TAG, String.format("update %s geoTrack Uri : ", rowsAffected, uri));
            break;
        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsAffected;
    }

}
