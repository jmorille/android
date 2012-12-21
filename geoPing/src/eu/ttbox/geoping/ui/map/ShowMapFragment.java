package eu.ttbox.geoping.ui.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapController.AnimationType;
import org.osmdroid.views.MapView;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.ui.map.core.MapConstants;
import eu.ttbox.geoping.ui.map.track.GeoTrackOverlay;
import eu.ttbox.geoping.ui.map.track.dialog.SelectGeoTrackDialog;
import eu.ttbox.geoping.ui.map.track.dialog.SelectGeoTrackDialog.OnSelectPersonListener;
import eu.ttbox.osm.ui.map.MapViewFactory;
import eu.ttbox.osm.ui.map.mylocation.MyLocationOverlay;

/**
 * @see http://mobiforge.com/developing/story/using-google-maps-android
 * 
 */
public class ShowMapFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = "ShowMapFragment";

	private static final int GEOTRACK_PERSON_LOADER = R.id.config_id_map_geotrack_person_loader;

	// Constant
	/**
	 * This number depend of previous menu
	 */
	private int MENU_LAST_ID = 3;

	// Map
	private MapController mapController;
	private MapView mapView;

	// Config
	private boolean geocodingAuto = true;

	// Overlay
	private MyLocationOverlay myLocation;
	// private GeoTrackOverlay geoTrackOverlay;
	private HashMap<String, GeoTrackOverlay> geoTrackOverlayByUser = new HashMap<String, GeoTrackOverlay>();

	// Listener
	private StatusReceiver mStatusReceiver;
	// Service
	private SharedPreferences sharedPreferences;
	private SharedPreferences privateSharedPreferences;
	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	// Deprecated
	private ResourceProxy mResourceProxy;

	// ===========================================================
	// Constructors
	// ===========================================================
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.map, container, false);

		// Prefs
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		privateSharedPreferences = getActivity().getSharedPreferences(MapConstants.PREFS_NAME, Context.MODE_PRIVATE);
		// Config
		geocodingAuto = sharedPreferences.getBoolean(AppConstants.PREFS_GEOPOINT_GEOCODING_AUTO, true);

		// Osm
		// ----------
		this.mResourceProxy = new DefaultResourceProxyImpl(getActivity().getApplicationContext());
		ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		// Maps
		ITileSource tileSource = getPreferenceMapViewTileSource();
		mapView = MapViewFactory.createOsmMapView(getActivity().getApplicationContext(), mResourceProxy, tileSource, activityManager);
		ViewGroup mapViewContainer = (ViewGroup) v.findViewById(R.id.mapViewContainer);
		mapViewContainer.addView((View) mapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		// Map Controler
		mapController = mapView.getController();
		// mapController.setZoom(17); // Zoon 1 is world view

		// Overlay
		// ----------
		this.myLocation = new MyLocationOverlay(getActivity(), this.mapView); // .getBaseContext()
		mapView.getOverlays().add(myLocation);
		// Service
		mStatusReceiver = new StatusReceiver();
		// Query
		getActivity().getSupportLoaderManager().initLoader(GEOTRACK_PERSON_LOADER, null, geoTrackPersonLoaderCallback);

		return v;
	}

	// ===========================================================
	// Life Cycle
	// ===========================================================

	@Override
	public void onDestroy() {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "### ### ### ### ### onDestroy call ### ### ### ### ###");
		}
		myLocation.disableCompass();
		myLocation.disableMyLocation();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
	}

	@Override
	public void onResume() {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "### ### ### ### ### onResume call ### ### ### ### ###");
		}
		super.onResume();

		// read preference
		ITileSource tileSource = getPreferenceMapViewTileSource();
		mapView.setTileSource(tileSource);

		// Zoon 1 is world view
		mapView.getController().setZoom(privateSharedPreferences.getInt(MapConstants.PREFS_ZOOM_LEVEL, 17));
		// Center
		int scrollX = privateSharedPreferences.getInt(MapConstants.PREFS_SCROLL_X, Integer.MIN_VALUE);
		int scrollY = privateSharedPreferences.getInt(MapConstants.PREFS_SCROLL_Y, Integer.MIN_VALUE);
		if (Integer.MIN_VALUE != scrollX && Integer.MIN_VALUE != scrollY) {
//			mapView.scrollTo(scrollX, scrollY);
			// mapView.scrollTo(privateSharedPreferences.getInt(MapConstants.PREFS_SCROLL_X,
			// 0), privateSharedPreferences.getInt(MapConstants.PREFS_SCROLL_Y,
			// 0));
		}
		// Options
		if (privateSharedPreferences.getBoolean(MapConstants.PREFS_SHOW_LOCATION, false)) {
			this.myLocation.enableMyLocation(true);
		}

		if (privateSharedPreferences.getBoolean(MapConstants.PREFS_SHOW_COMPASS, false)) {
			this.myLocation.enableCompass(true);
		}

		// Service
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intents.ACTION_NEW_GEOTRACK_INSERTED);
		getActivity().registerReceiver(mStatusReceiver, filter);

		// Overlay MyLocation
		if (myLocation != null) {
			myLocation.onResume();
		}
	}

	@Override
	public void onPause() {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "### ### ### ### ### onPause call ### ### ### ### ###");
		}
		// save Preference
		// final SharedPreferences.Editor edit = sharedPreferences.edit();
		// edit.putString(AppConstants.PREFS__KEY_TILE_SOURCE,
		// mapView.getTileProvider().getTileSource().name());
		// edit.commit();

		// Priavte Preference
		final SharedPreferences.Editor localEdit = privateSharedPreferences.edit();
		localEdit.putString(MapConstants.PREFS_TILE_SOURCE, mapView.getTileProvider().getTileSource().name());
		localEdit.putInt(MapConstants.PREFS_SCROLL_X, mapView.getScrollX());
		localEdit.putInt(MapConstants.PREFS_SCROLL_Y, mapView.getScrollY());
		localEdit.putInt(MapConstants.PREFS_ZOOM_LEVEL, mapView.getZoomLevel());
		localEdit.putBoolean(MapConstants.PREFS_SHOW_LOCATION, myLocation.isMyLocationEnabled());
		localEdit.putBoolean(MapConstants.PREFS_SHOW_COMPASS, myLocation.isCompassEnabled());
		localEdit.commit();

		// Service
		getActivity().unregisterReceiver(mStatusReceiver);

		// Overlay May Location
		if (myLocation != null) {
			myLocation.onPause();
		}

		// Overlay GeoTrack
		// if (!geoTrackOverlayByUser.isEmpty()) {
		// for (Map.Entry<String, GeoTrackOverlay> entry :
		// geoTrackOverlayByUser.entrySet()) {
		// String key = entry.getKey();
		// GeoTrackOverlay geoTrackOverlay = entry.getValue();
		// geoTrackOverlay.onPause();
		// }
		// }

		super.onPause();
		// timer.cancel();
	}

	// ===========================================================
	// Map Tile
	// ===========================================================

	public ITileSource getMapViewTileSource() {
		return mapView.getTileProvider().getTileSource();
	}

	public void setMapViewTileSource(ITileSource tileSource) {
		mapView.setTileSource(tileSource);
	}

	public ArrayList<ITileSource> getMapViewTileSources() {
		return TileSourceFactory.getTileSources();
	}

	public String getMapViewTileSourceName(ITileSource tileSource) {
		return tileSource.localizedName(mResourceProxy);
	}

	private ITileSource getPreferenceMapViewTileSource() {
		final String tileSourceName = privateSharedPreferences.getString(MapConstants.PREFS_TILE_SOURCE, TileSourceFactory.DEFAULT_TILE_SOURCE.name());
		ITileSource tileSource = null;
		try {
			tileSource = TileSourceFactory.getTileSource(tileSourceName);
		} catch (final IllegalArgumentException ignore) {
		}
		return tileSource;
	}

	// ===========================================================
	// Map Action
	// ===========================================================

	public void centerOnMyPosition() {
		mapView.getScroller().forceFinished(true);
		myLocation.enableFollowLocation();
		myLocation.runOnFirstFix(new Runnable() {

			@Override
			public void run() { 
//				myLocation.animateToLastFix();
				mapController.setZoom(17);
			}
		});
	}

	public void centerOnPersonPhone(final String phone, final int latE6, final int lngE6) {
		if (myLocation != null) {
			myLocation.disableFollowLocation();
		}
		executor.execute(new Runnable() {
			@Override
			public void run() {
				// Animate to
				if (Integer.MIN_VALUE != latE6 && Integer.MIN_VALUE != lngE6) {
					GeoPoint geoPoint = new GeoPoint(latE6, lngE6);
					mapController.setCenter(geoPoint);
//					mapController.animateTo(latE6, lngE6, AnimationType.HALFCOSINUSALDECELERATING);
				}
				// Display GeoPoints for person
				GeoTrackOverlay geoTrackOverlay = geoTrackOverlayGetOrAddForPhone(phone);
			}
		});
	}

	// ===========================================================
	// Select Person Dialog
	// ===========================================================

	public void showSelectPersonDialog() {
		LoaderManager loaderManager = getActivity().getSupportLoaderManager();
		SelectGeoTrackDialog personListDialod = new SelectGeoTrackDialog(getActivity(), loaderManager, onSelectPersonListener, geoTrackOverlayByUser);
		personListDialod.show();
	}

	private OnSelectPersonListener onSelectPersonListener = new OnSelectPersonListener() {

		@Override
		public void onDoRemovePerson(Person person) {
			geoTrackOverlayRemovePerson(person);
		}

		@Override
		public void onDoAddPerson(Person person) {
			geoTrackOverlayAddPerson(person);
		}

		@Override
		public void onSelectPerson(Person person) {
			geoTrackOverlayAnimateToLastKnowPosition(person.phone);
		}

		@Override
		public void onNoPerson(SelectGeoTrackDialog dialog) {
			// Open the Creation Person
			Intent intent = Intents.activityMain(getActivity());
			startActivity(intent);
		}
	};

	// ===========================================================
	// GeoTrack Overlay
	// ===========================================================

	private GeoTrackOverlay geoTrackOverlayGetOrAddForPhone(String phone) {
		GeoTrackOverlay geoTrackOverlay = geoTrackOverlayByUser.get(phone);
		// Add person layer
		if (geoTrackOverlay == null) {
			Person person = null;
			Cursor cursor = getActivity().getContentResolver().query(PersonProvider.Constants.CONTENT_URI, null, PersonColumns.SELECT_BY_PHONE_NUMBER, new String[] { phone }, null);
			if (cursor.moveToFirst()) {
				PersonHelper helper = new PersonHelper().initWrapper(cursor);
				person = helper.getEntity(cursor);
				cursor.close();
			}
			if (person != null) {
				geoTrackOverlay = geoTrackOverlayAddPerson(person);
			}
		}

		return geoTrackOverlay;
	}

	private GeoTrackOverlay geoTrackOverlayAddPerson(Person person) {
		GeoTrackOverlay geoTrackOverlay = null;
		boolean isDone = false;
		String userId = person.phone;
		if (!TextUtils.isEmpty(userId) && !geoTrackOverlayByUser.containsKey(userId)) {
			LoaderManager loaderManager = getActivity().getSupportLoaderManager();
			// Overlay .getBaseContext()
			geoTrackOverlay = new GeoTrackOverlay(getActivity(), this.mapView, loaderManager, person, System.currentTimeMillis(), geocodingAuto);
			geoTrackOverlayByUser.put(userId, geoTrackOverlay);
			// register
			isDone = mapView.getOverlays().add(geoTrackOverlay);
			Log.i(TAG, String.format("Add New GeoTrack Overlay (%s) for %s", isDone, person));
		} else {
			Log.e(TAG, String.format("Could not Add person %s in geoTrackOverlayByUser (It already in List)", person));
		}
		if (!isDone) {
			geoTrackOverlay = null;
		}
		return geoTrackOverlay;
	}

	private boolean geoTrackOverlayRemovePerson(Person person) {
		boolean isDone = false;
		Log.d(TAG, String.format("Want to remove New GeoTrack Overlay for %s", person));
		String userId = person.phone;
		if (geoTrackOverlayByUser.containsKey(userId)) {
			GeoTrackOverlay geoTrackOverlay = geoTrackOverlayByUser.remove(userId);
			isDone = mapView.getOverlays().remove(geoTrackOverlay);
			geoTrackOverlay.onDetach(mapView);
			Log.i(TAG, String.format("Remove GeoTrack Overlay (%s) for %s", isDone, person));
		} else {
			Log.e(TAG, String.format("Could not remove person %s in geoTrackOverlayByUser", person));
		}
		return isDone;
	}

	private boolean geoTrackOverlayAnimateToLastKnowPosition(String userId) {
		boolean isDone = false;
		if (geoTrackOverlayByUser.containsKey(userId)) {
			GeoTrackOverlay geoTrackOverlay = geoTrackOverlayByUser.get(userId);
			geoTrackOverlay.animateToLastKnowPosition();
			if (myLocation != null) {
				myLocation.disableFollowLocation();
			}
			isDone = true;
		} else {
			Log.e(TAG, String.format("Could not Animate to last position of person %s in geoTrackOverlayByUser", userId));
			for (String key : geoTrackOverlayByUser.keySet()) {
				Log.e(TAG, String.format("geoTrackOverlayByUser contains Key : %s", key));
			}
		}
		Log.d(TAG, String.format("animateToLastKnowPosition for User : %s (is done %s)", userId, isDone));
		return isDone;
	}

	// ===========================================================
	// Loader
	// ===========================================================

	private final LoaderManager.LoaderCallbacks<Cursor> geoTrackPersonLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			Log.d(TAG, "onCreateLoader");
			String sortOrder = PersonColumns.ORDER_NAME_ASC;
			String selection = PersonColumns.SELECT_BYPHONE_NUMBER_NOT_NULL;//  null;
			String[] selectionArgs = null;
			// Loader
			CursorLoader cursorLoader = new CursorLoader(getActivity(), PersonProvider.Constants.CONTENT_URI, null, selection, selectionArgs, sortOrder);
			return cursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			int resultCount = cursor.getCount();
			Log.d(TAG, String.format("onLoadFinished with %s results", resultCount));
			if (cursor.moveToFirst()) {
				PersonHelper helper = new PersonHelper().initWrapper(cursor);
				do {
					Person pers = helper.getEntity(cursor);
					Log.d(TAG, String.format("Add Person with phone : %s", pers));
					geoTrackOverlayAddPerson(pers);
				} while (cursor.moveToNext());
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			// for (Map.Entry<String, GeoTrackOverlay> entry :
			// geoTrackOverlayByUser.entrySet()) {
			// String key = entry.getKey();
			// removeGeoTrackOverlay(key);
			// }
		}

	};

	// ===========================================================
	// Listeners
	// ===========================================================

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(AppConstants.PREFS_GEOPOINT_GEOCODING_AUTO)) {
			geocodingAuto = sharedPreferences.getBoolean(AppConstants.PREFS_GEOPOINT_GEOCODING_AUTO, true);
		}
	}

	private class StatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.e(TAG, "########################################");
			Log.e(TAG, "ShwoMap StatusReceiver onReceive  action : " + action);
			if (Intents.ACTION_NEW_GEOTRACK_INSERTED.equals(action)) {
			}
		}
	};
	// ===========================================================
	// Others
	// ===========================================================

}
