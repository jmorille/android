package eu.ttbox.geoping.ui.map.geoping;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v4.app.LoaderManager;
import android.view.View;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.ui.map.track.GeoTrackOverlay.GeotrackLastAddedListener;
import eu.ttbox.osm.ui.map.mylocation.bubble.MapCalloutView;

public class GeoPingOverlay extends Overlay {

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
 
    public GeoPingOverlay(final Context ctx, final MapView mapView, LoaderManager loaderManager, Person userId, long timeDay, GeotrackLastAddedListener geotrackLastAddedListener) {
        this(ctx, mapView, new DefaultResourceProxyImpl(ctx), loaderManager, userId, timeDay,   geotrackLastAddedListener);
    }
    
    public GeoPingOverlay(final Context ctx, final MapView mapView, final ResourceProxy pResourceProxy, LoaderManager loaderManager, Person person, long timeInMs, GeotrackLastAddedListener geotrackLastAddedListener) {
        super(pResourceProxy);
        // Inititalise
        this.context = ctx;
//        this.person = person;
//        this.loaderManager = loaderManager;
        this.mapView = mapView;
        this.mMapController = mapView.getController();
        //
    }

    // ===========================================================
    // Map Drawable 
    // ===========================================================

    @Override
    protected void draw(Canvas c, MapView osmv, boolean shadow) {
        // TODO Auto-generated method stub
        
    }
    
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
