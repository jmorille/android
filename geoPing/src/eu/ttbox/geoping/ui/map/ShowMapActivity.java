package eu.ttbox.geoping.ui.map;

import java.util.ArrayList;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.views.overlay.TilesOverlay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.analytics.tracking.android.EasyTracker;
import com.slidingmenu.lib.SlidingMenu;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.VersionUtils;
import eu.ttbox.geoping.ui.GeoPingSlidingMenuFragmentActivity;

/**
 * @see http://mobiforge.com/developing/story/using-google-maps-android
 * 
 */
public class ShowMapActivity extends GeoPingSlidingMenuFragmentActivity {

    private static final String TAG = "ShowMapActivity";

    // Constant
    /**
     * This number depend of previous menu
     */
    private int MENU_LAST_ID = 3;

    // private SlidingMenu slidingMenu;
    // Map
    private ShowMapFragment mapFragment;

    private ActionMode mActionMode;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.map_activity);

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
        if (fragment instanceof ShowMapFragment) {
            mapFragment = (ShowMapFragment) fragment;
        }
    }

    // ===========================================================
    // Life cycle
    // ===========================================================

    @Override
    protected void onResume() {
        if (Log.isLoggable(TAG, Log.INFO)) {
            Log.i(TAG, "### ### ### ### ### onResume call ### ### ### ### ###");
        }
        super.onResume();
        handleIntent(getIntent());
    }

    // ===========================================================
    // Menu
    // ===========================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        // mapView.getOverlayManager().onPrepareOptionsMenu(menu, MENU_LAST_ID,
        // mapView);
        boolean prepare = super.onPrepareOptionsMenu(menu);

        // Current Tile Source
        ITileSource currentTileSrc = mapFragment.getMapViewTileSource();
        // Create Map
        MenuItem mapTypeItem = menu.findItem(R.id.menuMap_mapmode);
        final SubMenu mapTypeMenu = mapTypeItem.getSubMenu();
        mapTypeMenu.clear();
        int MENU_MAP_GROUP = MENU_LAST_ID;
        // int MENU_TILE_SOURCE_STARTING_ID =
        // TilesOverlay.MENU_TILE_SOURCE_STARTING_ID;
        ArrayList<ITileSource> tiles = mapFragment.getMapViewTileSources();
        int tileSize = tiles.size();
        for (int a = 0; a < tileSize; a++) {
            final ITileSource tileSource = tiles.get(a);
            String tileName = mapFragment.getMapViewTileSourceName(tileSource);
            MenuItem tileMenuItem = mapTypeMenu.add(MENU_MAP_GROUP, TilesOverlay.MENU_TILE_SOURCE_STARTING_ID + MENU_MAP_GROUP + a, Menu.NONE, tileName);
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
            mapFragment.centerOnMyPosition();
            return true;
        }
        case R.id.menuMap_mypositon_hide: {
            mapFragment.swichDisplayMyPosition();
            return true;
        }
        case R.id.menuMap_track_person: {
            mapFragment.showSelectPersonDialog();
            return true;
        }
        case R.id.menuMap_track_timeline: {
            mapFragment.swichRangeTimelineBarVisibility();
            return true;
        }
        case R.id.menuMap_geofence_add: {
            mapFragment.addGeofenceOverlayEditor();
            mActionMode = startActionMode(mActionModeCallbackAddGeofence);
            return true;
        }
        default: {
            // Map click
            final int menuId = item.getItemId() - MENU_LAST_ID;
            ArrayList<ITileSource> tiles = mapFragment.getMapViewTileSources();
            int tileSize = tiles.size();
            if ((menuId >= TilesOverlay.MENU_TILE_SOURCE_STARTING_ID) && (menuId < TilesOverlay.MENU_TILE_SOURCE_STARTING_ID + tileSize)) {
                mapFragment.setMapViewTileSource(tiles.get(menuId - TilesOverlay.MENU_TILE_SOURCE_STARTING_ID));
                // Compatibility
                if (VersionUtils.isHc11) {
                    isHc11InvalidateOptionsMenu();
                }
                return true;
            }
        }
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NewApi")
    private void isHc11InvalidateOptionsMenu() {
        if (VersionUtils.isHc11) {
            invalidateOptionsMenu();
        }
    }

    // ===========================================================
    // Handle Intent
    // ===========================================================

    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent Intent : " + intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (mapFragment != null) {
            mapFragment.handleIntent(intent);
        }
        // mapFragment.handleIntent(intent);
        //
        // String action = intent.getAction();
        // Log.d(TAG, String.format("Handle Intent for action %s : %s", action,
        // intent));
        // if (Intent.ACTION_VIEW.equals(action)) {
        // String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
        // Bundle bundle = intent.getExtras();
        // if ( bundle.containsKey(GeoTrackColumns.COL_LATITUDE_E6)
        // && bundle.containsKey(GeoTrackColumns.COL_LONGITUDE_E6) ) {
        // int latE6 = intent.getIntExtra(GeoTrackColumns.COL_LATITUDE_E6,
        // Integer.MIN_VALUE);
        // int lngE6 = intent.getIntExtra(GeoTrackColumns.COL_LONGITUDE_E6,
        // Integer.MIN_VALUE);
        // Log.w(TAG, String.format("Show on Map Phone [%s] (%s, %s) ", phone,
        // latE6, lngE6));
        // if (Integer.MIN_VALUE != latE6 && Integer.MIN_VALUE != lngE6) {
        // mapFragment.centerOnPersonPhone(phone, latE6, lngE6);
        // }
        // } else {
        // mapFragment.centerOnPersonPhone(phone);
        // }
        // }
    }

    // ===========================================================
    // Contextual Menu ActionMode
    // ===========================================================

    private ActionMode.Callback mActionModeCallbackAddGeofence = new ActionMode.Callback() {

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.geofence_edit_menu, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.d(TAG, "Click onActionItemClicked itemId : " + item.getItemId() + ", " + item);
            switch (item.getItemId()) {
            case R.id.menu_save:
//                Toast.makeText(ShowMapActivity.this, "Save menu", Toast.LENGTH_LONG).show();
                mapFragment.saveGeofenceOverlayEditor();
                mode.finish(); // Action picked, so close the CAB
                return true;
            case R.id.menu_delete:
//                Toast.makeText(ShowMapActivity.this, "Deleted menu", Toast.LENGTH_LONG).show();
                mapFragment.deleteGeofenceOverlayEditor();
                mode.finish(); // Action picked, so close the CAB
                return true;
            default:
                Log.w(TAG, "Ignore onActionItemClicked itemId : " + item.getItemId() + ", " + item);
                return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mapFragment.closeGeofenceOverlayEditor();
        }

    };

    // ===========================================================
    // Others
    // ===========================================================

}
