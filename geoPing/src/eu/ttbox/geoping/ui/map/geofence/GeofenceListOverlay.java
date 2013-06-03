package eu.ttbox.geoping.ui.map.geofence;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;

import microsoft.mappoint.TileSystem;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MotionEvent;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.GeoFenceProvider;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.domain.pairing.GeoFenceHelper;
import eu.ttbox.osm.core.AppConstants;

public class GeofenceListOverlay extends Overlay {

    private static final String TAG = "GeofenceListOverlay";
    private final int GEOFENCE_LIST_LOADER = R.id.config_id_geofence_list_loader;

    // Context
    private Context context;

    // Instance
    private CopyOnWriteArrayList<CircleGeofence> geofences = new CopyOnWriteArrayList<CircleGeofence>();

    // Color
    private Paint paintBorder;
    private Paint paintCenter;
    private Paint paintText;

    // Cache
    private Point drawPoint = new Point();

    // Service
    private final LoaderManager loaderManager;

    // ===========================================================
    // Constructors
    // ===========================================================

    public GeofenceListOverlay(Context context, LoaderManager loaderManager, Handler handler) {
        super(context);
        this.context = context;
        initPaint();

        // Service
        this.loaderManager = loaderManager;

        // Load Data
        this.loaderManager.initLoader(GEOFENCE_LIST_LOADER, null, geofencesLoaderCallback);
    }

    private void initPaint() {
        // Circle Border
        paintBorder = new Paint();
        // paintBorder.setARGB(100, 147, 186, 228);
        paintBorder.setARGB(100, 228, 0, 147);
        paintBorder.setStrokeWidth(2);
        paintBorder.setAntiAlias(true);
        paintBorder.setStyle(Paint.Style.STROKE);
        // Circle Center
        paintCenter = new Paint(paintBorder);
        paintCenter.setStyle(Paint.Style.FILL);
        paintCenter.setAlpha(20);
        // Text Color
        paintText = new Paint();
        paintText.setARGB(255, 255, 255, 255);
        paintText.setAntiAlias(true);
        paintText.setTextAlign(Paint.Align.CENTER);
    }

    // ===========================================================
    // Draw
    // ===========================================================

    private float metersToLatitudePixels(final float radiusInMeters, double latitude, int zoomLevel) {
        float radiusInPixelsV2 = (float) (radiusInMeters / TileSystem.GroundResolution(latitude, zoomLevel));
        return radiusInPixelsV2;
    }

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        Projection astral = mapView.getProjection();
        for (CircleGeofence fence : geofences) {
            IGeoPoint centerGeofence = fence.getCenterAsGeoPoint();
            float radiusInPixels = metersToLatitudePixels(fence.getRadius(), fence.getLatitudeE6() / AppConstants.E6, mapView.getZoomLevel());

            Point screenPixels = astral.toPixels(centerGeofence, drawPoint);
            int centerXInPixels = screenPixels.x;
            int centerYInPixels = screenPixels.y;

            canvas.drawCircle(centerXInPixels, centerYInPixels, radiusInPixels, paintBorder);
            canvas.drawCircle(centerXInPixels, centerYInPixels, radiusInPixels, paintCenter);

        }
    }

    // ===========================================================
    // Data Loader
    // ===========================================================

    private final LoaderManager.LoaderCallbacks<Cursor> geofencesLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String selection = null;
            String[] selectionArgs = null;
            String sortOrder = null;
            // Loader
            CursorLoader cursorLoader = new CursorLoader(context, GeoFenceProvider.Constants.CONTENT_URI, null, selection, selectionArgs, sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            int resultCount = cursor.getCount();
            Log.d(TAG, String.format("Found %s Geofences", resultCount));
            ArrayList<CircleGeofence> points = new ArrayList<CircleGeofence>();
            if (resultCount < 1) {

            }
            GeoFenceHelper helper = new GeoFenceHelper().initWrapper(cursor);
            while (cursor.moveToNext()) {
                CircleGeofence fence = helper.getEntity(cursor);
                points.add(fence);
            }
            geofences = new CopyOnWriteArrayList<CircleGeofence>(points);
           //  cursor.registerDataSet\
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            geofences = new CopyOnWriteArrayList<CircleGeofence>();
        }
    };

    // ===========================================================
    // Touch Event
    // ===========================================================

    @Override
    public boolean onLongPress(final MotionEvent event, final MapView mapView) {
        Projection pj = mapView.getProjection();

        // Convert
        IGeoPoint geoPoint = pj.fromPixels(event.getX(), event.getY());
        Point point = pj.fromMapPixels((int) event.getX(), (int) event.getY(), null);

        // Get Hit Points
        Log.d(TAG, "--- ----------------------------------------------");
        Log.d(TAG, String.format("--- onLongPress Event    x=%s,\t  y=%s", event.getX(), event.getY()));
        Log.d(TAG, String.format("--- onLongPress geoPoint x=%s,\t  y=%s", geoPoint.getLatitudeE6(), geoPoint.getLongitudeE6()));
        Log.d(TAG, String.format("--- onLongPress geoPoint x=%s,\t  y=%s", point.x, point.y));
        Log.d(TAG, "--- ----------------------------------------------");


        return false;
    }

    private CircleGeofence getHitMapLocation(MapView mapView, IGeoPoint tapPoint) {
        CircleGeofence result = null;
        Projection pj = mapView.getProjection();
        for (CircleGeofence testLocation : geofences) {

        }

        return result;
    }

}
