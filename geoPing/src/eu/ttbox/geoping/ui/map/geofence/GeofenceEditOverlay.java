package eu.ttbox.geoping.ui.map.geofence;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MotionEvent;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.GeoFenceProvider;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.domain.pairing.GeoFenceHelper;
import eu.ttbox.geoping.service.geofence.GeofenceUtils;
import eu.ttbox.geoping.ui.geofence.GeofenceEditFragment;
import eu.ttbox.osm.core.AppConstants;
import microsoft.mappoint.TileSystem;

public class GeofenceEditOverlay extends Overlay {

    public static final int MENU_CONTEXTUAL_EDIT = 100;
    private static final String TAG = "GeofenceEditOverlay";
    // Constant
    private final int GEOFENCE_LIST_LOADER = R.id.config_id_geofence_list_loader;
    // Service
    private final LoaderManager loaderManager;
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
// FIXME
// FIXME
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
    // Color
    private Paint paintBorder;
    private Paint paintCenter;
    private Paint paintText;
    private Paint paintArrow;

    // Config
    private float smallCircleRadius = 10;

    // Context
    private FragmentActivity context;
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

    // Cache
    private Path distanceTextPath = new Path();
    private Path nameTextPath = new Path();
    private Point drawPoint = new Point();
    private Point touchPoint = new Point();

    // Service
    private final Geocoder geocoder;

    // ===========================================================
    // Menu Contextual
    // ===========================================================


    private ActionMode.Callback mActionModeCallbackAddGeofence = new ActionMode.Callback() {

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
//            mode.setTitle("XXXXXXXX");
            inflater.inflate(R.menu.geofence_mapoverlay_edit_menu, menu);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            Log.d(TAG, "Click onActionItemClicked itemId : " + item.getItemId() + ", " + item);
            switch (item.getItemId()) {
                case R.id.menu_save:
                    saveGeofenceOverlayEditor();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_delete:
                    deleteGeofenceOverlayEditor();
                    mode.finish(); // Action picked, so close the CAB
                    return true;
                case R.id.menu_edit:
                    // TODO Display

                    GeofencePropDialogFragment dialog =  GeofencePropDialogFragment.newInstance(context, geofence,
                            new GeofencePropDialogFragment.OnEditGeofenceistener() {
                               public void onResult(int resultCode) {
                                    mapView.postInvalidate();
                                }

                    });
                    dialog.show();
                    return true;
                default:
                    Log.w(TAG, "Ignore onActionItemClicked itemId : " + item.getItemId() + ", " + item);
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
//            mActionMode = null;
            geofence = null;
            mapView.postInvalidate();
        }

    };

    // ===========================================================
    // Constructors
    // ===========================================================


    public GeofenceEditOverlay(FragmentActivity context, MapView mapView, LoaderManager loaderManager, Handler handler) {
        this(context, mapView, loaderManager, null, handler);
    }

    public GeofenceEditOverlay(FragmentActivity context, MapView mapView, LoaderManager loaderManager, IGeoPoint center, int radiusInMeters, Handler handler) {
        this(context, mapView, loaderManager, new CircleGeofence(center, radiusInMeters), handler);
    }


