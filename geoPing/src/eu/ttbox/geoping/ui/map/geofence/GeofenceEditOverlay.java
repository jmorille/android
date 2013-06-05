package eu.ttbox.geoping.ui.map.geofence;

import microsoft.mappoint.TileSystem;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.service.geofence.GeofenceUtils;
import eu.ttbox.osm.core.AppConstants;

public class GeofenceEditOverlay extends Overlay {

    private static final String TAG = "GeofenceEditOverlay";

    // Constant
//    public static final int MOTION_CIRCLE_STOP = 100;
   
    private float smallCircleRadius = 10;

    // Context
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

    // ===========================================================
    // Constructors
    // ===========================================================

    public GeofenceEditOverlay(Context context, MapView mapView, IGeoPoint center, int radiusInMeters, Handler handler) {
        this(context, mapView, new CircleGeofence(center, radiusInMeters), handler);
    }

    public GeofenceEditOverlay(Context context, MapView mapView, CircleGeofence geofence, Handler handler) {
        super(context);
        this.geofence = geofence;
        Log.d(TAG, "GeofenceEditOverlay : " + geofence);
        this.mapView = mapView;
        this.handler = handler;
        initPaint();
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

    public void addCircleGeofence() {
        Log.d(TAG, "addGenceOverlayEditor");
        // Compute the default fence Size
        BoundingBoxE6 boundyBox = mapView.getBoundingBox();
        IGeoPoint center = boundyBox.getCenter();
        int radiusInMeters = boundyBox.getDiagonalLengthInMeters() / 8;
        // Edit
        geofence = new CircleGeofence(center, radiusInMeters);
    }
    
    // ===========================================================
    // Other
    // ===========================================================

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

    private float metersToLatitudePixels(final float radiusInMeters, double latitude, int zoomLevel) {
        float radiusInPixelsV2 = (float) (radiusInMeters / TileSystem.GroundResolution(latitude, zoomLevel));
        return radiusInPixelsV2;
    }

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        // try {
        Projection astral = mapView.getProjection();
        // Draw Geofence Circle
        this.radiusInPixels = metersToLatitudePixels(this.geofence.radiusInMeters, geofence.getLatitudeE6() / AppConstants.E6, mapView.getZoomLevel());

        Point screenPixels = astral.toPixels(this.geofence.getCenterAsGeoPoint(), drawPoint);
        this.centerXInPixels = screenPixels.x;
        this.centerYInPixels = screenPixels.y;

        canvas.drawCircle(centerXInPixels, centerYInPixels, this.radiusInPixels, paintBorder);
        canvas.drawCircle(centerXInPixels, centerYInPixels, this.radiusInPixels, paintCenter);

        
        // Recompute Text Size
        paintText.setTextSize(this.radiusInPixels / 4);
        String distanceText = getDistanceText(this.geofence.radiusInMeters);

        float x = (float) (this.centerXInPixels - this.radiusInPixels);
        float y = (float) (this.centerYInPixels);
        float radiusInPixelsThird = this.radiusInPixels / 3;
        
        // Draw Distance text
        distanceTextPath.rewind();
        distanceTextPath.moveTo(x, y + radiusInPixelsThird);
        distanceTextPath.lineTo(x + this.radiusInPixels * 2, y +radiusInPixelsThird);
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

    @Override
    public boolean onLongPress(final MotionEvent e, final MapView mapView) {
        Projection pj = mapView.getProjection();
        IGeoPoint center = pj.fromPixels((int) e.getX(), (int) e.getY());
        Log.d(TAG, "onDoubleTapEvent : center=" + center);
        moveCenter(center); 
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

}
