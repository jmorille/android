package eu.ttbox.geoping.ui.map;

import java.util.HashMap;
import java.util.Map;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.Person;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.ui.map.core.MapConstants;
import eu.ttbox.geoping.ui.map.core.MyAppTilesProviders;
import eu.ttbox.geoping.ui.map.mylocation.MyLocationOverlay;
import eu.ttbox.geoping.ui.map.track.GeoTrackOverlay;
import eu.ttbox.geoping.ui.map.track.dialog.SelectGeoTrackDialog;
import eu.ttbox.geoping.ui.map.track.dialog.SelectGeoTrackDialog.OnSelectPersonListener;

/**
 * @see http://mobiforge.com/developing/story/using-google-maps-android
 * @author deostem
 * 
 */
public class ShowMapActivity extends FragmentActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = "ShowMapActivity";

	private static final int GEOTRACK_PERSON_LOADER = R.id.config_id_map_geotrack_person_loader;

	// Constant
	/**
	 * This number depend of previous menu
	 */
	private int MENU_LAST_ID = 3;

	// Map
	private MapController mapController;
	private MapView mapView;

	// Overlay
	private MyLocationOverlay myLocation;
	// private GeoTrackOverlay geoTrackOverlay;
	private HashMap<String, GeoTrackOverlay> geoTrackOverlayByUser = new HashMap<String, GeoTrackOverlay>();

	// Listener
	private SharedPreferences sharedPreferences;
	private SharedPreferences privateSharedPreferences;

	// Deprecated
	private ResourceProxy mResourceProxy;

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.map);

		// Prefs
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		privateSharedPreferences = getSharedPreferences(MapConstants.PREFS_NAME, MODE_PRIVATE);
		// Map
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setMultiTouchControls(true);
		mapView.setHapticFeedbackEnabled(true);
		// Map Controler
		mapController = mapView.getController();
		mapController.setZoom(17); // Zoon 1 is world view
		this.mResourceProxy = new DefaultResourceProxyImpl(this);
		// Tiles
		MyAppTilesProviders.initTilesSource(this);

		// Overlay
		this.myLocation = new MyLocationOverlay(this.getBaseContext(), this.mapView);
		mapView.getOverlays().add(myLocation);
		// Overlay
		// Query
		getSupportLoaderManager().initLoader(GEOTRACK_PERSON_LOADER, null, geoTrackPersonLoaderCallback); 
	}



	@Override
	protected void onDestroy() {
		myLocation.disableCompass();
		myLocation.disableMyLocation();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "### ### ### ### ### onDestroy call ### ### ### ### ###");
			Log.i(TAG, "### ### ### ### ### ### ### ### ### ### ### ### ###");
		}
	}

	@Override
	protected void onPause() {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "### ### ### ### ### ### ### ### ### ### ### ### ###");
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

		// Overlay May Location
		if (myLocation != null) {
			myLocation.onPause();
		}

		// Overlay GeoTrack
		if (!geoTrackOverlayByUser.isEmpty()) {
			for (Map.Entry<String, GeoTrackOverlay> entry : geoTrackOverlayByUser.entrySet()) {
				// String key = entry.getKey();
				GeoTrackOverlay geoTrackOverlay = entry.getValue();
				geoTrackOverlay.onPause();
			}
		}

		super.onPause();
		// timer.cancel();
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "### ### ### ### ### onPause call ### ### ### ### ###");
			Log.i(TAG, "### ### ### ### ### ### ### ### ### ### ### ### ###");
		}
	}

	@Override
	protected void onResume() {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "### ### ### ### ###  ### ### ###  ### ### ### ### ###");
			Log.i(TAG, "### ### ### ### ### onResume call ### ### ### ### ###");
		}
		super.onResume();

		// read preference
		final String tileSourceName = privateSharedPreferences.getString(AppConstants.PREFS_KEY_TILE_SOURCE, TileSourceFactory.DEFAULT_TILE_SOURCE.name());
		try {
			final ITileSource tileSource = TileSourceFactory.getTileSource(tileSourceName);
			mapView.setTileSource(tileSource);
		} catch (final IllegalArgumentException ignore) {
		}

		if (privateSharedPreferences.getBoolean(MapConstants.PREFS_SHOW_LOCATION, false)) {
			this.myLocation.enableMyLocation();
		}
		if (privateSharedPreferences.getBoolean(MapConstants.PREFS_SHOW_COMPASS, false)) {
			this.myLocation.enableCompass();
		}
		// mapView.getController().setZoom(privateSharedPreferences.getInt(MapConstants.PREFS_ZOOM_LEVEL,
		// 1));
		// mapView.scrollTo(privateSharedPreferences.getInt(MapConstants.PREFS_SCROLL_X,
		// 0), privateSharedPreferences.getInt(MapConstants.PREFS_SCROLL_Y, 0));

		// Overlay MyLocation
		if (myLocation != null) {
			myLocation.onResume();
		}

		// Overlay GeoTrack
		if (!geoTrackOverlayByUser.isEmpty()) {
			for (Map.Entry<String, GeoTrackOverlay> entry : geoTrackOverlayByUser.entrySet()) {
				// String key = entry.getKey();
				GeoTrackOverlay geoTrackOverlay = entry.getValue();
				geoTrackOverlay.onResume();
			}
		}

		handleIntent(getIntent());

		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "### ### ### ### ### onResume call ### ### ### ### ###");
			Log.i(TAG, "### ### ### ### ###  ### ### ###  ### ### ### ### ###");
		}
	}

	// ===========================================================
	// Menu
	// ===========================================================

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_map, menu);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

		}
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(final Menu menu) {
		// mapView.getOverlayManager().onPrepareOptionsMenu(menu, MENU_LAST_ID,
		// mapView);
		boolean prepare = super.onPrepareOptionsMenu(menu);

		// Current Tile Source
		ITileSource currentTileSrc = mapView.getTileProvider().getTileSource();
		// Create Map
		MenuItem mapTypeItem = menu.findItem(R.id.menuMap_mapmode);
		final SubMenu mapTypeMenu = mapTypeItem.getSubMenu();
		mapTypeMenu.clear();
		int MENU_MAP_GROUP = MENU_LAST_ID;
		// int MENU_TILE_SOURCE_STARTING_ID =
		// TilesOverlay.MENU_TILE_SOURCE_STARTING_ID;
		for (int a = 0; a < TileSourceFactory.getTileSources().size(); a++) {
			final ITileSource tileSource = TileSourceFactory.getTileSources().get(a);
			MenuItem tileMenuItem = mapTypeMenu.add(MENU_MAP_GROUP, TilesOverlay.MENU_TILE_SOURCE_STARTING_ID + MENU_MAP_GROUP + a, Menu.NONE, tileSource.localizedName(mResourceProxy));
			if (currentTileSrc != null && currentTileSrc.ordinal() == tileSource.ordinal()) {
				tileMenuItem.setChecked(true);
			}
		}
		mapTypeMenu.setGroupCheckable(MENU_MAP_GROUP, true, true);
		return prepare;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuMap_mypositoncenter: {
			myLocation.enableFollowLocation();
			myLocation.runOnFirstFix(new Runnable() {

				@Override
				public void run() {
					mapController.setZoom(17);
				}
			});
			return true;
		}
		case R.id.menuMap_track_person: {
			SelectGeoTrackDialog personListDialod = new SelectGeoTrackDialog(this, getSupportLoaderManager(), new OnSelectPersonListener() {

				@Override
				public void onSelectPerson(Person person) {
					// TODO Auto-generated method stub
					Toast.makeText(ShowMapActivity.this, String.format("Select Person %s", person), Toast.LENGTH_SHORT).show();

				}
			}, geoTrackOverlayByUser);
			personListDialod.show();
		}
		default: {
			// Map click
			final int menuId = item.getItemId() - MENU_LAST_ID;
			if ((menuId >= TilesOverlay.MENU_TILE_SOURCE_STARTING_ID) && (menuId < TilesOverlay.MENU_TILE_SOURCE_STARTING_ID + TileSourceFactory.getTileSources().size())) {
				mapView.setTileSource(TileSourceFactory.getTileSources().get(menuId - TilesOverlay.MENU_TILE_SOURCE_STARTING_ID));
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					invalidateOptionsMenu();
				}
				return true;
			}
		}
		}
		return false;
	}

	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {

	}
	
	// ===========================================================
	// GeoTrack Overlay
	// ===========================================================


	private void addGeoTrackOverlay(String userId) {
		if (!geoTrackOverlayByUser.containsKey(userId)) {
			GeoTrackOverlay geoTrackOverlay = new GeoTrackOverlay(this, this.mapView, getSupportLoaderManager(), userId, System.currentTimeMillis());
			geoTrackOverlayByUser.put(userId, geoTrackOverlay);
			// register
			mapView.getOverlays().add(geoTrackOverlay);
		}
	}

	private void removeGeoTrackOverlay(String userId) {
		if (!geoTrackOverlayByUser.containsKey(userId)) {
			GeoTrackOverlay geoTrackOverlay = geoTrackOverlayByUser.remove(userId);
			mapView.getOverlays().remove(geoTrackOverlay);
		}
	}
	

	// ===========================================================
	// Listeners
	// ===========================================================

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// TODO Auto-generated method stub

	}

	// ===========================================================
	// Loader
	// ===========================================================

	private final LoaderManager.LoaderCallbacks<Cursor> geoTrackPersonLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			Log.d(TAG, "onCreateLoader");
			String sortOrder = String.format("%s ASC", PersonColumns.KEY_NAME);
			String selection = null;
			String[] selectionArgs = null;
			// Loader
			CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), PersonProvider.Constants.CONTENT_URI_PERSON, null, selection, selectionArgs, sortOrder);
			return cursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			int resultCount = cursor.getCount();
			Log.d(TAG, String.format("onLoadFinished with %s results", resultCount));
			if (cursor.moveToFirst()) {
				PersonHelper helper = new PersonHelper().initWrapper(cursor);
				do {
					String phone = helper.getPersonPhone(cursor);
					Log.d(TAG, String.format("Add Person with phone : %s", phone));
					addGeoTrackOverlay(phone);
				} while (cursor.moveToNext());
			}
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			for (Map.Entry<String, GeoTrackOverlay> entry : geoTrackOverlayByUser.entrySet()) {
				String key = entry.getKey();
				removeGeoTrackOverlay(key);
			}
		}

	};

	// ===========================================================
	// Others
	// ===========================================================

	// class TrackOverlay extends com.google.android.maps.Overlay {
	//
	// List<GeoTrack> points;
	// Location location;
	// Bitmap bmp = BitmapFactory.decodeResource(getResources(),
	// R.drawable.marker);
	//
	// public TrackOverlay(List<GeoTrack> trackPoints) {
	// this.points = trackPoints;
	// }
	//
	// public void addOverlay(GeoTrack point) {
	// points.add(point);
	// }
	//
	// /**
	// * @see http
	// * ://stackoverflow.com/questions/2176397/drawing-a-line-path-on
	// * -google-maps
	// */
	// public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long
	// when) {
	//
	// Point lastPoint = null;
	// // Path path = new Path();
	// // Paint Line
	// Paint paintLine = new Paint();
	// paintLine.setStrokeWidth(5);
	// paintLine.setColor(Color.BLUE);
	// paintLine.setStyle(Paint.Style.FILL);
	// // Paint texte
	// Paint paint = new Paint();
	// paint.setStrokeWidth(1);
	// paint.setTextSize(20);
	// // paint.setARGB(255, 255, 255, 255);
	// // paint.setColor(Color.GREEN);
	// paint.setColor(Color.RED);
	// paint.setStyle(Paint.Style.FILL_AND_STROKE);
	//
	// int zoonLevel = mapView.getZoomLevel();
	// for (GeoTrack point : points) {
	// // Converts lat/lng-Point to OUR coordinates on the screen.
	// Point myScreenCoords = new Point();
	// mapView.getProjection().toPixels(point.asGeoPoint(), myScreenCoords);
	// canvas.drawBitmap(bmp, myScreenCoords.x, myScreenCoords.y, paint);
	// if (zoonLevel > 19) {
	// canvas.drawText(point.getTimeAsDate().toLocaleString(), myScreenCoords.x,
	// myScreenCoords.y, paint);
	// }
	// // Draw line
	//
	// if (lastPoint == null) {
	// lastPoint = myScreenCoords;
	// } else {
	// // path.moveTo((float) lastPoint.x, (float) lastPoint.y);
	// canvas.drawLine((float) lastPoint.x, (float) lastPoint.y, (float)
	// myScreenCoords.x, (float) myScreenCoords.y, paintLine);
	//
	// }
	// }
	// return super.draw(canvas, mapView, shadow, when);
	// }
	//
	// @Override
	// public boolean onTouchEvent(MotionEvent event, MapView mapView) {
	// // ---when user lifts his finger---
	// if (event.getAction() == 1) {
	// // mapView.invalidate();
	// // GeoPoint p = mapView.getProjection().fromPixels((int)
	// // event.getX(), (int) event.getY());
	// // Toast.makeText(getBaseContext(), p.getLatitudeE6() / 1E6 +
	// // "," + p.getLongitudeE6() / 1E6, Toast.LENGTH_SHORT).show();
	// }
	// return false;
	// }
	//
	// }

}
