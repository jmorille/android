package eu.ttbox.velib.map.osm;

import java.util.List;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.api.IProjection;
import org.osmdroid.google.wrapper.GeoPoint;
import org.osmdroid.google.wrapper.MapController;
import org.osmdroid.google.wrapper.Projection;

import com.google.android.maps.Overlay;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;

public class GoogleMapView implements IMapView {

    private final com.google.android.maps.MapView mMapView;

    public GoogleMapView(final com.google.android.maps.MapView pMapView) {
        mMapView = pMapView;
    }

    public GoogleMapView(final Context pContext, final AttributeSet pAttrs, final int pDefStyle) {
        this(new com.google.android.maps.MapView(pContext, pAttrs, pDefStyle));
    }

    public GoogleMapView(final Context pContext, final AttributeSet pAttrs) {
        this(new com.google.android.maps.MapView(pContext, pAttrs));
    }

    public GoogleMapView(final Context pContext, final String pApiKey) {
        this(new com.google.android.maps.MapView(pContext, pApiKey));
    }

    @Override
    public IMapController getController() {
        return new MapController(mMapView.getController());
    }

    @Override
    public IProjection getProjection() {
        return new Projection(mMapView.getProjection());
    }

    @Override
    public int getZoomLevel() {
        return mMapView.getZoomLevel();
    }

    @Override
    public int getLatitudeSpan() {
        return mMapView.getLatitudeSpan();
    }

    @Override
    public int getLongitudeSpan() {
        return mMapView.getLongitudeSpan();
    }

    @Override
    public IGeoPoint getMapCenter() {
        return new GeoPoint(mMapView.getMapCenter());
    }

    @Override
    public int getMaxZoomLevel() {
        return mMapView.getMaxZoomLevel();
    }

    @Override
    public void setBackgroundColor(final int pColor) {
        // this doesn't seem to have any visible effect on the Google MapView
        mMapView.setBackgroundColor(pColor);
    }
    
    public void invalidate() {
        mMapView.invalidate(); 
    }

    public void invalidate(Rect dirty) {
        mMapView.invalidate(dirty); 
    }
    
    public  List<Overlay> getOverlays() {
       List<Overlay> overLay = mMapView.getOverlays();
       return overLay;
    }
}
