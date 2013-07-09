package eu.ttbox.osm.ui.map;

import org.osmdroid.ResourceProxy;
import org.osmdroid.tileprovider.MapTileProviderBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.views.MapView;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Handler;
import eu.ttbox.osm.tiles.MapTileProviderTTbox;
import eu.ttbox.osm.tiles.MyAppTilesProviders;

public class MapViewFactory {

    // public static MapView createOsmMapView(Context context, String
    // gooleApiKey) {
    // GoogleMapView mapView2 = new GoogleMapView(context, gooleApiKey);
    // }

    public static MapView createOsmMapView(Context context, ResourceProxy mResourceProxy, ITileSource tileSource, ActivityManager activityManager) {
    	 int cacheSizeInBytes = MapTileProviderTTbox.getCacheSizeSuggested(activityManager);
    	 return createOsmMapView(context, mResourceProxy, tileSource, cacheSizeInBytes);
    }
    
   
    
    public static MapView createOsmMapView(Context context, ResourceProxy mResourceProxy, ITileSource tileSource, int cacheSizeInBytes) {
        Handler tileRequestCompleteHandler = null;
        MapTileProviderBase aTileProvider  =  new MapTileProviderTTbox(context, tileSource,   cacheSizeInBytes); 
    	 
        MapView mapView = new MapView(context, 256, mResourceProxy, aTileProvider, tileRequestCompleteHandler);
        mapView.setMultiTouchControls(true);
        mapView.setHapticFeedbackEnabled(true);
        // mapView.setBuiltInZoomControls(true);
        // Init Tiles
        // Tiles
        MyAppTilesProviders.initTilesSource(context);
        return mapView;
    }
}
