package eu.ttbox.velib;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.TilesOverlay;

import eu.ttbox.velib.core.Intents;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.database.Velo.VeloColumns;
import eu.ttbox.velib.ui.map.VelibMapFragment;
import eu.ttbox.velib.ui.map.VelibProviderContainer;
import eu.ttbox.velib.ui.preference.VelibPreferenceActivity;

/**
 * @author deostem
 * @see https://github.com/eskerda/CityBikes/tree/openvelib
 * <p/>
 * Map Api {@link http
 * ://www.vogella.com/articles/AndroidLocationAPI/article.html#overview}
 * <p/>
 * Osm Sample {@link http://code.google.com/p/osmdroid/source/browse/trunk/
 * OpenStreetMapViewer/src/org/osmdroid/MapActivity.java}
 * <p/>
 * MVP : {@link http
 * ://stackoverflow.com/questions/4916209/which-design-patterns-are
 * -used-on-android/6770903#6770903}
 */
public class VelibMapActivity extends eu.ttbox.velib.ui.CityLibSlidingMenuFragmentActivity implements VelibProviderContainer {
    // VelibMapView

    private static final String TAG = "VelibMapActivity";
    int MENU_LAST_ID = 3;
    private VelibMapFragment mapFragment;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.map_activity); // bind the layout to the
        // activity

        // Center on my last position Only if is in define Box
        GeoPoint intentGeoPoint = handleIntent(getIntent());
        if (intentGeoPoint != null) {
            mapFragment.mapAnimateTo(intentGeoPoint);
        }

        // Create a bright wake lock
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // Recherche
        // Get the intent, verify the action and get the query
        // {@link
        // http://developer.android.com/guide/topics/search/search-dialog.html}

        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "### ### ### ### ### onCreate call ### ### ### ### ###");
        }
        // Tracker
        EasyTracker.getInstance().activityStart(this);
    }

    public SlidingMenu customizeSlidingMenu() {
        SlidingMenu slidingMenu = super.customizeSlidingMenu();
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
        return slidingMenu;
    }

    @Override
    public void onStop() {
        super.onStop();
        // Tracker
        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof VelibMapFragment) {
            mapFragment = (VelibMapFragment) fragment;
        }
    }

    // ===========================================================
    // Life Cycle
    // ===========================================================

    @Override
    public void onResume() {
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "### ### ### ### ### onResume call ### ### ### ### ###");
        }
        // Super
        super.onResume();
        // Handle Intent
        handleIntent(getIntent());

    }

    // ===========================================================
    // Handle Intent
    // ===========================================================

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private GeoPoint handleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, String.format("Handle Intent for action %s : %s", action, intent));
        // if (Intent.ACTION_VIEW.equals(action)) {
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
            String[] projection = new String[]{VeloColumns.COL_ID, VeloColumns.COL_LATITUDE_E6, VeloColumns.COL_LONGITUDE_E6};
            Cursor cursor = getContentResolver().query(data, projection, null, null, null);
            try {
                while (cursor.moveToNext()) {
                    int latitudeE6 = cursor.getInt(1);
                    int longitudeE6 = cursor.getInt(2);
                    Log.d(TAG, String.format("handleIntent Request for coord(%s, %s)", latitudeE6, longitudeE6));
                    point = new GeoPoint(latitudeE6, longitudeE6);
                }
                if (point != null) {
                    mapFragment.mapAnimateTo(point);
                }
            } finally {
                cursor.close();
            }

            // String stationIdString = data.getLastPathSegment();
            // int stationId = Integer.parseInt(stationIdString);

        }
        return point;
    }

    // ===========================================================
    // Menu
    // ===========================================================

    /**
     * http://stackoverflow.com/questions/9327826/searchviews-oncloselistener-
     * doesnt-work
     */
    @SuppressLint("NewApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.map, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        // mapView.getOverlayManager().onPrepareOptionsMenu(menu, MENU_LAST_ID,
        // mapView);
        boolean prepare = super.onPrepareOptionsMenu(menu);

        // Current Tile Source
        if (mapFragment != null) {
            ITileSource currentTileSrc = mapFragment.getMapViewTileSource();
            // Create Map
            MenuItem mapTypeItem = menu.findItem(R.id.menuMap_mapmode);
            final SubMenu mapTypeMenu = mapTypeItem.getSubMenu();
            mapTypeMenu.clear();
            int MENU_MAP_GROUP = MENU_LAST_ID;
            // int MENU_TILE_SOURCE_STARTING_ID =
            // TilesOverlay.MENU_TILE_SOURCE_STARTING_ID;
            for (int a = 0; a < TileSourceFactory.getTileSources().size(); a++) {
                final ITileSource tileSource = TileSourceFactory.getTileSources().get(a);
                String tileName = mapFragment.getMapViewTileSourceName(tileSource);
                MenuItem tileMenuItem = mapTypeMenu.add(MENU_MAP_GROUP, TilesOverlay.MENU_TILE_SOURCE_STARTING_ID + MENU_MAP_GROUP + a, Menu.NONE, tileName);
                if (currentTileSrc != null && currentTileSrc.ordinal() == tileSource.ordinal()) {
                    tileMenuItem.setChecked(true);
                }
            }
            mapTypeMenu.setGroupCheckable(MENU_MAP_GROUP, true, true);
        }
        return prepare;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Now process the menu item selection
        switch (item.getItemId()) {
            case R.id.menuMap_mypositoncenter: {
                mapFragment.centerOnMyLocationFix();
                return true;
            }
            case R.id.menu_search: {
                onSearchRequested();

                return true;
            }
            case R.id.menuMap_favorite: {
                startActivity(Intents.searchVelo(this, mapFragment.getVelibProvider()));
                return true;
            }
            case R.id.menuOptions: {
                Intent intentOption = new Intent(this, VelibPreferenceActivity.class);
                startActivity(intentOption);
                return true;
            }
            // case R.id.menuDownloadTiles: {
            // MapTileProviderBase tileProvider = mapView.getTileProvider();
            // ITileSource tileSource = tileProvider.getTileSource();
            // int minZoom = mapView.getZoomLevel(); //
            // tileSource.getMinimumZoomLevel()
            // int maxZoom = mapView.getZoomLevel(); //
            // tileSource.getMaximumZoomLevel()
            // double[] boundyBox = velibProvider.getBoundyBox();
            // Log.i(TAG, "startService downloadMapTiles for " + minZoom +
            // "<=zoom<=" + maxZoom);
            // startService(Intents.downloadMapTiles(this, tileSource, minZoom,
            // maxZoom, boundyBox));
            // return true;
            // }
            default: {
                // Map click
                final int menuId = item.getItemId() - MENU_LAST_ID;
                if ((menuId >= TilesOverlay.MENU_TILE_SOURCE_STARTING_ID) && (menuId < TilesOverlay.MENU_TILE_SOURCE_STARTING_ID + TileSourceFactory.getTileSources().size())) {
                    mapFragment.setMapViewTileSource(TileSourceFactory.getTileSources().get(menuId - TilesOverlay.MENU_TILE_SOURCE_STARTING_ID));
                    isHc11InvalidateOptionsMenu();
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
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @SuppressLint("NewApi")
    public void isHc11InvalidateOptionsMenu() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            invalidateOptionsMenu();
        }
    }

    // ===========================================================
    // Search
    // ===========================================================

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_SEARCH:
                return onSearchRequested();
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onSearchRequested() {
        // if (Log.isLoggable(TAG, Log.DEBUG))
        Log.d(TAG, "---------------  onSearchRequested  ---------------------------");
        Bundle appData = new Bundle();
        VelibProvider velibProvider = mapFragment.getVelibProvider();
        appData.putString(Intents.EXTRA_VELIB_PROVIDER, velibProvider.getProviderName());
        startSearch(null, false, appData, false);
        return true;
    }

    @Override
    public VelibProvider getVelibProvider() {
        VelibProvider result = null;
        if (mapFragment != null) {
            result = mapFragment.getVelibProvider();
        }
        return result;
    }

    // ===========================================================
    // Other
    // ===========================================================

}
