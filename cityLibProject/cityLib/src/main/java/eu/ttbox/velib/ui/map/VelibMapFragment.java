package eu.ttbox.velib.ui.map;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;
import android.widget.ToggleButton;
import eu.ttbox.osm.ui.map.MapViewFactory;
import eu.ttbox.osm.ui.map.mylocation.MyLocationOverlay;
import eu.ttbox.osm.ui.map.mylocation.dialog.GpsActivateAskDialog;
import eu.ttbox.velib.R;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.map.provider.VeloProviderItemizedOverlay;
import eu.ttbox.velib.map.station.StationDispoOverlay;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.VelibService;
import eu.ttbox.velib.service.VelibService.LocalBinder;
import eu.ttbox.velib.service.geo.GeoUtils;

/**
 * @see https://github.com/eskerda/CityBikes/tree/openvelib
 * 
 *      Map Api {@link http
 *      ://www.vogella.com/articles/AndroidLocationAPI/article.html#overview}
 * 
 *      Osm Sample {@link http://code.google.com/p/osmdroid/source/browse/trunk/
 *      OpenStreetMapViewer/src/org/osmdroid/MapActivity.java}
 * 
 *      MVP : {@link http
 *      ://stackoverflow.com/questions/4916209/which-design-patterns-are
 *      -used-on-android/6770903#6770903}
 * @author deostem
 * 
 */
public class VelibMapFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener, VelibProviderContainer {

	private static final String TAG = "VelibMapFragment";

	private ResourceProxy mResourceProxy;

	private MapController mapController;
	private MapView mapView;

	private ToggleButton swtichMode;

	private SharedPreferences sharedPreferences;
	private SharedPreferences privateSharedPreferences;

	// private PowerManager mPowerManager;
	// private WakeLock mWakeLock;

	private MyLocationOverlay myLocation;

	private VelibService velibService;

	private ScheduledThreadPoolExecutor timer;

	// Binding
	private Drawable packing;
	private Drawable cycle;
	private Drawable all;

	// Instance Value
	private VelibServiceConnection velibServiceConnection;
	private VelibProvider velibProvider;
	private StationDispoOverlay stationOverlay;

	// Config
	boolean askToEnableGps = true;
	private StationDispoModeSwitch stationDispoModeSwitch;

	// Manage Thread
	/**
	 * * Le AtomicBoolean pour lancer et stopper la Thread :
	 * http://mathias-seguy
	 * .developpez.com/cours/android/handler_async_memleak/#L4-1
	 */
	private AtomicBoolean isThreadRunnning = new AtomicBoolean();
	private AtomicBoolean isThreadPausing = new AtomicBoolean();

	// Message Handler
	protected static final int UI_MSG_ANIMATE_TO_GEOPOINT = 0;
	protected static final int UI_MSG_TOAST = 1;
	protected static final int UI_MSG_TOAST_ERROR = 2;

