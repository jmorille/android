package eu.ttbox.geoping.ui.map;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapController.AnimationType;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.ui.map.core.MapConstants;
import eu.ttbox.geoping.ui.map.core.MyAppTilesProviders;
import eu.ttbox.geoping.ui.map.mylocation.MyLocationOverlay;
import eu.ttbox.geoping.ui.map.track.GeoTrackOverlay;
import eu.ttbox.geoping.ui.map.track.dialog.SelectGeoTrackDialog;
import eu.ttbox.geoping.ui.map.track.dialog.SelectGeoTrackDialog.OnSelectPersonListener;

/**
 * @see http://mobiforge.com/developing/story/using-google-maps-android
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
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.map);

        // Prefs
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        privateSharedPreferences = getSharedPreferences(MapConstants.PREFS_NAME, MODE_PRIVATE);
        // Config
        geocodingAuto = sharedPreferences.getBoolean(AppConstants.PREFS_GEOPOINT_GEOCODING_AUTO, true);
        
        // Map
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setMultiTouchControls(true);
        mapView.setHapticFeedbackEnabled(true);
        // Map Controler
        mapController = mapView.getController();
        // mapController.setZoom(17); // Zoon 1 is world view
        this.mResourceProxy = new DefaultResourceProxyImpl(this);
        // Tiles
        MyAppTilesProviders.initTilesSource(this);

        // Overlay
        this.myLocation = new MyLocationOverlay(this.getBaseContext(), this.mapView);
        mapView.getOverlays().add(myLocation);
        // Service
        mStatusReceiver = new StatusReceiver();
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
        // Zoon 1 is world view
        mapView.getController().setZoom(privateSharedPreferences.getInt(MapConstants.PREFS_ZOOM_LEVEL, 17));
        // Center
        int scrollX = privateSharedPreferences.getInt(MapConstants.PREFS_SCROLL_X, Integer.MIN_VALUE);
        int scrollY = privateSharedPreferences.getInt(MapConstants.PREFS_SCROLL_Y, Integer.MIN_VALUE);
        if (Integer.MIN_VALUE != scrollX && Integer.MIN_VALUE != scrollY) {
            mapView.scrollTo(scrollX, scrollY);
        }
        // Options
        boolean showLocation = privateSharedPreferences.getBoolean(MapConstants.PREFS_SHOW_LOCATION, true);
        this.myLocation.enableMyLocation(showLocation);
        boolean showCompass = privateSharedPreferences.getBoolean(MapConstants.PREFS_SHOW_COMPASS, false);
        this.myLocation.enableCompass(showCompass);

        // Service
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_NEW_GEOTRACK_INSERTED);
        registerReceiver(mStatusReceiver, filter);

        // Overlay MyLocation
        if (myLocation != null) {
            myLocation.onResume();
        }

        // Overlay GeoTrack
        // if (!geoTrackOverlayByUser.isEmpty()) {
        // for (Map.Entry<String, GeoTrackOverlay> entry :
        // geoTrackOverlayByUser.entrySet()) {
        // // String key = entry.getKey();
        // GeoTrackOverlay geoTrackOverlay = entry.getValue();
        // geoTrackOverlay.onResume();
        // }
        // }

        handleIntent(getIntent());

        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "### ### ### ### ### onResume call ### ### ### ### ###");
            Log.i(TAG, "### ### ### ### ###  ### ### ###  ### ### ### ### ###");
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

        // Service
        unregisterReceiver(mStatusReceiver);

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
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "### ### ### ### ### onPause call ### ### ### ### ###");
            Log.i(TAG, "### ### ### ### ### ### ### ### ### ### ### ### ###");
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
            SelectGeoTrackDialog personListDialod = new SelectGeoTrackDialog(this, getSupportLoaderManager(), onSelectPersonListener, geoTrackOverlayByUser);
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
    };

    // ===========================================================
    // Handle Intent
    // ===========================================================

    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        Log.d(TAG, String.format("Handle Intent for action %s : %s", action, intent));
        if (Intent.ACTION_VIEW.equals(action)) {
            String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            int latE6 = intent.getIntExtra(GeoTrackColumns.COL_LATITUDE_E6, Integer.MIN_VALUE);
            int lngE6 = intent.getIntExtra(GeoTrackColumns.COL_LONGITUDE_E6, Integer.MIN_VALUE);
            Log.w(TAG, String.format("Show on Map Phone [%s] (%s, %s) ", phone, latE6, lngE6));
            if (Integer.MIN_VALUE != latE6 && Integer.MIN_VALUE != lngE6) {
                if (myLocation != null) {
                    myLocation.disableFollowLocation();
                }
                annimateToPersonPhone(phone, latE6, lngE6);
            }
        }
    }

    private void annimateToPersonPhone(final String phone, final int latE6, final int lngE6) {
        mapController.animateTo(latE6, lngE6, AnimationType.HALFCOSINUSALDECELERATING);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // Animate to
                if (Integer.MIN_VALUE != latE6 && Integer.MIN_VALUE != lngE6) {
                    mapController.animateTo(latE6, lngE6, AnimationType.HALFCOSINUSALDECELERATING);
                }
                // Display GeoPoints for person
                GeoTrackOverlay geoTrackOverlay = geoTrackOverlayGetOrAddForPhone(phone);
             }
        });
    }

    // ===========================================================
    // GeoTrack Overlay
    // ===========================================================

    private GeoTrackOverlay geoTrackOverlayGetOrAddForPhone(String phone) {
        GeoTrackOverlay geoTrackOverlay = geoTrackOverlayByUser.get(phone);
        // Add person layer
        if (geoTrackOverlay == null) {
            Person person = null;
            Cursor cursor = getContentResolver().query(PersonProvider.Constants.CONTENT_URI, null, PersonColumns.SELECT_BY_PHONE_NUMBER, new String[] { phone }, null);
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
        if (!geoTrackOverlayByUser.containsKey(userId)) {
            geoTrackOverlay = new GeoTrackOverlay(this, this.mapView, getSupportLoaderManager(), person, System.currentTimeMillis(), geocodingAuto);
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
            isDone = true;
        } else {
            Log.e(TAG, String.format("Could not Animate to last position of person %s in geoTrackOverlayByUser", userId));
            for (String key :  geoTrackOverlayByUser.keySet() ) {
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
            String sortOrder = String.format("%s ASC", PersonColumns.COL_NAME);
            String selection = null;
            String[] selectionArgs = null;
            // Loader
            CursorLoader cursorLoader = new CursorLoader(getApplicationContext(), PersonProvider.Constants.CONTENT_URI, null, selection, selectionArgs, sortOrder);
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
