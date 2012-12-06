package eu.ttbox.velib;

import java.io.IOException;
import java.util.List;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.model.VelibProvider;

/**
 * {link http://code.google.com/p/android/issues/detail?id=8816}
 *  
 * 
 */
public class GeocoderContentProvider extends ContentProvider {

	private final String TAG = "GeocoderContentProvider";

	private Geocoder geocoder;

	private static final UriMatcher sURIMatcher;
	private static final int GEOCODE_ADDRESS = 1;
	private static final int GEOCODE_ADDRESS_BY_PROVIDER = 2;
	private static final int SEARCH_SUGGEST_BY_PROVIDER = 3;
	private static final int SEARCH_SUGGEST = 4;
	private static final int REFRESH_SHORTCUT = 5;

	public static class Constants {
		public static final String AUTHORITY = "eu.ttbox.velib.GeocoderContentProvider";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/geocoder");

		public static final String COLLECTION_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.ttbox.velib.geocoder";
		public static final String ITEM_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.ttbox.velib.geocoder";
	}

	static {
		UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		// to get definitions...
		for (VelibProvider prov : VelibProvider.values()) {
			matcher.addURI(Constants.AUTHORITY, "geocoder/" + prov.getProviderName() + "/*", GEOCODE_ADDRESS_BY_PROVIDER);
			matcher.addURI(Constants.AUTHORITY,  prov.getProviderName() + "/"+SearchManager.SUGGEST_URI_PATH_QUERY , SEARCH_SUGGEST_BY_PROVIDER);
			matcher.addURI(Constants.AUTHORITY,  prov.getProviderName() + "/"+SearchManager.SUGGEST_URI_PATH_QUERY +"/*", SEARCH_SUGGEST_BY_PROVIDER);
		}
		matcher.addURI(Constants.AUTHORITY, "geocoder/*", GEOCODE_ADDRESS);
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

	public static class GeocoderColumns {
		public static final String COL_ID = BaseColumns._ID;
		public static final String COL_FeatureName = SearchManager.SUGGEST_COLUMN_TEXT_1; // "COL_FeatureName";
		public static final String COL_AddressLines = SearchManager.SUGGEST_COLUMN_TEXT_2; // "COL_AddressLines";
		public static final String COL_AdminArea = "COL_AdminArea";
		public static final String COL_SubAdminArea = "COL_SubAdminArea";
		public static final String COL_Locality = "COL_Locality";
		public static final String COL_SubLocality = "COL_SubLocality";
		public static final String COL_Thoroughfare = "COL_Thoroughfare";
		public static final String COL_SubThoroughfare = "COL_SubThoroughfare";
		public static final String COL_Premises = "COL_Premises";
		public static final String COL_PostalCode = "COL_PostalCode";
		public static final String COL_CountryCode = "COL_CountryCode";
		public static final String COL_CountryName = "COL_CountryName";
		public static final String COL_Latitude = "COL_Latitude";
		public static final String COL_Longitude = "COL_Longitude";
		public static final String COL_Phone = "COL_Phone";
		public static final String COL_Url = "COL_Url";

		public static final String[] COL_ALL = new String[] { //
		COL_ID //
				, COL_FeatureName, COL_AddressLines //
				, COL_AdminArea, COL_SubAdminArea, COL_Locality, COL_SubLocality //
				, COL_Thoroughfare, COL_SubThoroughfare //
				, COL_Premises, COL_PostalCode //
				, COL_CountryCode, COL_CountryName //
				, COL_Latitude, COL_Longitude //
				, COL_Phone, COL_Url };

	}

	private Object[] getAddressAsCursorValues(Address addr) {
		StringBuffer addrLines = new StringBuffer();
		int maxAddrLines = addr.getMaxAddressLineIndex();
		for (int i = 0; i <= maxAddrLines; i++) {
			addrLines.append(addr.getAddressLine(i));
			if (i != maxAddrLines) {
				addrLines.append(", ");
			}
		}
		String id = "1";

		Object[] columnValues = { id //
				, addr.getFeatureName(), addrLines //
				, addr.getAdminArea(), addr.getSubAdminArea(), addr.getLocality(), addr.getSubLocality() //
				, addr.getThoroughfare(), addr.getSubThoroughfare() //
				, addr.getPremises(), addr.getPostalCode() //
				, addr.getCountryCode(), addr.getCountryName() //
				, addr.getLatitude(), addr.getLongitude()//
				, addr.getPhone(), addr.getUrl() //
		};
		Log.d(TAG, String.format("FeatureName=%s / AddressLines=%s", addr.getFeatureName(), addrLines));
		return columnValues;
	}

	@Override
	public boolean onCreate() {
		geocoder = new Geocoder(getContext()); 
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case GEOCODE_ADDRESS_BY_PROVIDER:
		case GEOCODE_ADDRESS:
			return Constants.COLLECTION_MIME_TYPE;
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;
		case REFRESH_SHORTCUT:
			return SearchManager.SHORTCUT_MIME_TYPE;
		}
		return Constants.COLLECTION_MIME_TYPE;
	}

