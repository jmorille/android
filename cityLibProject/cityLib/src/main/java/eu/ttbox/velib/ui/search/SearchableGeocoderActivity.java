package eu.ttbox.velib.ui.search;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import eu.ttbox.velib.GeocoderContentProvider;
import eu.ttbox.velib.GeocoderContentProvider.GeocoderColumns;
import eu.ttbox.velib.R;
import eu.ttbox.velib.VelibMapActivity;
import eu.ttbox.velib.core.Intents;
import eu.ttbox.velib.model.VelibProvider;

public class SearchableGeocoderActivity  extends ListActivity  {

	private static final String TAG = "SearchGeoActivity";

	 private TextView resultStatus;
	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_geoname); 
		// View Elemeny
		resultStatus = (TextView)findViewById(R.id.resultStatus);
		// handle Intent
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) { 
		handleIntent(intent);
	}



	private void handleIntent(Intent intent) {
//		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "Handle Intent for action : " + intent.getAction());
//		}
		// Search context
		VelibProvider velibProvider=null;
		Bundle appData = intent.getBundleExtra(SearchManager.APP_DATA);
		if (appData != null) {
			String velibProviderString = appData.getString(Intents.EXTRA_VELIB_PROVIDER);
			if (velibProviderString!=null) {
				velibProvider = VelibProvider.valueOf(VelibProvider.class, velibProviderString);
			}
		}
		// Parse Uri search
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			
			doSearch(query, velibProvider);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			setTitle(R.string.menu_search);
			// Handle a suggestions click (because the suggestions all use ACTION_VIEW)
			// Log.i(TAG, "*********** TODO show result intent"+ intent);
			Uri data = intent.getData();
			CharSequence userQuery = intent.getCharSequenceExtra(SearchManager.USER_QUERY);
			if (userQuery != null) {
				doSearch(userQuery.toString(), velibProvider);
			}
			// Show Result;
//			startMapActivityWithData(data); 
		} else {
			Log.w(TAG, "Not Handle Intent for for Action : " + intent.getAction());
		}
	}

	private void doSearch(String query, VelibProvider velibProvider) {
		String[] projection = null;
		String selection = null;
		String[] selectionArgs = null;
		String sortOrder = null;
		Uri geocoderUri = null;
		if (velibProvider!=null) {
			geocoderUri = Uri.withAppendedPath(GeocoderContentProvider.Constants.CONTENT_URI, String.format("%s/%s", velibProvider.getProviderName(), query)); 
		} else {
			geocoderUri = Uri.withAppendedPath(GeocoderContentProvider.Constants.CONTENT_URI, query);
		}
 		Cursor cursor = managedQuery(geocoderUri, projection, selection, selectionArgs, sortOrder);
  		showResult(cursor);
		
	}
	
	private void showResult(Cursor cursor) {
		if (cursor == null || cursor.getCount()<1) {
			// There are no results
			// mTextView.setText(getString(R.string.no_results, new Object[] {query}));
			resultStatus.setText(R.string.searchStation_resultCount_none);
			resultStatus.setVisibility(View.VISIBLE);
		} else {
			resultStatus.setVisibility(View.GONE);
			String[] from = new String[] { GeocoderColumns.COL_AddressLines, GeocoderColumns.COL_FeatureName }  ;
			int[] to = new int[] {R.id.geocoder_list_item_address, R.id.geocoder_list_item_ocupation };
			final SimpleCursorAdapter veloAdapter = new SimpleCursorAdapter(this, R.layout.geocoder_list_item, cursor,from, to);
			setListAdapter(veloAdapter);

			// Define the on-click listener for the list items
			getListView().setOnItemClickListener(new OnItemClickListener() {
				// @see http://www.mkyong.com/android/android-listview-example/
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Cursor cursor = (Cursor)veloAdapter.getItem(position);
					double lat = cursor.getDouble(cursor.getColumnIndex(GeocoderColumns.COL_Latitude));
					double lng = cursor.getDouble(cursor.getColumnIndex(GeocoderColumns.COL_Longitude));
					Uri data = Uri.withAppendedPath(GeocoderContentProvider.Constants.CONTENT_URI, String.format("geopoint/%s", id));
					startMapActivityWithData(data);
				}
			});
		}
	}
	
	private void startMapActivityWithData(Uri data) {
		if (data != null) {
			// Log.i(TAG, "*********** TODO show result data : "+ data);
			Intent mapIntent = new Intent(getApplicationContext(), VelibMapActivity.class);
			mapIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mapIntent.setData(data);
			startActivity(mapIntent);
		}
	}
}
