package eu.ttbox.geoping.ui.map;

import java.util.ArrayList;

import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.views.overlay.TilesOverlay;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;

/**
 * @see http://mobiforge.com/developing/story/using-google-maps-android
 * 
 */
public class ShowMapActivity extends FragmentActivity  {

    private static final String TAG = "ShowMapActivity";
 
    // Constant
    /**
     * This number depend of previous menu
     */
    private int MENU_LAST_ID = 3;
    
    // Map
    private ShowMapFragment mapFragment;

   
    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.map_activity);
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
        ITileSource currentTileSrc = mapFragment.getMapViewTileSource();
        // Create Map
        MenuItem mapTypeItem = menu.findItem(R.id.menuMap_mapmode);
        final SubMenu mapTypeMenu = mapTypeItem.getSubMenu();
        mapTypeMenu.clear();
        int MENU_MAP_GROUP = MENU_LAST_ID;
        // int MENU_TILE_SOURCE_STARTING_ID =
        // TilesOverlay.MENU_TILE_SOURCE_STARTING_ID;
        ArrayList<ITileSource> tiles = mapFragment.getMapViewTileSources() ;
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
        case R.id.menuMap_track_person: {
            mapFragment.showSelectPersonDialog();
            return true;
         }
        default: {
            // Map click
            final int menuId = item.getItemId() - MENU_LAST_ID;
            ArrayList<ITileSource>  tiles = mapFragment.getMapViewTileSources();
            int  tileSize = tiles.size();
            if ((menuId >= TilesOverlay.MENU_TILE_SOURCE_STARTING_ID) && (menuId < TilesOverlay.MENU_TILE_SOURCE_STARTING_ID +tileSize)) {
                mapFragment.setMapViewTileSource(tiles.get(menuId - TilesOverlay.MENU_TILE_SOURCE_STARTING_ID));
                // Compatibility
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    invalidateOptionsMenu();
                }
                return true;
            }
        }
        }
        return false;
    }
 

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
                mapFragment. centerOnPersonPhone(phone, latE6, lngE6); 
            }
        }
    }

   
 
    // ===========================================================
    // Others
    // ===========================================================

}
