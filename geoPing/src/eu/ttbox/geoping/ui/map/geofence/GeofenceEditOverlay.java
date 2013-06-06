package eu.ttbox.geoping.ui.map.geofence;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.GeoFenceProvider;
import eu.ttbox.geoping.domain.pairing.GeoFenceHelper;
import microsoft.mappoint.TileSystem;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.service.geofence.GeofenceUtils;
import eu.ttbox.osm.core.AppConstants;

public class GeofenceEditOverlay extends Overlay {

    private static final String TAG = "GeofenceEditOverlay";

    // Constant
    private final int GEOFENCE_LIST_LOADER = R.id.config_id_geofence_list_loader;
//    public static final int MOTION_CIRCLE_STOP = 100;

    // Config
    private float smallCircleRadius = 10;

    // Context
    private Context context;
    private MapView mapView;
    private Handler handler;

    // Instance
    private CircleGeofence geofence;

    // Edit Map Instance
    private int status = 0;
    private float radiusInPixels;

    private float centerXInPixels;
    private float centerYInPixels;

    private float smallCircleX;
    private float smallCircleY;

    private float angle = 0;

    // List Instance
    private CopyOnWriteArrayList<CircleGeofence> geofences = new CopyOnWriteArrayList<CircleGeofence>();
    private MyContentObserver geofencesContentObserver;

    // Color
    Paint paintBorder;
    Paint paintCenter;
    Paint paintText;
    Paint paintArrow;

    // Cache
    private Path distanceTextPath = new Path();
    private Path nameTextPath = new Path();
    private Point drawPoint = new Point();
    private Point touchPoint = new Point();

    // Service
    private final LoaderManager loaderManager;
    private Projection astral;

    // ===========================================================
    // Constructors
    // ===========================================================

    public GeofenceEditOverlay(Context context, MapView mapView, LoaderManager loaderManager, Handler handler) {
        this(context, mapView, loaderManager, null, handler);
    }

    public GeofenceEditOverlay(Context context, MapView mapView, LoaderManager loaderManager, IGeoPoint center, int radiusInMeters, Handler handler) {
        this(context, mapView, loaderManager, new CircleGeofence(center, radiusInMeters), handler);
    }

    public GeofenceEditOverlay(Context context, MapView mapView, LoaderManager loaderManager, CircleGeofence geofence, Handler handler) {
        super(context);
        this.context = context;
        this.geofence = geofence;
        this.mapView = mapView;
        this.astral = mapView.getProjection();
        this.handler = handler;
        // Service
        this.loaderManager = loaderManager;
        // Init
        initPaint();
        onResume();
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
        // Arrow
        paintArrow = new Paint();
        // paintArrow.setARGB(255, 147, 186, 228);
        paintArrow.setARGB(255, 228, 0, 147);
        paintArrow.setStrokeWidth(2);
        paintArrow.setAntiAlias(true);
        paintArrow.setStrokeCap(Cap.ROUND);
        paintArrow.setStyle(Paint.Style.FILL);
    }


    // ===========================================================
    // Life Cycle
    // ===========================================================

    public void onResume() {
        Log.d(TAG, "onResume");
        // Load Data
        this.loaderManager.initLoader(GEOFENCE_LIST_LOADER, null, geofencesLoaderCallback);
        // Observer
        if (geofencesContentObserver == null) {
            this.geofencesContentObserver = new MyContentObserver(handler);
            context.getContentResolver().registerContentObserver(GeoFenceProvider.Constants.CONTENT_URI, true, geofencesContentObserver);
        }
    }

    public void onPause() {
        if (geofencesContentObserver != null) {
            context.getContentResolver().unregisterContentObserver(geofencesContentObserver);
            geofencesContentObserver = null;
        }
    }

    public void onDetach(final MapView mapView) {
        Log.i(TAG, "onDetach from mapView");
        onPause();
        super.onDetach(mapView);
    }

    // ===========================================================
    // Result Accessors
    // ===========================================================

    public CircleGeofence getCircleGeofence() {
        // CircleGeofence circleGeofence = geofence != null ? new
        // CircleGeofence(geofence) : new CircleGeofence();
        // Copy valid
        // circleGeofence.setCenter(centerGeofence).setRadiusInMeters(radiusInMeters);
        // return circleGeofence;
        return geofence;
    }

    public void doEditCircleGeofence(CircleGeofence geofenceEdit) {
        Log.d(TAG,"Change do editMode for hitPoint : " + geofenceEdit);
        this.geofence = geofenceEdit;
        if (geofenceEdit!=null) {
            // Request Contextual Edit Menu

        }
        mapView.postInvalidate();
    }

    public void doAddCircleGeofence() {
        Log.d(TAG, "addGenceOverlayEditor");
        // Compute the default fence Size
        BoundingBoxE6 boundyBox = mapView.getBoundingBox();
        IGeoPoint center = boundyBox.getCenter();
        int radiusInMeters = boundyBox.getDiagonalLengthInMeters() / 8;
        // Edit
        geofence = new CircleGeofence(center, radiusInMeters);
        // mapView
        mapView.postInvalidate();
    }