	private Handler uiHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (isThreadRunnning.get()) {
				switch (msg.what) {
				case UI_MSG_ANIMATE_TO_GEOPOINT: {
					GeoPoint geoPoint = (GeoPoint) msg.obj;
					if (geoPoint != null) {
						if (myLocation != null) {
							myLocation.disableFollowLocation();
						}
						Log.d(TAG, "uiHandler mapController : animateTo " + geoPoint);
						mapController.setCenter(geoPoint);
						mapController.setZoom(17);
					}
					break;
				}
				case UI_MSG_TOAST: {
					String msgToast = (String) msg.obj;
					Toast.makeText(getActivity(), msgToast, Toast.LENGTH_SHORT).show();
					break;
				}
				case UI_MSG_TOAST_ERROR: {
					String msgToastError = (String) msg.obj;
					Toast.makeText(getActivity(), msgToastError, Toast.LENGTH_SHORT).show();
					break;
				}
				}
			}
		}
	};

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.map, container, false);

		// Service
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);
		privateSharedPreferences = getActivity().getSharedPreferences(MapConstants.PREFS_NAME, Context.MODE_PRIVATE);
		timer = new ScheduledThreadPoolExecutor(1);

		// Osm
		// -------------
		mResourceProxy = new DefaultResourceProxyImpl(getActivity().getApplicationContext());
		// Map
		ITileSource tileSource = getPreferenceMapViewTileSource();
		String googleApiKey = getResources().getString(R.string.map_apiKey);
		mapView = createMapView(tileSource, false, googleApiKey);

		ViewGroup mapViewContainer = (ViewGroup) v.findViewById(R.id.mapViewContainer);
		mapViewContainer.addView((View) mapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		// Controler
		mapController = mapView.getController();
		mapController.setZoom(17); // Zoon 1 is world view

		// Binding
		// -------------
		swtichMode = (ToggleButton) v.findViewById(R.id.map_button_parking_cycle_switch);
		packing = getResources().getDrawable(R.drawable.panneau_parking);
		cycle = getResources().getDrawable(R.drawable.panneau_obligation_cycles);
		all = getResources().getDrawable(R.drawable.panneau_parking_cycle);
		stationDispoModeSwitch = new StationDispoModeSwitch();
		if (swtichMode != null) {
			stationDispoModeSwitch.displayToMode(0);
			swtichMode.setOnClickListener(stationDispoModeSwitch);
		}
		// Action bar and compatibility
		// -----------------------------
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			// No Action bar => Activate Map Button Overlay
			ToggleButton myPositionButton = (ToggleButton) v.findViewById(R.id.map_button_myposition);
			if (myPositionButton != null) {
				myPositionButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						centerOnMyLocationFix();
					}
				});
				myPositionButton.setVisibility(View.VISIBLE);
			}
		}

		// Map Overlay
		// -------------
		// MyLocation Overlay
		myLocation = new MyLocationOverlay(getActivity(), this.mapView, mResourceProxy);
		myLocation.enableMyLocation();
		myLocation.enableCompass();
		mapView.getOverlays().add((Overlay) myLocation);

		// Bind CityLib Service
		// ---------------------
		velibServiceConnection = new VelibServiceConnection();
		getActivity().bindService(new Intent(getActivity(), VelibService.class), velibServiceConnection, Context.BIND_AUTO_CREATE);

		GeoPoint lastKnownLocationAsGeoPoint = myLocation.getLastKnownLocationAsGeoPoint();
		velibProvider = computeConditionVelibProvider(lastKnownLocationAsGeoPoint);

		// Center on Provider
		// ---------------------
		if (velibProvider != null) {
			centerMapToFixBox(velibProvider, lastKnownLocationAsGeoPoint);
		}

		// Check For Gps
		// --------------
		boolean enableGPS = isGpsLocationProviderIsEnable();
		if (askToEnableGps && !enableGPS) {
			new GpsActivateAskDialog(getActivity()).show();
		}
		// Initialiser le booléen isThreadRunning
		isThreadRunnning.set(true);
		return v;
	}

	private VelibProvider computeConditionVelibProvider(GeoPoint lastKnownLocationAsGeoPoint) {
		return VelibProviderHelper.computeConditionVelibProvider(sharedPreferences, lastKnownLocationAsGeoPoint);
	}

	private MapView createMapView(ITileSource tileSource, boolean google, String gooleApiKey) {
		MapView mapView = null;
		if (google) {
			// GoogleMapView mapView2 = new GoogleMapView(this, gooleApiKey);
		} else {
			ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
			mapView = MapViewFactory.createOsmMapView(getActivity().getApplicationContext(), mResourceProxy, tileSource, activityManager);
		}
		return mapView;
	}

	// ===========================================================
	// Life Cycle
	// ===========================================================

	@Override
	public void onDestroy() {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "### ### ### ### ### onDestroy call ### ### ### ### ###");
		}
		// Tuer la Thread
		isThreadRunnning.set(false);
		timer.shutdownNow();
		myLocation.disableCompass();
		myLocation.disableMyLocation();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
		// Unbind service
		getActivity().unbindService(velibServiceConnection);

		// Super
		super.onDestroy();
	}

	@Override
	public void onPause() {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "### ### ### ### ### onPause call ### ### ### ### ###");
		}
		// Mettre la Thread en pause
		isThreadPausing.set(true);

		// Priavte Preference
		final SharedPreferences.Editor localEdit = privateSharedPreferences.edit();
		localEdit.putString(MapConstants.PREFS_TILE_SOURCE, mapView.getTileProvider().getTileSource().name());
		localEdit.putInt(MapConstants.PREFS_SCROLL_X, mapView.getScrollX());
		localEdit.putInt(MapConstants.PREFS_SCROLL_Y, mapView.getScrollY());
		localEdit.putInt(MapConstants.PREFS_ZOOM_LEVEL, mapView.getZoomLevel());
		localEdit.putBoolean(MapConstants.PREFS_SHOW_LOCATION, myLocation.isMyLocationEnabled());
		localEdit.putBoolean(MapConstants.PREFS_SHOW_COMPASS, myLocation.isCompassEnabled());
		localEdit.commit();

		// Desactivated
		if (stationOverlay != null) {
			stationOverlay.disableTimer();
		}
		myLocation.disableCompass();
		myLocation.disableMyLocation();
		myLocation.disableThreadExecutors();

		// timer.getQueue().clear();
		if (stationOverlay != null) {
			stationOverlay.disableTimer();
		}
		super.onPause();
		// timer.cancel();
	}

	@Override
	public void onResume() {
		if (Log.isLoggable(TAG, Log.INFO)) {
			Log.i(TAG, "### ### ### ### ### onResume call ### ### ### ### ###");
		}
		// Relancer la Thread
		isThreadPausing.set(false);
		super.onResume();

		// read preference
		ITileSource tileSource = getPreferenceMapViewTileSource();
		mapView.setTileSource(tileSource);

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

		// Enable Status
		if (stationOverlay != null) {
			stationOverlay.enableTimer();
		}
		myLocation.enableMyLocation();
		myLocation.enableCompass();
		myLocation.enableThreadExecutors();

	}

	// ===========================================================
	// Listener
	// ===========================================================

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		// Log.i(TAG, "------------------------------------------------");
		// Log.i(TAG, "Preference change for key " + key);
		// Log.i(TAG, "------------------------------------------------");
		// Preference Providers
		if (AppConstants.PREFS_KEY_PROVIDER_SELECT.equals(key)) {
			String providerName = sharedPreferences.getString(AppConstants.PREFS_KEY_PROVIDER_SELECT, VelibProvider.FR_PARIS.getProviderName());
			VelibProvider newVelibProvider = VelibProvider.getVelibProvider(providerName);
			if (Log.isLoggable(TAG, Log.INFO))
				Log.i(TAG, String.format("Preference change for key %s = %s", key, newVelibProvider));
			if (velibProvider == null || !velibProvider.equals(newVelibProvider)) {
				// Center on new Velib Provider
				Log.d(TAG, "onSharedPreferenceChanged mapController : setCenter " + newVelibProvider.asGeoPoint());
				mapController.setCenter(newVelibProvider.asGeoPoint());
				if (myLocation != null) {
					myLocation.enableFollowLocation();
				}
				// Download Stations
				DownloadVeloStationsTask downloadVeloStationsTask = new DownloadVeloStationsTask();
				downloadVeloStationsTask.execute(newVelibProvider);
			}
		}
		// Other Preference

	}

	// ===========================================================
	// Map Tiles
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

	public ITileSource getPreferenceMapViewTileSource() {
		final String tileSourceName = privateSharedPreferences.getString(MapConstants.PREFS_TILE_SOURCE, TileSourceFactory.DEFAULT_TILE_SOURCE.name());
		ITileSource tileSource = null;
		try {
			tileSource = TileSourceFactory.getTileSource(tileSourceName);
		} catch (final IllegalArgumentException ignore) {
			Log.e(TAG, "Error in reading TileSource : " + tileSourceName);
		}
		return tileSource;
	}

	// ===========================================================
	// Map Action
	// ===========================================================

	public boolean isGpsLocationProviderIsEnable() {
		boolean result = false;
		if (myLocation != null) {
			result = myLocation.isGpsLocationProviderIsEnable();
		}
		return result;
	}

	public void centerOnMyLocationFix() { 
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

	public void centerMapToFixBox(VelibProvider velibProvider, GeoPoint lastKnownLocationAsGeoPoint) {
		boolean isLastKnownLocationInBoundyBox = GeoUtils.isGeoPointInBoundyBox(velibProvider.getBoundyBoxE6(), lastKnownLocationAsGeoPoint);
		if (lastKnownLocationAsGeoPoint != null && (isLastKnownLocationInBoundyBox || !velibProvider.isBoundyBoxE6())) {
			mapController.setCenter(lastKnownLocationAsGeoPoint);
			Log.d(TAG, " centerMapToFixBox mapController : setCenter " + lastKnownLocationAsGeoPoint);
			// Ask to make Fix Off my position
			// myLocation.runOnFirstFix(new Runnable() {
			// public void run() {
			// mapController.animateTo(myLocation.getMyLocation());
			// mapController.setZoom(17);
			// // myLocation.runOnFirstFix(null);
			// }
			// });
		} else {
			mapController.setCenter(velibProvider.asGeoPoint());
			Log.d(TAG, " centerMapToFixBox mapController : setCenter " + velibProvider.asGeoPoint());
		}
	}

	public void mapAnimateTo(GeoPoint geoPoint) {
		if (geoPoint != null) {
			Message msg = uiHandler.obtainMessage(UI_MSG_ANIMATE_TO_GEOPOINT, geoPoint);
			uiHandler.sendMessage(msg);
		}
	}

	// ===========================================================
	// Accessor
	// ===========================================================

	public VelibProvider getVelibProvider() {
		return velibProvider;
	}

	// ===========================================================
	// Service
	// ===========================================================

	private class VelibServiceBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
		}

	}

	private class VelibServiceConnection implements ServiceConnection {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			velibService = null;

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			velibService = ((LocalBinder) service).getService();
			// downloadVeloStationsTask = new DownloadVeloStationsTask();
			// --- Init Velib Provider List ---
			// --------------------------------
			Drawable providerOverlaytMarker = getResources().getDrawable(R.drawable.marker_velib_circle);
			VeloProviderItemizedOverlay providerOverlay = new VeloProviderItemizedOverlay(getActivity(), providerOverlaytMarker, velibService);
			mapView.getOverlays().add(providerOverlay);

			// --- Init station Dispo ---
			// ----------------------------
			if (velibProvider != null) {
				DownloadVeloStationsTask downloadVeloStationsTask = new DownloadVeloStationsTask();
				downloadVeloStationsTask.execute(velibProvider);
			}
		}
	};

	private class DownloadVeloStationsTask extends AsyncTask<VelibProvider, Void, ArrayList<Station>> {

		@Override
		protected ArrayList<Station> doInBackground(VelibProvider... params) {
			// Looper.prepare();
			// Do the Job
			VelibProvider newVelibProvider = params[0];
			if (Log.isLoggable(TAG, Log.INFO))
				Log.i(TAG, String.format("DownloadVeloStationsTask for provider %s", newVelibProvider));
			velibProvider = newVelibProvider;
			ArrayList<Station> stations = null;
			try {
				stations = velibService.getStationsByProvider(newVelibProvider);
			} catch (final Exception e) {
				String errorMsg = String.format("Error in getStationsByProvider : %s", e.getMessage());
				Log.e(TAG, errorMsg, e);
				uiHandler.sendMessage(uiHandler.obtainMessage(UI_MSG_TOAST_ERROR, errorMsg));
			}
			// TODO Toast.makeText(VelibMapActivity.this,
			// "Download Stations size " + stations.size(),
			// Toast.LENGTH_SHORT).show();
			return stations;

		}

		@Override
		protected void onPostExecute(ArrayList<Station> stations) {
			if (stations != null && !stations.isEmpty() && isThreadRunnning.get()) {
				if (Log.isLoggable(TAG, Log.INFO))
					Log.i(TAG, String.format("DownloadVeloStationsTask result of stations count %s", stations.size()));
				int stationSize = stations.size();
				String displayStationInfos = getResources().getQuantityString(R.plurals.numberOfStationAvailable, stationSize, stationSize);
				uiHandler.sendMessage(uiHandler.obtainMessage(UI_MSG_TOAST, displayStationInfos));
				// Add Station Overlay
				if (stationOverlay != null) {
					velibService.removeOnStationDispoUpdatedListener(stationOverlay);
					mapView.getOverlays().remove(stationOverlay);
				}
				stationOverlay = new StationDispoOverlay(getActivity(), mapView, stations, velibService, uiHandler, timer);
				stationOverlay.enableDisplayDispoText();
				// Register listener for update
				velibService.addOnStationDispoUpdatedListener(stationOverlay);
				// Register Overlay
				mapView.getOverlays().add(stationOverlay);
			}

		}
	}

	// ===========================================================
	// UI Listener
	// ===========================================================

	private class StationDispoModeSwitch implements View.OnClickListener {
		private int status = 0;

		@Override
		public void onClick(View v) {
			if (stationOverlay != null) {
				displayToMode(status + 1);
			}
		}

		public void displayToMode(int pMode) {
			int mode = pMode % 3;// pMode > 3 ? pMode % 3 : pMode;
			switch (mode) {
			case 0:
				swtichMode.setBackgroundDrawable(all);
				if (stationOverlay != null) {
					stationOverlay.setDrawDisplayCycleParking(true, true);
				}
				break;
			case 1:
				swtichMode.setBackgroundDrawable(cycle);
				if (stationOverlay != null) {
					stationOverlay.setDrawDisplayCycleParking(true, false);
				}
				break;
			case 2:
				swtichMode.setBackgroundDrawable(packing);
				if (stationOverlay != null) {
					stationOverlay.setDrawDisplayCycleParking(false, true);
				}
				break;
			default:
				break;
			}
			this.status = mode;
		}
	}

	// ===========================================================
	// Other
	// ===========================================================

}