    public GeofenceEditOverlay(FragmentActivity context, MapView mapView, LoaderManager loaderManager, CircleGeofence geofence, Handler handler) {
        super(context);
        this.context = context;
        this.geofence = geofence;
        this.mapView = mapView;
        this.handler = handler;
        // Service
        this.loaderManager = loaderManager;
        if (Geocoder.isPresent()) {
            this.geocoder = new Geocoder(context, Locale.getDefault());
        } else {
            this.geocoder = null;
            Log.w(TAG, "The Geocoder is not Present");
        }
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
//        paintText.setARGB(255, 255, 255, 255);
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

    public ActionMode.Callback getMenuActionCallback() {
        return mActionModeCallbackAddGeofence;
    }


    // ===========================================================
    // Result Accessors
    // ===========================================================


    public void doEditCircleGeofence(CircleGeofence geofenceEdit) {
        Log.d(TAG, "Change do editMode for hitPoint : " + geofenceEdit);
        this.geofence = geofenceEdit;
        if (geofenceEdit != null) {
            // Request Contextual Edit Menu
            handler.sendEmptyMessage(MENU_CONTEXTUAL_EDIT);
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
        doEditCircleGeofence(new CircleGeofence(center, radiusInMeters));
    }

    public void saveGeofenceOverlayEditor() {
        if (geofence != null) {
            ContentValues values = GeoFenceHelper.getContentValues(geofence);
            ContentResolver cr = context.getContentResolver();
            if (geofence.id == -1) {
                Uri geofenceUri = cr.insert(GeoFenceProvider.Constants.CONTENT_URI, values);
                long geofenceId = Long.valueOf(geofenceUri.getLastPathSegment());
                geofence.setId(geofenceId);
            } else {
                Uri entityUri = GeoFenceProvider.Constants.getContentUri(geofence.id);
                cr.update(entityUri, values, null, null);
            }
            this.geofence = null;
            mapView.postInvalidate();
        }
    }

    public void deleteGeofenceOverlayEditor() {
        if (geofence != null) {
            if (geofence.id != -1) {
                ContentResolver cr = context.getContentResolver();
                Uri entityUri = GeoFenceProvider.Constants.getContentUri(geofence.id);
                cr.delete(entityUri, null, null);
            }
            this.geofence = null;
            mapView.postInvalidate();
        }
    }

    public void moveCenter(IGeoPoint point) {
        this.geofence.setCenter(point);
        this.geofence.address = null;
        // TODO this.radiusInPixels = (float) TileSystem.GroundResolution(
        // centerGeofence.getLatitudeE6() / AppConstants.E6,
        // mapView.getZoomLevel());
        mapView.postInvalidate();
        if (geocoder != null) {
 //            GeocoderTask geocoderTask = new GeocoderTask(point);
//            geocoderTask.run();
        }
    }

    // ===========================================================
    // Geocoder
    // ===========================================================

    private class  GeocoderTask implements Runnable {
        int latitudeE6;
        int longitudeE6;

        public GeocoderTask(IGeoPoint point) {
            this(point.getLatitudeE6(), point.getLongitudeE6());
        }
        public GeocoderTask(int latE6, int lngE6) {
            this.latitudeE6 = latE6;
            this.longitudeE6 = lngE6;
        }

        @Override
        public void run() {
            try {
                if (geocoder != null) {
                    double latitude = latitudeE6 / AppConstants.E6;
                    double longitude = longitudeE6 / AppConstants.E6;
                    Log.d(TAG, "Ask geocoding (" +  this.latitudeE6 + ", " + this.longitudeE6 + ") as (" +  latitude+ ", " + longitude+ ")");
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        final Address addr = addresses.get(0);
                        String addrString = GeofenceUtils.getAddressAsString(addr);
                        geofence.address = addrString;
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "MyLocation Geocoder Error : " + e.getMessage());
            }
        }

    }


    // ===========================================================
    // Map Draw
    // ===========================================================


    private static final int CACHE_GROUD_RESOLUTION_LATITUDE_E6_DELTA = 999999;
    private float cacheGroundResolution;
    private int cacheGroundResolutionForZoomLevel = Integer.MIN_VALUE;
    private int cacheGroundResolutionForLatitudeE6 = Integer.MIN_VALUE;


    private float getGroundResolution(int latitudeE6, int zoomLevel, boolean useCache) {
        boolean computeResolution = true;
        if (cacheGroundResolutionForZoomLevel!= zoomLevel) {
            // Change Zoom => Recompute
            computeResolution  = true;
        } else if (useCache) {
            // TODO Check Exact Lat approximate match
//            computeResolution = (latitudeE6< (cacheGroundResolutionForLatitudeE6 - CACHE_GROUD_RESOLUTION_LATITUDE_E6_DELTA)) || ;
            computeResolution =  (cacheGroundResolutionForLatitudeE6 == Integer.MIN_VALUE);
        } else {
            // Check Exact Lat match
            computeResolution = latitudeE6 != cacheGroundResolutionForLatitudeE6;
        }
        // Compute Ground Resolution
        float groundResolution = cacheGroundResolution;
        if (computeResolution) {
            double latitude = latitudeE6 / AppConstants.E6;
            groundResolution = (float)TileSystem.GroundResolution(latitude, zoomLevel);
            Log.d(TAG, "Compute GroundResolution Latitude=" + latitude + " : zoom=" + zoomLevel + " ==> ground Resolution = " + cacheGroundResolution);
            // Cache Value
            this.cacheGroundResolution = groundResolution;
            this.cacheGroundResolutionForZoomLevel = zoomLevel;
            this.cacheGroundResolutionForLatitudeE6 = latitudeE6;
        }
        return  groundResolution;
    }

    private float metersToLatitudePixels(final float radiusInMeters, int latitudeE6, int zoomLevel, boolean useCache) {
        float groundResolution = getGroundResolution(latitudeE6, zoomLevel, useCache);
        float radiusInPixelsV2 = (float) (radiusInMeters / groundResolution);
//        Log.d(TAG, "metersToLatitudePixels " + radiusInMeters + " m ==> Pixels = " + radiusInPixelsV2  + "(GroundResolution " + groundResolution + ")" );
        return radiusInPixelsV2;
    }

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (shadow) {
            return;
        }
        // Draw the List
        long fenceEditingId = geofence != null ? geofence.id : -1l;
        for (CircleGeofence fence : geofences) {
            if (fenceEditingId != fence.id) {
                float radiusInPixels = metersToLatitudePixels(fence.getRadiusInMeters(), fence.getLatitudeE6() , mapView.getZoomLevel(), true);
                Point centerScreenPixels = drawGeofenceCircle(canvas, mapView, fence, radiusInPixels);
                // Draw Name text
                drawText(canvas, fence, centerScreenPixels, radiusInPixels, false);
            }
        }

        // Draw Editing Geofence Circle
        if (this.geofence == null) {
            this.radiusInPixels = 0;
            this.centerXInPixels = 0;
            this.centerYInPixels = 0;
        } else {
            this.radiusInPixels = metersToLatitudePixels(this.geofence.radiusInMeters, geofence.getLatitudeE6() , mapView.getZoomLevel(), false);

            Point centerScreenPixels = drawGeofenceCircle(canvas, mapView, this.geofence, radiusInPixels);
            this.centerXInPixels = centerScreenPixels.x;
            this.centerYInPixels = centerScreenPixels.y;

            // Draw Distance & Name text
            drawText(canvas, geofence, centerScreenPixels, radiusInPixels, true);

            // Draw Arrow
            drawArrow(canvas, centerScreenPixels, this.radiusInPixels, angle);
        }
    }


    private Point drawGeofenceCircle(Canvas canvas, MapView mapView, CircleGeofence fence, float radiusInPixels) {
        IGeoPoint centerGeofence = fence.getCenterAsGeoPoint();

        Projection astral = mapView.getProjection();
        Point screenPixels = astral.toPixels(centerGeofence, drawPoint);
        float centerXInPixels = screenPixels.x;
        float centerYInPixels = screenPixels.y;

        canvas.drawCircle(centerXInPixels, centerYInPixels, radiusInPixels, paintBorder);
        canvas.drawCircle(centerXInPixels, centerYInPixels, radiusInPixels, paintCenter);

        return screenPixels;
    }


    private void drawText(Canvas canvas, CircleGeofence geofence, Point centerScreenPixels, float radiusInPixels, boolean drawDistance) {
        // Recompute Text Size
        paintText.setTextSize(radiusInPixels / 4);

        float x = (float) (centerScreenPixels.x - radiusInPixels);
        float y = (float) (centerScreenPixels.y);
        float radiusInPixelsThird = radiusInPixels / 3;

        if (drawDistance) {
            String distanceText = GeofenceUtils.getDistanceText(geofence.radiusInMeters);
            // Draw Distance text
            distanceTextPath.rewind();
            distanceTextPath.moveTo(x, y + radiusInPixelsThird);
            distanceTextPath.lineTo(x + radiusInPixels * 2, y + radiusInPixelsThird);
            canvas.drawTextOnPath(distanceText, distanceTextPath, 0, 0, paintText);
            canvas.drawPath(distanceTextPath, paintText);
        }
        // Draw Name text
        if (geofence.name != null) {
            nameTextPath.rewind();
            nameTextPath.moveTo(x, y - radiusInPixelsThird);
            nameTextPath.lineTo(x + radiusInPixels * 2, y - radiusInPixelsThird);
            canvas.drawTextOnPath(geofence.name, nameTextPath, 0, 0, paintText);
            canvas.drawPath(nameTextPath, paintText);
        }
    }
    // ===========================================================
    // Data Loader
    // ===========================================================



    private void drawArrow(Canvas canvas, Point sPC, float length, double angle) {

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
        boolean isConsume = false;
        Projection pj = mapView.getProjection();
        IGeoPoint tapPoint = pj.fromPixels((int) e.getX(), (int) e.getY());
        Log.d(TAG, "onLongPress : center=" + tapPoint);
        if (this.geofence != null) {
            moveCenter(tapPoint);
            isConsume = true;
        } else {
            // Check that long click was on List Point
            CircleGeofence hitPoint = getHitMapLocation(mapView, tapPoint);
            if (hitPoint != null) {
                doEditCircleGeofence(hitPoint);
                isConsume = true;
            } else {
                doAddCircleGeofence();
                isConsume = true;
            }
        }
        return isConsume;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e, MapView mapView) {
        Projection pj = mapView.getProjection();
        int action = e.getAction();
        if ( this.geofence == null) {
            status = 0;
        }
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
    // Contextual Menu ActionMode
    // ===========================================================

//    private ActionMode mActionMode;

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
    // ===========================================================
    // Other
    // ===========================================================

}