    public void moveCenter(IGeoPoint point) {
        this.geofence.setCenter(point);
        mapView.postInvalidate();
        // TODO this.radiusInPixels = (float) TileSystem.GroundResolution(
        // centerGeofence.getLatitudeE6() / AppConstants.E6,
        // mapView.getZoomLevel());
    }

    public void setRadius(int meters) {
        this.geofence.setRadiusInMeters(meters);
    }

    public int getRadius() {
        return this.geofence.radiusInMeters;
    }

    public IGeoPoint getPoint() {
        return this.geofence.getCenterAsGeoPoint();
    }

    // ===========================================================
    // Map Draw
    // ===========================================================


    private float metersToLatitudePixels(final float radiusInMeters, double latitude, int zoomLevel) {
        float radiusInPixelsV2 = (float) (radiusInMeters / TileSystem.GroundResolution(latitude, zoomLevel));
        return radiusInPixelsV2;
    }

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {

        // Draw the List
        long fenceEditingId = geofence !=null ? geofence.id : -1l;
        for (CircleGeofence fence : geofences) {
            if (fenceEditingId !=   fence.id ) {
                float radiusInPixels = metersToLatitudePixels(fence.getRadiusInMeters(), fence.getLatitudeE6() / AppConstants.E6, mapView.getZoomLevel());
                drawGeofenceCircle(canvas, mapView, fence, radiusInPixels);
            }
        }

        // Draw Editing Geofence Circle
        if (this.geofence == null) {
            this.radiusInPixels = 0;
            this.centerXInPixels = 0;
            this.centerYInPixels = 0;
        } else {
            this.radiusInPixels = metersToLatitudePixels(this.geofence.radiusInMeters, geofence.getLatitudeE6() / AppConstants.E6, mapView.getZoomLevel());

            Point screenPixels = drawGeofenceCircle(canvas, mapView, this.geofence, radiusInPixels);
            this.centerXInPixels = screenPixels.x;
            this.centerYInPixels = screenPixels.y;

            // Recompute Text Size
            paintText.setTextSize(this.radiusInPixels / 4);
            String distanceText = getDistanceText(this.geofence.radiusInMeters);

            float x = (float) (this.centerXInPixels - this.radiusInPixels);
            float y = (float) (this.centerYInPixels);
            float radiusInPixelsThird = this.radiusInPixels / 3;

            // Draw Distance text
            distanceTextPath.rewind();
            distanceTextPath.moveTo(x, y + radiusInPixelsThird);
            distanceTextPath.lineTo(x + this.radiusInPixels * 2, y + radiusInPixelsThird);
            canvas.drawTextOnPath(distanceText, distanceTextPath, 0, 0, paintText);
            canvas.drawPath(distanceTextPath, paintText);

            // Draw Name text
            if (geofence != null && geofence.name != null) {
                nameTextPath.rewind();
                nameTextPath.moveTo(x, y - radiusInPixelsThird);
                nameTextPath.lineTo(x + this.radiusInPixels * 2, y - radiusInPixelsThird);
                canvas.drawTextOnPath(geofence.name, nameTextPath, 0, 0, paintText);
                canvas.drawPath(nameTextPath, paintText);
            }
            // Draw Arrow
            drawArrow(canvas, screenPixels, this.radiusInPixels, angle);
        }
    }

    private Point drawGeofenceCircle(Canvas canvas, MapView mapView, CircleGeofence fence, float radiusInPixels) {
        IGeoPoint centerGeofence = fence.getCenterAsGeoPoint();

        Point screenPixels = astral.toPixels(centerGeofence, drawPoint);
        int centerXInPixels = screenPixels.x;
        int centerYInPixels = screenPixels.y;

        canvas.drawCircle(centerXInPixels, centerYInPixels, radiusInPixels, paintBorder);
        canvas.drawCircle(centerXInPixels, centerYInPixels, radiusInPixels, paintCenter);

        return screenPixels;
    }

    private String getDistanceText(int radiusInMeters) {
        String distanceText;
        if (radiusInMeters > 1000) {
            int km = radiusInMeters / 1000;
            int m = radiusInMeters % 1000;
            distanceText = Integer.toString(km) + " km, " + Integer.toString(m) + " m";
        } else {
            distanceText = Integer.toString(radiusInMeters) + " m";
        }
        return distanceText;
    }

    public void drawArrow(Canvas canvas, Point sPC, float length, double angle) {

        float x = (float) (sPC.x + length * Math.cos(angle));
        float y = (float) (sPC.y + length * Math.sin(angle));
        canvas.drawLine(sPC.x, sPC.y, x, y, paintArrow);

        // canvas.drawCircle(x, y, 10, paint);

        canvas.drawCircle(sPC.x, sPC.y, 5, paintArrow);

        smallCircleX = x;
        smallCircleY = y;

        canvas.drawCircle(x, y, 8, paintArrow);

    }

