package eu.ttbox.geoping.ui.map.geoping;

import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;

import android.content.Context;
import android.view.View;
import eu.ttbox.osm.ui.map.mylocation.bubble.MapCalloutView;

public class GeoPingOverlay {

    private static final String TAG = "GeoPingOverlay";
    
    // Map
    private Context context;
    private final MapController mMapController;
    private final MapView mapView;

    // Bubble
    private static final int INDEX_FIRST = 0;
    private static final int INDEX_SECOND = 1;
    
    private MapCalloutView mMapCallouts[] = new MapCalloutView[2];
    private int mMapCalloutIndex;

    // ===========================================================
    // Constructor
    // ===========================================================

    
    // ===========================================================
    // Show Bubble
    // ===========================================================


 
    public void showCallout(int position) {
        final MapCalloutView mapCalloutView = getNextMapCallout();
//        mapCalloutView.setData(annotation);
        
    }
    
    private MapCalloutView getMapCallout(int index) {
        if (mMapCallouts[index] == null) {
            mMapCallouts[index] = new MapCalloutView(context );
            mMapCallouts[index].setVisibility(View.GONE);
//            mMapCallouts[index].setOnClickListener(mOnClickListener);
//            mMapCallouts[index].setOnDoubleTapListener(mOnDoubleTapListener);
        }
        return mMapCallouts[index];
    }

    private MapCalloutView getCurrentMapCallout() {
        return getMapCallout(mMapCalloutIndex);
    }

    private MapCalloutView getNextMapCallout() {
        if (mMapCalloutIndex == INDEX_FIRST) {
            mMapCalloutIndex = INDEX_SECOND;
        } else {
            mMapCalloutIndex = INDEX_FIRST;
        }
        return getMapCallout(mMapCalloutIndex);
    }
    
}
