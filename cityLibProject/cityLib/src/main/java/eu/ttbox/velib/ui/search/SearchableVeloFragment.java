package eu.ttbox.velib.ui.search;

import android.annotation.TargetApi;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import org.osmdroid.util.GeoPoint;

import eu.ttbox.osm.ui.map.mylocation.sensor.LocationUtils;
import eu.ttbox.velib.R;
import eu.ttbox.velib.VelibMapActivity;
import eu.ttbox.velib.VeloContentProvider;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.database.Velo.VeloColumns;
import eu.ttbox.velib.ui.map.VelibProviderHelper;
import eu.ttbox.velib.ui.search.adapter.StationItemCurAdapter;

public class SearchableVeloFragment extends Fragment {

    private static final String TAG = "SearchableVeloFragment";

    // Constant
    private static final int PERSON_LIST_LOADER = R.id.config_id_search_velib_list_loader;
    private static final String SEARCH_STATION_SORT_DEFAULT = String.format("%s ASC", VeloColumns.COL_NAME);
    private static final String[] SEARCH_PROJECTION_COLOMN = new String[] { //
    VeloColumns.COL_ID, VeloColumns.COL_NUMBER, VeloColumns.COL_NAME, VeloColumns.COL_ADDRESS//
            , VeloColumns.COL_FAVORY, VeloColumns.COL_FAVORY_TYPE//
            , VeloColumns.COL_ALIAS_NAME, VeloColumns.COL_STATION_UPDATE_TIME//
            , VeloColumns.COL_STATION_CYCLE, VeloColumns.COL_STATION_PARKING//
            , VeloColumns.COL_LATITUDE_E6, VeloColumns.COL_LONGITUDE_E6//
            , SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID //
    };

    // Config
    private static final long DEFAULT_DISPO_DELTA_DELAY_IN_S = 60 * 5; 
    
    long  checkDispoDeltaDelayInMs = AppConstants.ONE_SECOND_IN_MS *DEFAULT_DISPO_DELTA_DELAY_IN_S ;
    
//     // Service
    private LocationManager locationManager;
    private SensorManager mSensorManager;
    private SharedPreferences sharedPreferences;
    
    private StationItemCurAdapter listAdapter;