    // ===========================================================
    // Data Loader
    // ===========================================================


    private class MyContentObserver extends ContentObserver {

        public MyContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            Log.e(TAG, "########################################");
            Log.e(TAG, "### ContentObserver Notify Change for URI : " + uri);
            loaderManager.restartLoader(GEOFENCE_LIST_LOADER, null, geofencesLoaderCallback);
            super.onChange(selfChange);
        }
    }

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
            if (cursor.moveToFirst()) {
                do {
                    CircleGeofence fence = helper.getEntity(cursor);
                    points.add(fence);
                } while (cursor.moveToNext());
                geofences = new CopyOnWriteArrayList<CircleGeofence>(points);
            } else if (geofences != null && !geofences.isEmpty()) {
                geofences.clear();
            }
            mapView.postInvalidate();
            // cursor.registerDataSet\
            // Listener
            // cursor.setNotificationUri(context.getContentResolver(),
            // GeoFenceProvider.Constants.CONTENT_URI);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            geofences = new CopyOnWriteArrayList<CircleGeofence>();
        }
    };

    // ===========================================================
    // Touch Event
    // ===========================================================

    private CircleGeofence getHitMapLocation(MapView mapView, IGeoPoint tapPoint) {
        for (CircleGeofence testLocation : geofences) {
            boolean isOncircle = GeofenceUtils.isOnCircle(tapPoint, testLocation.getCenterAsGeoPoint(), testLocation.getRadiusInMeters());
            if (isOncircle) {
                return testLocation;
            }
        }

        return null;
    }

    @Override
    public boolean onLongPress(final MotionEvent e, final MapView mapView) {
        Projection pj = mapView.getProjection();
        IGeoPoint tapPoint = pj.fromPixels((int) e.getX(), (int) e.getY());
        Log.d(TAG, "onLongPress : center=" + tapPoint);
        if (this.geofence != null) {
            moveCenter(tapPoint);
        } else {
            // Check that long click was on List Point
            CircleGeofence hitPoint = getHitMapLocation(mapView, tapPoint);
            if (hitPoint!=null) {

                doEditCircleGeofence ( hitPoint);
            }
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e, MapView mapView) {
        Projection pj = mapView.getProjection();
        int action = e.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                // Click Point
                Point p = pj.fromMapPixels((int) e.getX(), (int) e.getY(), touchPoint);
                float x = p.x;
                float y = p.y;
                // Compute Point Click
                boolean onCircle = GeofenceUtils.isOnCircle(x, y, this.smallCircleX, this.smallCircleY, this.smallCircleRadius + 20);
                boolean onCenter = false;
                if (!onCircle) {
                    onCenter = GeofenceUtils.isOnCircle(x, y, this.centerXInPixels, this.centerYInPixels, this.smallCircleRadius + 20);
                    Log.d(TAG, "onTouchEvent : onCenter = " + onCenter);
                }
                // Manage Status
                if (onCircle) {
                    this.status = 1;
                } else if (onCenter) {
                    this.status = 2;
                } else
                    this.status = 0;
                Log.d(TAG, "MotionEvent.ACTION_DOWN : status = " + status);
            }
            break;
            case MotionEvent.ACTION_UP:
                if (this.status > 0) {
                    this.status = 0;
                    mapView.postInvalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (this.status == 1) {
                    // Click Point
                    Point p = pj.fromMapPixels((int) e.getX(), (int) e.getY(), touchPoint);
                    float x = p.x;
                    float y = p.y;

                    Log.d(TAG, "MotionEvent.ACTION_MOVE circle : status = " + status);
                    double dist = Math.sqrt(Math.pow(Math.abs(this.centerXInPixels - x), 2) + Math.pow(Math.abs(this.centerYInPixels - y), 2));
                    this.geofence.radiusInMeters = (int) Math.round((dist * this.geofence.radiusInMeters) / this.radiusInPixels);
                    Log.d(TAG, "MotionEvent.ACTION_MOVE : radiusInMeters = " + geofence.radiusInMeters);

                    // Recalculate angle
                    float opp = this.centerYInPixels - y;
                    float adj = this.centerXInPixels - x;
                    float tan = Math.abs(opp) / Math.abs(adj);
                    this.angle = (float) Math.atan(tan);
                    if (opp > 0) {
                        if (adj > 0) {
                            this.angle += Math.PI;
                        } else {
                            this.angle = this.angle * -1;
                        }
                    } else {
                        if (adj > 0) {
                            this.angle = (float) Math.PI - this.angle;
                        } else {
                            // Okay
                        }
                    }
                    mapView.postInvalidate();
                    // handler.sendEmptyMessage(MOTION_CIRCLE_STOP);
                } else if (this.status == 2) {
                    IGeoPoint center = pj.fromPixels((int) e.getX(), (int) e.getY());
                    moveCenter(center);
                    mapView.postInvalidate();
                }
                break;
        }
        return this.status > 0 ? true : super.onTouchEvent(e, mapView);
    }

    // ===========================================================
    // Other
    // ===========================================================

}