	private VelibProvider getVelibProvider(List<String> pathSegments, int postion) {
		VelibProvider provider = null;
		String providerString = pathSegments.get(postion);
		if (providerString != null) {
			provider = VelibProvider.valueOf(VelibProvider.class, providerString);
		}
		return provider;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		Log.i(TAG,String.format( "Geocoder query : %s / selection : %s / selectionArgs : %s" ,uri  ,selection, selectionArgs));

		String query = null;
		VelibProvider provider = null;

		switch (sURIMatcher.match(uri)) {
		case SEARCH_SUGGEST_BY_PROVIDER:
			provider = getVelibProvider(uri.getPathSegments(), 0); 
			query = uri.getLastPathSegment();
			break;
		case SEARCH_SUGGEST:
			query = uri.getLastPathSegment();
			break;
		case GEOCODE_ADDRESS_BY_PROVIDER:
			provider = getVelibProvider(uri.getPathSegments(), 1); 
			query = uri.getLastPathSegment();
			break;
		case GEOCODE_ADDRESS:
			query = uri.getLastPathSegment();
			break;
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
		int maxResults = 10;
		return getGeocodeFromLocation(query, provider, maxResults);
	}

	private Cursor getGeocodeFromLocation(String locationName, VelibProvider provider, int maxResults) {
		boolean isBoundyBox = false;
		double lowerLeftLatitude = 0;
		double lowerLeftLongitude = 0;
		double upperRightLatitude = 0;
		double upperRightLongitude = 0;
		if (provider != null) {
			double[] boundyBox = provider.getBoundyBoxE6();
			lowerLeftLatitude = boundyBox[0] / AppConstants.E6;
			lowerLeftLongitude = boundyBox[1] / AppConstants.E6;
			upperRightLatitude = boundyBox[2] / AppConstants.E6;
			upperRightLongitude = boundyBox[3] / AppConstants.E6;
			isBoundyBox = true;
		}
		// Search
		List<Address> addresses = null;
		try {
			if (isBoundyBox) {
				addresses = geocoder.getFromLocationName(locationName, maxResults, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);
			} else {
				addresses = geocoder.getFromLocationName(locationName, maxResults);
			}
		} catch (IOException ioe) {
			Log.e(TAG, ioe.getMessage());
		}
		// Log.d(TAG, "getFromLocationName([" + locationName + "])=> " +
		// addresses);
		// Wraping as cursor
		GeocoderPointCursor cursor = null;
		if (addresses != null && !addresses.isEmpty()) {
			cursor = new GeocoderPointCursor(GeocoderColumns.COL_ALL);
			for (Address addr : addresses) {
				Object[] columnValues = getAddressAsCursorValues(addr);
				cursor.addRow(columnValues);
			}
		}
		return cursor;

	}

	private class GeocoderPointCursor extends MatrixCursor {
		public GeocoderPointCursor(String[] columnNames) {
			super(columnNames);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return 0;
	}

}