    // Binding
    private TextView resultStatus;
    private ListView mListView;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.search_station, container, false);
        // Service
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
         mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
         // Config
         checkDispoDeltaDelayInMs = sharedPreferences.getLong(AppConstants.PREFS_KEY_CHEK_DISPO_DELTA_DELAY_IN_S, DEFAULT_DISPO_DELTA_DELAY_IN_S)  * AppConstants.ONE_SECOND_IN_MS;
         
        // Adpater
        Location lastLoc = getLastKnownLocation();
        final WindowManager windowManager = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        Display mDisplay = windowManager.getDefaultDisplay();
      
        listAdapter = new StationItemCurAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER, lastLoc, mDisplay, checkDispoDeltaDelayInMs);
         // Binding
        resultStatus = (TextView) v.findViewById(R.id.searchStation_resultStatus);
        mListView = (ListView) v.findViewById(android.R.id.list);
        // Define the on-click listener for the list items
        mListView.setOnItemClickListener(new OnItemClickListener() {
            // @see http://www.mkyong.com/android/android-listview-example/
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri data = Uri.withAppendedPath(VeloContentProvider.Constants.CONTENT_URI, String.format("station/%s", id));
                startMapActivityWithData(data);
            }
        });
        mListView.setAdapter(listAdapter);
        return v;
    }

    // ===========================================================
    // Life Cycle
    // ===========================================================

    @Override
    public void onResume() {
        super.onResume();
        // Service
        Location lastLoc = LocationUtils.getLastKnownLocation(locationManager);
        if (lastLoc!=null) {
        	listAdapter.onLocationChanged(lastLoc);
        }
        for (String provider : locationManager.getAllProviders() ) {
            locationManager.requestLocationUpdates(provider, 1000l, 0l, listAdapter);
        }
        final Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        if (sensor != null) {
              mSensorManager.registerListener(listAdapter, sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        locationManager.removeUpdates(listAdapter);
        mSensorManager.unregisterListener(listAdapter);
        super.onPause();
    }

    // ===========================================================
    // Location
    // ===========================================================

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

    // ===========================================================
    // Intent
    // ===========================================================

    public void startMapActivityWithData(Uri data) {
        if (data != null) {
            // Log.i(TAG, "*********** TODO show result data : "+ data);
            Intent mapIntent = new Intent(getActivity(), VelibMapActivity.class) //
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)//
                    .setData(data);
            startActivity(mapIntent);
        }
    }

    // ===========================================================
    // Service Accessors
    // ===========================================================

    private static final String SEARCH_KEY_QUERY = "SEARCH_KEY_QUERY";
    private static final String SEARCH_KEY_VELIB_PROVIDER_ORDINAL = "SEARCH_KEY_VELIB_PROVIDER_ORDINAL";
    private static final String SEARCH_KEY_IS_FAVORITE = "SEARCH_KEY_IS_FAVORITE";

    public void doSearch(String query) {
        Log.d(TAG, "Ask to do Search with query : " + query);
        Bundle args = new Bundle();
        args.putBoolean(SEARCH_KEY_IS_FAVORITE, false);
        args.putString(SEARCH_KEY_QUERY, query);
        LoaderManager loaderManager =   getActivity().getSupportLoaderManager();
        loaderManager.restartLoader(PERSON_LIST_LOADER, args,searchLoaderCallback);
//       loaderManager.initLoader(PERSON_LIST_LOADER, args, searchLoaderCallback);
    }

    public void doSearchFavorite(int velibProvider) {
        if (velibProvider < 0) {
            Location lastLocation =  getLastKnownLocation();
            GeoPoint lastKnownLocationAsGeoPoint = lastLocation!=null ? new GeoPoint(lastLocation) : null;
            VelibProvider velibProviderEnum =  VelibProviderHelper.computeConditionVelibProvider(sharedPreferences, lastKnownLocationAsGeoPoint);
            if (velibProviderEnum!=null) {
                velibProvider = velibProviderEnum.getProvider();
            }
        }
        Bundle args = new Bundle();
        args.putBoolean(SEARCH_KEY_IS_FAVORITE, true);
        args.putString(SEARCH_KEY_VELIB_PROVIDER_ORDINAL, String.valueOf(velibProvider) );
        LoaderManager loaderManager =   getActivity().getSupportLoaderManager();
        loaderManager.restartLoader(PERSON_LIST_LOADER, args,searchLoaderCallback);
//        loaderManager.initLoader(PERSON_LIST_LOADER, args, searchLoaderCallback);
    }

    @TargetApi(11)
    public OnQueryTextListener getOnQueryTextListener() {
        return onQueryTextListener;
    }
    
    // ===========================================================
    // Loader
    // ===========================================================

    private Bundle getLastQueryParam() {
        Bundle args = new Bundle();
        args.putBoolean(SEARCH_KEY_IS_FAVORITE, false);
       return args;
    }
    
    private final OnQueryTextListener onQueryTextListener = new OnQueryTextListener() {

        @Override
        public boolean onQueryTextSubmit(String query) {
            Log.d(TAG, "onQueryTextSubmit  with query : " + query);
            
            Bundle args = getLastQueryParam();
            args.putString(SEARCH_KEY_QUERY, query);
            LoaderManager loaderManager =   getActivity().getSupportLoaderManager();
            loaderManager.restartLoader(PERSON_LIST_LOADER, args,searchLoaderCallback);
//            getActivity().getSupportLoaderManager().initLoader(PERSON_LIST_LOADER, args, searchLoaderCallback);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            Log.d(TAG, "onQueryTextChange  with query : " + newText);
            Bundle args = getLastQueryParam();
            args.putString(SEARCH_KEY_QUERY, newText);
            LoaderManager loaderManager =   getActivity().getSupportLoaderManager();
            loaderManager.restartLoader(PERSON_LIST_LOADER, args,searchLoaderCallback);
//            getActivity().getSupportLoaderManager().initLoader(PERSON_LIST_LOADER, args, searchLoaderCallback);
            return true;
        }
        
    };


    private final LoaderManager.LoaderCallbacks<Cursor> searchLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String sortOrder = SEARCH_STATION_SORT_DEFAULT;
            String selection = null;
            String[] selectionArgs = null;
            // Create Query
            Uri searchUri = null;
            boolean searchFavorite = args.getBoolean(SEARCH_KEY_IS_FAVORITE, false);
            if (searchFavorite) {
                String velibProvider = args.getString(SEARCH_KEY_VELIB_PROVIDER_ORDINAL);
                searchUri = Uri.withAppendedPath(VeloContentProvider.Constants.CONTENT_URI_FAVORITE, velibProvider.toString());
            } else {
                String query = args.getString(SEARCH_KEY_QUERY);
                searchUri = Uri.withAppendedPath(VeloContentProvider.Constants.CONTENT_URI, String.format("%s/%s", SearchManager.SUGGEST_URI_PATH_QUERY, query));
            }
            // Loader
            CursorLoader cursorLoader = new CursorLoader(getActivity(), searchUri, SEARCH_PROJECTION_COLOMN, selection, selectionArgs, sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.d(TAG, "onLoadFinished");
            // Display List
            listAdapter.changeCursor(cursor);
            cursor.setNotificationUri(getActivity().getContentResolver(), VeloContentProvider.Constants.CONTENT_URI);
            // Display Counter
            int count = 0;
            if (cursor != null) {
                count = cursor.getCount();
            }
            if (count < 1) {
                resultStatus.setText(R.string.searchStation_resultCount_none);
                resultStatus.setVisibility(View.VISIBLE);
            } else {
                resultStatus.setVisibility(View.GONE);
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            listAdapter.changeCursor(null);
        }

    };
}
