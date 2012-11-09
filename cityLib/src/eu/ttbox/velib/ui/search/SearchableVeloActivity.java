package eu.ttbox.velib.ui.search;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import eu.ttbox.velib.R;
import eu.ttbox.velib.VelibMapActivity;
import eu.ttbox.velib.VeloContentProvider;
import eu.ttbox.velib.core.Intents;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.search.StationRecentSearchRecentSuggestionsProvider;
import eu.ttbox.velib.service.database.Velo.VeloColumns;
import eu.ttbox.velib.ui.preference.VelibPreferenceActivity;
import eu.ttbox.velib.ui.search.adapter.StationItemCurAdapter;

/**
 * General configuration of Search Dialog: @see http://developer.android.com/guide/topics/search/search-dialog.html
 * 
 * For sample implementation :
 * 
 * @see http ://developer.android.com/resources/samples/SearchableDictionary/src/com /example/android/searchabledict/SearchableDictionary.html
 * 
 *      For howto use cursor:
 * 
 * @see http://mobile.tutsplus.com/tutorials/android/android-sdk_loading- data_cursorloader/
 */
public class SearchableVeloActivity extends ListActivity {

	private static final String TAG = "SearchableVeloActivity";

    public static final String ACTION_VIEW_FAVORITE = "eu.ttbox.velib.ui.search.ACTION_VIEW_FAVORITE";

    private static final String[] SEARCH_PROJECTION_COLOMN = new String[] { VeloColumns.COL_ID, VeloColumns.COL_NUMBER, VeloColumns.COL_NAME, VeloColumns.COL_ADDRESS,
    	VeloColumns.COL_FAVORY, VeloColumns.COL_FAVORY_TYPE, 
			VeloColumns.COL_ALIAS_NAME, VeloColumns.ALIAS_COL_DISPO_CYCLE_PARKING, VeloColumns.ALIAS_COL_LAT_LNG_E6 ,SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };

	 private TextView resultStatus;
	// private ListView mListView;

