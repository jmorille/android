package eu.ttbox.velib;

import java.util.Arrays;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;
import eu.ttbox.velib.service.database.VelibDatabase;
import eu.ttbox.velib.service.database.Velo.VeloColumns;

/**
 * @see http://developer.android.com/resources/samples/SearchableDictionary/src/com/example/android/searchabledict/DictionaryProvider.html
 * @see http://www.androidcompetencycenter.com/2009/01/basics-of-android-part-iv-android-content-providers/ Adding suggest :
 *      http://developer.android.com/guide/topics/search/adding-custom-suggestions.html Recent suggest @see
 *      http://developer.android.com/guide/topics/search/adding-recent-query-suggestions.html
 * @author deostem
 * 
 */
public class VeloContentProvider extends ContentProvider //
{

	private final static String TAG = "VeloContentProvider";

	private VelibDatabase veloDatabase;

	// UriMatcher stuff
	private static final int GET_ALL_STATION = 0;
	private static final int GET_FAVORITE_STATION = 1;
	private static final int GET_STATION = 2;
	private static final int SEARCH_SUGGEST = 3;
	private static final int REFRESH_SHORTCUT = 4;
	private static final UriMatcher sURIMatcher;

	public static class Constants {
		public static String AUTHORITY = "eu.ttbox.velib.VeloContentProvider";

		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
		public static final Uri CONTENT_URI_FAVORITE = Uri.parse("content://" + AUTHORITY+"/"+"stations/favorite/");
		public static final Uri CONTENT_URI_SUGGEST_URI_PATH_QUERY = Uri.parse("content://" + AUTHORITY+"/"+ SearchManager.SUGGEST_URI_PATH_QUERY + "/");
		
		// MIME types used for searching words or looking up a single definition
		public static final String COLLECTION_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ttbox.velib";
		public static final String ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ttbox.velib";

	}

	static {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		// to get definitions...
		matcher.addURI(Constants.AUTHORITY, "stations", GET_ALL_STATION);
		matcher.addURI(Constants.AUTHORITY, "stations/favorite/*", GET_FAVORITE_STATION);
		matcher.addURI(Constants.AUTHORITY, "station/#", GET_STATION);
		// to get suggestions...
		matcher.addURI(Constants.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
		matcher.addURI(Constants.AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

		/*
		 * The following are unused in this implementation, but if we include {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our suggestions
		 * table, we could expect to receive refresh queries when a shortcutted suggestion is displayed in Quick Search Box, in which case, the following Uris
		 * would be provided and we would return a cursor with a single item representing the refreshed suggestion data.
		 */
		// matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT, REFRESH_SHORTCUT);
		// matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT + "/*", REFRESH_SHORTCUT);
		// Define
		sURIMatcher = matcher;
	}

	@Override
	public boolean onCreate() {
		veloDatabase = new VelibDatabase(getContext()); 
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case GET_ALL_STATION:
			return Constants.COLLECTION_MIME_TYPE;
		case GET_FAVORITE_STATION:
			return Constants.COLLECTION_MIME_TYPE;
		case GET_STATION:
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
		// Use the UriMatcher to see what kind of query we have and format the db query accordingly
		String whereCriteria = null;
		String[] whereCriteriaArgs;
		// Manage Uri
		switch (sURIMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			String suggestString = new StringBuffer().append("%").append(uri.getLastPathSegment().toLowerCase()).append("%").toString();
			// whereCriteria = String.format("lower(%s) like ? or lower(%s) like ? or lower(%s) like ?", VeloColumns.COL_NAME, VeloColumns.COL_ALIAS_NAME,
			// VeloColumns.COL_ADDRESS);
			whereCriteria = String.format("%s like ? or %s like ? or %s like ?", VeloColumns.COL_NAME, VeloColumns.COL_ALIAS_NAME, VeloColumns.COL_ADDRESS);
			whereCriteriaArgs = new String[] { suggestString, suggestString };
			// whereClause = String.format("lower(%s) like ?", VeloColumns.COL_NAME, suggestString);
			// whereClause = new StringBuilder().append(VeloColumns.COL_NAME).append(" LIKE '%").append(selectionArgs[0].toLowerCase()).append("%'").toString();
			break;
		case GET_ALL_STATION:
//			whereCriteria = String.format("%s = ?", VeloColumns.COL_PROVIDER);
//			whereCriteriaArgs = new String[] { String.valueOf(VelibProvider.FR_PARIS.getProvider()) };
			whereCriteria = selection;
			whereCriteriaArgs = selectionArgs;
			break;
		case GET_FAVORITE_STATION:
 			whereCriteria = String.format("%s = ? and %s = ?", VeloColumns.COL_PROVIDER, VeloColumns.COL_FAVORY);
			whereCriteriaArgs = new String[] {  uri.getLastPathSegment(), "1" };
  			break;
		case GET_STATION:
			String stationId = uri.getLastPathSegment();
			whereCriteria = String.format("%s = ?", VeloColumns.COL_ID);
			whereCriteriaArgs = new String[] { stationId };
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		// Default Column
		String sort = sortOrder;
		if (sort == null) {
			sort = String.format("%s ASC", VeloColumns.COL_NAME);
		}
		String[] columns;
		if (projection != null) {
			columns = projection;
		} else {
			columns = new String[] { VeloColumns.COL_ID, VeloColumns.COL_NAME, VeloColumns.COL_ADDRESS, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };
			// , VeloColumns.ALIAS_COL_DISPO_CYCLE_PARKING
			// columns = new String[] { BaseColumns._ID, VeloColumns.COL_NAME, VeloColumns.COL_ADDRESS };
			// columns = VelibDatabase.ALL_COLS;
		}

		// Query Databse
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(VelibDatabase.TABLE_VELIB);
		builder.setProjectionMap(VelibDatabase.MAP_PROJECTION_COLUMN);
		// Where
		// builder.appendWhere(whereClause);
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "################################################################################");
			Log.i(TAG, "#################### ContentProvider Select " + Arrays.toString(columns));
			Log.i(TAG, "#################### ContentProvider WhereClause " + whereCriteria);
			Log.i(TAG, "#################### ContentProvider WhereArgs " + Arrays.toString(whereCriteriaArgs));
			Log.i(TAG, "################################################################################");
		}
		// Open Database
		SQLiteDatabase bdd = veloDatabase.getReadableDatabase();
		// cursor=builder.query(db.getReadableDatabase(), projection, db.colDeptName+"=?", new String[]{"IT"}, null, null, sortOrder)
		Cursor cursor = builder.query(bdd, columns, whereCriteria, whereCriteriaArgs, null, null, sort);
		// ---register to watch a content URI for changes---
		// cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

}
