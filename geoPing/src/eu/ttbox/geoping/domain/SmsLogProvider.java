package eu.ttbox.geoping.domain;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;

public class SmsLogProvider extends ContentProvider {

    private static final String TAG = "SmsLogProvider";

    // MIME types used for searching words or looking up a single definition
    public static final String SMSLOGS_LIST_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/ttbox.geoping.smsLog";
    public static final String SMSLOG_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/ttbox.geoping.smsLog";

    public static class Constants {
        public static String AUTHORITY = "eu.ttbox.geoping.SmsLogProvider";
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/smslog");
        
        public static final Uri CONTENT_URI_PHONE_FILTER = Uri.withAppendedPath(CONTENT_URI, "phone_lookup");
     }

    private SmsLogDatabase smslogDatabase;

    // UriMatcher stuff
    private static final int SMSLOGS = 0;
    private static final int SMSLOG_ID = 1;
    private static final int PHONE_FILTER = 2;

    private static final UriMatcher sURIMatcher = buildUriMatcher();

    /**
     * Builds up a UriMatcher for search suggestion and shortcut refresh
     * queries.
     */
    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        // to get definitions...
        matcher.addURI(Constants.AUTHORITY, "smslog", SMSLOGS);
        matcher.addURI(Constants.AUTHORITY, "smslog/#", SMSLOG_ID);
        
        matcher.addURI(Constants.AUTHORITY, "smslog/phone_lookup/*", PHONE_FILTER);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        smslogDatabase = new SmsLogDatabase(getContext());
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
        case SMSLOGS:
            return search(projection, selection, selectionArgs, sortOrder);
        case SMSLOG_ID:
            return getSmsLog(uri);
        case PHONE_FILTER:
            String phone = uri.getLastPathSegment();
            String phoneDecoder = Uri.decode(phone);
            return smslogDatabase.searchForPhoneNumber(phoneDecoder, projection, selection,  selectionArgs, sortOrder);            
        default:
            throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    private Cursor search(String[] _projection, String _selection, String[] _selectionArgs, String _sortOrder) {
        String[] projection = _projection == null ? SmsLogDatabase.SmsLogColumns.ALL_COLS : _projection;
        String selection = _selection;
        String[] selectionArgs = _selectionArgs;
        String sortOrder = _sortOrder;
        return smslogDatabase.queryEntities(projection, selection, selectionArgs, sortOrder);
    }

    private Cursor getSmsLog(Uri uri) {
        String rowId = uri.getLastPathSegment();
        String[] columns = SmsLogDatabase.SmsLogColumns.ALL_COLS;
        return smslogDatabase.getEntityById(rowId, columns);
    }

    /**
     * This method is required in order to query the supported types. It's also
     * useful in our own query() method to determine the type of Uri received.
     */
    @Override
    public String getType(Uri uri) {
        switch (sURIMatcher.match(uri)) {
        case SMSLOGS:
            return SMSLOGS_LIST_MIME_TYPE;
        case SMSLOG_ID:
            return SMSLOG_MIME_TYPE;
        default:
            throw new IllegalArgumentException("Unknown URL " + uri);
        }
    }

    // Other required implementations...

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (sURIMatcher.match(uri)) {
        case SMSLOGS:
            long smslogId = smslogDatabase.insertEntity(values);
            Uri smslogUri = null;
            if (smslogId > -1) {
                smslogUri = Uri.withAppendedPath(Constants.CONTENT_URI, String.valueOf( smslogId));
                getContext().getContentResolver().notifyChange(uri, null);
            }
            return smslogUri;
        default:
            throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count = 0;
        switch (sURIMatcher.match(uri)) {
        case SMSLOG_ID:
            String entityId = uri.getLastPathSegment();
            String[] args = new String[] { entityId };
            count = smslogDatabase.deleteEntity(SmsLogColumns.SELECT_BY_ENTITY_ID, args);
            break;
        case SMSLOGS:
            count = smslogDatabase.deleteEntity(selection, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        if (count>0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int count = 0;
        switch (sURIMatcher.match(uri)) {
        case SMSLOG_ID:
            String entityId = uri.getLastPathSegment();
            String[] args = new String[] { entityId };
            count = smslogDatabase.updateEntity(values, SmsLogColumns.SELECT_BY_ENTITY_ID, args);
            break;
        case SMSLOGS:
            count = smslogDatabase.updateEntity(values, selection, selectionArgs);
            break;
        default:
            throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

}
