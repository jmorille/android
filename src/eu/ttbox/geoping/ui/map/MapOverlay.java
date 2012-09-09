package eu.ttbox.geoping.ui.map;

import org.osmdroid.ResourceProxy;
import org.osmdroid.google.overlay.GoogleTilesOverlay;
import org.osmdroid.tileprovider.MapTileProviderBase;

import android.content.Context;
import android.view.MotionEvent;

import com.google.android.maps.MapView;

public class MapOverlay extends GoogleTilesOverlay {

    public MapOverlay(MapTileProviderBase aTileProvider, Context aContext) {
        super(aTileProvider, aContext);
    }

    public MapOverlay(MapTileProviderBase aTileProvider, ResourceProxy pResourceProxy) {
        super(aTileProvider, pResourceProxy);
    }

//    @Override
    public void onDetach(MapView mapView) {
       
    }
    
    // TODO
    public boolean onSingleTapUp(final MotionEvent event, final MapView mapView) {
        return false;
    }

}
