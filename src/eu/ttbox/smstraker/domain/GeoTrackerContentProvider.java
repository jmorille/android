package eu.ttbox.smstraker.domain;

import eu.ttbox.smstraker.domain.geotrack.GeoTrackDatabase;
import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class GeoTrackerContentProvider extends ContentProvider {

	private final static String TAG = "GeoTrackerContentProvider";
	
	private GeoTrackDatabase database;
	
	private static final int GET_ALL_STATION = 0;
	private static final int GET_STATION = 1;
	private static final int SEARCH_SUGGEST = 2;
	private static final int REFRESH_SHORTCUT = 3;
	
	private static final UriMatcher sURIMatcher;
	
	public static class Constants {
		public static String AUTHORITY = "eu.ttbox.smstraker.domain.GeoTrackerContentProvider";

		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

		// MIME types used for searching words or looking up a single definition
		public static final String COLLECTION_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ttbox.trackPoint";
		public static final String ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ttbox.trackPoint";

	}
	
	static {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		// to get definitions...
		matcher.addURI(Constants.AUTHORITY, "stations", GET_ALL_STATION);
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
		database = new GeoTrackDatabase(getContext());

		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case GET_ALL_STATION:
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
		// TODO Auto-generated method stub
		return null;
	}
	
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	 
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase bdd = database.beginTransaction();
		//
		database.commit(bdd);
		return null;
	}




	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
