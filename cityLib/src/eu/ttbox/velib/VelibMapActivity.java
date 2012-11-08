package eu.ttbox.velib;

import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.tileprovider.tilesource.bing.BingMapTileSource;
import org.osmdroid.tileprovider.util.CloudmadeUtil;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.TilesOverlay;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.Toast;
import android.widget.ToggleButton;
import eu.ttbox.osm.tiles.MapTileProviderTTbox;
import eu.ttbox.osm.ui.map.MapViewFactory;
import eu.ttbox.osm.ui.map.mylocation.MyLocationOverlay;
import eu.ttbox.osm.ui.map.mylocation.dialog.GpsActivateAskDialog;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.core.Intents;
import eu.ttbox.velib.map.osm.GoogleMapView;
import eu.ttbox.velib.map.provider.VeloProviderItemizedOverlay;
import eu.ttbox.velib.map.station.StationDispoOverlay;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.VelibService;
import eu.ttbox.velib.service.VelibService.LocalBinder;
import eu.ttbox.velib.service.database.Velo.VeloColumns;
import eu.ttbox.velib.service.geo.GeoUtils;
import eu.ttbox.velib.ui.map.MapConstants;
import eu.ttbox.velib.ui.map.VelibMapPresenter;
import eu.ttbox.velib.ui.map.VelibMapView;
import eu.ttbox.velib.ui.preference.VelibPreferenceActivity;
import eu.ttbox.velib.ui.search.SearchableVeloActivity;

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
public class VelibMapActivity extends Activity implements VelibMapView, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "VelibMapActivity";

    int MENU_LAST_ID = 3;

    VelibMapPresenter presenter;

    private ResourceProxy mResourceProxy;

    private IMapController mapController;
    private MapView mapView;
    private ToggleButton myPositionButton;

    private SharedPreferences sharedPreferences;
    private SharedPreferences privateSharedPreferences;

    // private PowerManager mPowerManager;
    // private WakeLock mWakeLock;

    private MyLocationOverlay myLocation;

    private VelibService velibService;

    private ScheduledThreadPoolExecutor timer;

    // Instance Value
    private VelibServiceConnection velibServiceConnection;
    private VelibProvider velibProvider;
    private StationDispoOverlay stationOverlay;

    // Config
    boolean askToEnableGps = true;

    // Manage Thread
    /** * Le AtomicBoolean pour lancer et stopper la Thread */
    private AtomicBoolean isThreadRunnning = new AtomicBoolean();
    private AtomicBoolean isThreadPausing = new AtomicBoolean();

    // Message Handler
    protected static final int UI_MSG_ANIMATE_TO_GEOPOINT = 0;
    protected static final int UI_MSG_TOAST = 1;
    protected static final int UI_MSG_TOAST_ERROR = 2;

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case UI_MSG_ANIMATE_TO_GEOPOINT: {
                GeoPoint geoPoint = (GeoPoint) msg.obj;
                if (geoPoint != null) {
                    if (myLocation != null) {
                        myLocation.disableFollowLocation();
                    }
                    mapController.animateTo(geoPoint);
                    mapController.setZoom(17);
                }
                break;
            }
            case UI_MSG_TOAST: {
                String msgToast = (String) msg.obj;
                Toast.makeText(VelibMapActivity.this, msgToast, Toast.LENGTH_SHORT).show();
                break;
            }
            case UI_MSG_TOAST_ERROR: {
                String msgToastError = (String) msg.obj;
                Toast.makeText(VelibMapActivity.this, msgToastError, Toast.LENGTH_SHORT).show();
                break;
            }
            }
        }
    };

    public VelibMapActivity() {
        super();
        presenter = new VelibMapPresenter(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private MapView createMapView(ITileSource tileSource, boolean google, String gooleApiKey) {
        MapView mapView = null;
        if (google) {
            GoogleMapView mapView2 = new GoogleMapView(this, gooleApiKey);
        } else {
        	ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            mapView = MapViewFactory.createOsmMapView(this, mResourceProxy, tileSource, activityManager);
        }
        return mapView;
    }

    @Override
    public void onCreate(Bundle bundle) {
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "### ### ### ### ###  ### ### ###  ### ### ### ### ###");
            Log.i(TAG, "### ### ### ### ### onCreate call ### ### ### ### ###");
        }
        isThreadRunnning.set(true);
        super.onCreate(bundle);
        setContentView(R.layout.map); // bind the layout to the activity

        // Service
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        privateSharedPreferences = getSharedPreferences(MapConstants.PREFS_NAME, MODE_PRIVATE);

        // OSM
        mResourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

        // Init handler
        timer = new ScheduledThreadPoolExecutor(1);

        // Osm
        // -------------
        // Map
        ITileSource tileSource = getPrefTileSource();  
        String googleApiKey = getResources().getString(R.string.map_apiKey);
        mapView = createMapView(tileSource, false, googleApiKey); 
        
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.mapViewContainer);
        mapViewContainer.addView((View) mapView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        // Controler
        mapController = mapView.getController();
        mapController.setZoom(17); // Zoon 1 is world view

        // Search Map composant
        myPositionButton = (ToggleButton) findViewById(R.id.map_button_myposition);
        myPositionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                centerOnMyLocationFix();
            }
        });

        // action bar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayShowTitleEnabled(false);
            // actionBar.setHomeButtonEnabled(true) ;
            // actionBar.setDisplayUseLogoEnabled(true);
            // actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        } else {
            myPositionButton.setVisibility(View.VISIBLE);
        }

        // Init My Last position
        // myLocation = new MyLocationDirectionOverlay(getApplicationContext(),
        // mapView);
        // myLocation = new MyLocationAndroidOverlay(getApplicationContext(),
        // mapView);
        this.myLocation = new MyLocationOverlay(this.getBaseContext(), this.mapView, mResourceProxy);
        GeoPoint lastKnownLocationAsGeoPoint = myLocation.getLastKnownLocationAsGeoPoint();

        // TODO Better Default Provider
        String providerName = sharedPreferences.getString(AppConstants.PREFS_KEY_PROVIDER_SELECT, VelibProvider.FR_PARIS.getProviderName());
        if (providerName != null) {
            velibProvider = VelibProvider.getVelibProvider(providerName);
        }
        if (velibProvider == null) {
            ArrayList<VelibProvider> providers = VelibProvider.getVelibProviderInBoundyBox(lastKnownLocationAsGeoPoint);
            if (providers != null && !providers.isEmpty()) {
                velibProvider = providers.get(0);
            }
        }
        if (velibProvider == null) {
            velibProvider = VelibProvider.FR_PARIS;
        }
        // Center on Provider
        if (velibProvider != null) {
            mapController.setCenter(velibProvider.asGeoPoint());
        }

        // Center on my last position Only if is in define Box
        GeoPoint intentGeoPoint = handleIntent(getIntent());
        if (intentGeoPoint == null) {
            centerMapToFixBox(velibProvider, lastKnownLocationAsGeoPoint);
        } else {
            mapController.setCenter(intentGeoPoint);
        }

        myLocation.enableMyLocation();
        myLocation.enableCompass();

        mapView.getOverlays().add((Overlay) myLocation);
        // Init Data
        velibServiceConnection = new VelibServiceConnection();
        bindService(new Intent(this, VelibService.class), velibServiceConnection, Context.BIND_AUTO_CREATE);
        // Create a bright wake lock
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // Check For Gps
        boolean enableGPS = myLocation.isGpsLocationProviderIsEnable();

        if (askToEnableGps && !enableGPS) {
            new GpsActivateAskDialog(this).show();
        }

        // Recherche
        // Get the intent, verify the action and get the query
        // @see
        // http://developer.android.com/guide/topics/search/search-dialog.html
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "### ### ### ### ### onCreate call ### ### ### ### ###");
            Log.i(TAG, "### ### ### ### ###  ### ### ###  ### ### ### ### ###");
        }
    }

    private GeoPoint handleIntent(Intent intent) {

        GeoPoint point = null;
        if (intent != null && intent.getData() != null) {
            Uri data = intent.getData();
            // if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "----------------------------------------------");
            Log.i(TAG, "----------------------------------------------");
            Log.i(TAG, String.format("handleIntent : %s", data));
            Log.i(TAG, "----------------------------------------------");
            Log.i(TAG, "----------------------------------------------");
            // }
            String[] projection = new String[] { VeloColumns.COL_ID, VeloColumns.COL_LATITUDE_E6, VeloColumns.COL_LONGITUDE_E6 };
            Cursor cursor = getContentResolver().query(data, projection, null, null, null);

            while (cursor.moveToNext()) {
                int latitudeE6 = cursor.getInt(1);
                int longitudeE6 = cursor.getInt(2);
                if (Log.isLoggable(TAG, Log.INFO))
                    Log.i(TAG, String.format("handleIntent Request for coord(%s, %s)", latitudeE6, longitudeE6));
                point = new GeoPoint(latitudeE6, longitudeE6);
            }
            if (point != null) {
                uiHandler.sendMessage(uiHandler.obtainMessage(UI_MSG_ANIMATE_TO_GEOPOINT, point));
            }
            // String stationIdString = data.getLastPathSegment();
            // int stationId = Integer.parseInt(stationIdString);

        }
        return point;
    }

    @Override
    protected void onStop() {
        if (Log.isLoggable(TAG, Log.INFO)) {
            // Log.i(TAG,
            // "### ### ### ### ### ### ### ### ### ### ### ### ###");
            Log.i(TAG, "### ### ### ### ### onStop call ### ### ### ### ###");
        }

        // Super
        super.onStop();
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "### ### ### ### ### onStop call ### ### ### ### ###");
            // Log.i(TAG,
            // "### ### ### ### ### ### ### ### ### ### ### ### ###");
        }
    }

    @Override
    protected void onDestroy() {
        if (Log.isLoggable(TAG, Log.INFO)) {
            // Log.i(TAG,
            // "### ### ### ### ### ### ### ### ### ### ### ### ###");
            Log.i(TAG, "### ### ### ### ### onDestroy call ### ### ### ### ###");
        }
        isThreadRunnning.set(false);
        timer.shutdownNow();
        myLocation.disableCompass();
        myLocation.disableMyLocation();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        // Unbind service
        unbindService(velibServiceConnection);

        // Cancel Download
        // if (downloadVeloStationsTask != null) {
        // downloadVeloStationsTask.cancel(true);
        // downloadVeloStationsTask = null;
        // }
        // Annimation

        // Super
        super.onDestroy();
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "### ### ### ### ### onDestroy call ### ### ### ### ###");
            // Log.i(TAG,
            // "### ### ### ### ### ### ### ### ### ### ### ### ###");
        }
    }

    private ITileSource getPrefTileSource() {
        final String tileSourceName = privateSharedPreferences.getString(MapConstants.PREFS_TILE_SOURCE, TileSourceFactory.DEFAULT_TILE_SOURCE.name());
        ITileSource tileSource = null;
        try {
            tileSource = TileSourceFactory.getTileSource(tileSourceName);
        } catch (final IllegalArgumentException ignore) {
            Log.e(TAG, "Error in reading TileSource : " + tileSourceName);
        }
        return tileSource;
    }

    @Override
    protected void onPause() {
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "### ### ### ### ### ### ### ### ### ### ### ### ###");
            Log.i(TAG, "### ### ### ### ### onPause call ### ### ### ### ###");
        }
        isThreadPausing.set(true);
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
        isThreadPausing.set(false);
        super.onResume();

        // read preference
        ITileSource tileSource = getPrefTileSource();
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

        handleIntent(getIntent());

        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "### ### ### ### ### onResume call ### ### ### ### ###");
            Log.i(TAG, "### ### ### ### ###  ### ### ###  ### ### ### ### ###");
        }
    }

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
                mapController.setCenter(newVelibProvider.asGeoPoint());
                myLocation.enableFollowLocation();
                // Download Stations
                DownloadVeloStationsTask downloadVeloStationsTask = new DownloadVeloStationsTask();
                downloadVeloStationsTask.execute(newVelibProvider);
            }
        }
        // Other Preference

    }

    private void centerMapToFixBox(VelibProvider velibProvider, GeoPoint lastKnownLocationAsGeoPoint) {
        boolean isLastKnownLocationInBoundyBox = GeoUtils.isGeoPointInBoundyBox(velibProvider.getBoundyBoxE6(), lastKnownLocationAsGeoPoint);
        if (lastKnownLocationAsGeoPoint != null && (isLastKnownLocationInBoundyBox || !velibProvider.isBoundyBoxE6())) {
            mapController.setCenter(lastKnownLocationAsGeoPoint);
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
        }
    }

    private class VelibServiceBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            // intent.getData().get
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
            VeloProviderItemizedOverlay providerOverlay = new VeloProviderItemizedOverlay(VelibMapActivity.this, providerOverlaytMarker, velibService);
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
            if (stations != null && !stations.isEmpty()) {
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
                stationOverlay = new StationDispoOverlay(VelibMapActivity.this, mapView, stations, velibService, uiHandler, timer);
                stationOverlay.enableDisplayDispoText();
                // Register listener for update
                velibService.addOnStationDispoUpdatedListener(stationOverlay);
                // Register Overlay
                mapView.getOverlays().add(stationOverlay);
            }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map, menu);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            MenuItem searchitem = menu.findItem(R.id.menu_search);
            // TODO searchitem.collapseActionView();
            // Configure Search View
            SearchView searchView = (SearchView) searchitem.getActionView();
            // searchView.setSearchableInfo(searchable)
            searchView.setIconifiedByDefault(true);
        }

        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
        case KeyEvent.KEYCODE_SEARCH:
            return onSearchRequested();
        }
        return super.onKeyUp(keyCode, event);
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

        // Now process the menu item selection
        switch (item.getItemId()) {
        case R.id.menuMap_mypositoncenter: {
            centerOnMyLocationFix();
            return true;
        }
        case R.id.menu_search: {
            onSearchRequested();

            return true;
        }
        case R.id.menuMap_favorite: {
            Intent intent = new Intent(this, SearchableVeloActivity.class);
            intent.setAction(SearchableVeloActivity.ACTION_VIEW_FAVORITE);
            intent.putExtra(VelibProvider.class.getSimpleName(), velibProvider.ordinal());
            startActivity(intent);
            return true;
        }
        case R.id.menuOptions: {
            Intent intentOption = new Intent(this, VelibPreferenceActivity.class);
            startActivity(intentOption);
            return true;
        }
//        case R.id.menuDownloadTiles: {
//            MapTileProviderBase tileProvider = mapView.getTileProvider();
//            ITileSource tileSource = tileProvider.getTileSource(); 
//            int minZoom = mapView.getZoomLevel(); // tileSource.getMinimumZoomLevel()
//            int maxZoom = mapView.getZoomLevel(); // tileSource.getMaximumZoomLevel() 
//            double[] boundyBox = velibProvider.getBoundyBox(); 
//            Log.i(TAG, "startService downloadMapTiles for " + minZoom + "<=zoom<=" + maxZoom);
//            startService(Intents.downloadMapTiles(this, tileSource, minZoom, maxZoom, boundyBox)); 
//            return true; 
//        }
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
            // We can also respond to events in the overlays here
            // boolean isDo =
            // mapView.getOverlayManager().onOptionsItemSelected(item,
            // MENU_LAST_ID, mapView);
            // if (isDo) {
            // final int overlayItemId = item.getItemId() - MENU_LAST_ID;
            // if (overlayItemId == TilesOverlay.MENU_OFFLINE) {
            // final int id = mapView.useDataConnection() ?
            // R.string.set_mode_online : R.string.set_mode_offline;
            // Toast.makeText(this, id, Toast.LENGTH_LONG).show();
            // }
            // }
            // return isDo;
            return false;
        }
        }
    }

    @Override
    public boolean onSearchRequested() {
        // if (Log.isLoggable(TAG, Log.DEBUG))
        Log.d(TAG, "---------------  onSearchRequested  ---------------------------");
        Bundle appData = new Bundle();
        appData.putString(Intents.EXTRA_VELIB_PROVIDER, velibProvider.getProviderName());
        startSearch(null, false, appData, false);
        return true;
    }

    private void centerOnMyLocationFix() {
        myLocation.enableFollowLocation();
        myLocation.runOnFirstFix(new Runnable() {

            @Override
            public void run() {
                mapController.setZoom(17);
            }
        });
    }

    public void mapAnimateTo(GeoPoint geoPoint) {
        if (geoPoint != null) {
            Message msg = uiHandler.obtainMessage(UI_MSG_ANIMATE_TO_GEOPOINT, geoPoint);
            uiHandler.sendMessage(msg);
        }
    }

    // BroadcastReceiver onReceiver =

}