	private LocationManager locationManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_station);
		// Captor
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		// View Elemeny
		resultStatus = (TextView)findViewById(R.id.searchStation_resultStatus);
		// handle Intent
		handleIntent(getIntent());
	}

	@Override
	protected void onNewIntent(Intent intent) { 
		handleIntent(intent);
	}



	private void handleIntent(Intent intent) {
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, "handleIntent for action : " + intent.getAction());
		}

		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			setTitle(R.string.menu_search);
			// handles a search query
			String query = intent.getStringExtra(SearchManager.QUERY);
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this, StationRecentSearchRecentSuggestionsProvider.AUTHORITY,
					StationRecentSearchRecentSuggestionsProvider.MODE);
			suggestions.saveRecentQuery(query, null);
			// Log.d(TAG, "---------------------------------------------------------");
			// Log.d(TAG, "ContentProvider Search suggest for " + query);
			// Log.d(TAG, "---------------------------------------------------------");
			doSearch(query);
		} else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			setTitle(R.string.menu_search);
			// Handle a suggestions click (because the suggestions all use ACTION_VIEW)
			// Log.i(TAG, "*********** TODO show result intent"+ intent);
			Uri data = intent.getData();
			CharSequence userQuery = intent.getCharSequenceExtra(SearchManager.USER_QUERY);
			if (userQuery != null) {
				doSearch(userQuery.toString());
			}
			// Show Result;
			startMapActivityWithData(data);
		} else if (ACTION_VIEW_FAVORITE.equals((intent.getAction()))) {
			setTitle(R.string.menu_favorite);
			Integer velibProvider = intent.getIntExtra( Intents.EXTRA_VELIB_PROVIDER , -1);
 			doSearchFavorite(velibProvider);
		} else {
			Log.w(TAG, "Not Handle Intent for for Action : " + intent.getAction());
		}
	}

	public Location getLastKnownLocation() {
		// Location loc = null;
		// Criteria
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		// Ask Last Location
		Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
 		return lastKnownLocation;
	}
	
	private void doSearch(String query ) {
		String selection = null;
		String[] selectionArgs = null; // new String[] { query };
		String sortOrder = String.format("%s ASC", VeloColumns.COL_NAME);
		// CursorLoader cursorLoader = new CursorLoader(this,
		// VeloContentProvider.CONTENT_URI, projection, selection, selectionArgs, sortOrder);

		Uri myPerson = Uri.withAppendedPath(VeloContentProvider.Constants.CONTENT_URI, String.format("%s/%s", SearchManager.SUGGEST_URI_PATH_QUERY, query));
 		Cursor cursor = managedQuery(myPerson, SEARCH_PROJECTION_COLOMN, selection, selectionArgs, sortOrder);
  		showResult(cursor);
	}
	private void doSearchFavorite(Integer velibProvider) {
		String selection = null;
		String[] selectionArgs = null; // new String[] { query };
		String sortOrder = String.format("%s ASC", VeloColumns.COL_NAME);
		// CursorLoader cursorLoader = new CursorLoader(this,
		// VeloContentProvider.CONTENT_URI, projection, selection, selectionArgs, sortOrder);

		Uri myPerson = Uri.withAppendedPath(VeloContentProvider.Constants.CONTENT_URI_FAVORITE, velibProvider.toString());
 		Cursor cursor = managedQuery(myPerson, SEARCH_PROJECTION_COLOMN, selection, selectionArgs, sortOrder);
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
//			resultStatus.setText(R.string.searchStation_resultCount_none);
			// Display the number of results
			// int count = cursor.getCount();
			// String countString = getResources().getQuantityString(R.plurals.search_results,
			// count, new Object[] {count, query});
			// mTextView.setText(countString);

			// Specify the columns we want to display in the result to the corresponding layout elements
			//TODO ALIAS_COL_LAT_LNG_E6
			// TODO COL_ALIAS_NAME
			String[] from = new String[] { VeloColumns.COL_NUMBER, VeloColumns.COL_NAME, VeloColumns.COL_ADDRESS, VeloColumns.ALIAS_COL_DISPO_CYCLE_PARKING, 
					 VeloColumns.COL_FAVORY_TYPE }; // ,
			int[] to = new int[] { R.id.station_list_item_distance, R.id.station_list_item_ocupation, R.id.station_list_item_adress,
					R.id.station_list_item_dispo, R.id.station_list_item_icon_favorite }; // ,
			// Create a simple cursor adapter for the definitions and apply them to the ListView
			Location lastLoc = getLastKnownLocation();
			StationItemCurAdapter veloAdapter = new StationItemCurAdapter(this, R.layout.stations_list_item, cursor, from, to,
					SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER,   lastLoc );
			setListAdapter(veloAdapter);

			// Define the on-click listener for the list items
			getListView().setOnItemClickListener(new OnItemClickListener() {
				// @see http://www.mkyong.com/android/android-listview-example/
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Uri data = Uri.withAppendedPath(VeloContentProvider.Constants.CONTENT_URI, String.format("station/%s", id));
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Création d'un MenuInflater qui va permettre d'instancier un Menu XML
		// en un objet Menu
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//			SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//			searchView.setIconifiedByDefault(false);
//		}

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuOptions: {
			Intent intentOption = new Intent(this, VelibPreferenceActivity.class);
			startActivity(intentOption);
			return true;
		}
		case R.id.menu_search: {
			onSearchRequested();
			Log.d(TAG, "---------------  onSearchRequested  ---------------------------");
			return true;
		}
		case R.id.menuMap: {
			Intent intentMap = new Intent(this, VelibMapActivity.class);
			startActivity(intentMap);
			return true;
		}
		case R.id.menuQuit: {
			finish();
			return true;
		}
		}
		return false;
	}

}
