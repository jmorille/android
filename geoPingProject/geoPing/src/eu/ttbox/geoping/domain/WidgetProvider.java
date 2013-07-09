package eu.ttbox.geoping.domain;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class WidgetProvider extends ContentProvider {

	private static final String TAG = "WidgetProvider";

	public static class Constants {
		public static String AUTHORITY = "eu.ttbox.geoping.WidgetProvider";
		public static final Uri CONTENT_URI_PERSON = Uri.parse("content://" + AUTHORITY + "/person");
		public static final Uri CONTENT_URI_PAIRING = Uri.parse("content://" + AUTHORITY + "/pairing");

	}

	private static final UriMatcher sURIMatcher = buildUriMatcher();

	// UriMatcher stuff
	private static final int PERSONS = 0;
	private static final int PAIRINGS = 1;

	/**
	 * Builds up a UriMatcher for search suggestion and shortcut refresh
	 * queries.
	 */
	private static UriMatcher buildUriMatcher() {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		// to get definitions...
		matcher.addURI(Constants.AUTHORITY, "person", PERSONS);
		matcher.addURI(Constants.AUTHORITY, "pairing", PAIRINGS);
		return matcher;
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case PERSONS:
			return PersonProvider.PERSONS_LIST_MIME_TYPE;
		case PAIRINGS:
			return PairingProvider.PAIRING_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.d(TAG, "query for uri : " + uri); 
		switch (sURIMatcher.match(uri)) {
		case PERSONS:
			return queryPerson(uri, projection, selection, selectionArgs, sortOrder);
		case PAIRINGS:
			return queryPairing(uri, projection, selection, selectionArgs, sortOrder);
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}
	
	private Cursor queryPerson(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.i(TAG, "Widget query Person with Uri : " + uri);
		Cursor cursor = getContext().getContentResolver().query(PersonProvider.Constants.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
		return cursor;
	}

	private Cursor queryPairing(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.i(TAG, "Widget query Pairing with Uri : " + uri);
		Cursor cursor = getContext().getContentResolver().query(PairingProvider.Constants.CONTENT_URI, projection, selection, selectionArgs, sortOrder);
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		 throw new IllegalArgumentException("Not Authorize Insert Uri: " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		 throw new IllegalArgumentException("Not Authorize Delete Uri: " + uri);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		 throw new IllegalArgumentException("Not Authorize Update Uri: " + uri);

	}

}
