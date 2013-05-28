package eu.ttbox.geoping.ui.map.geofence;

 import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
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
import eu.ttbox.geoping.service.geofence.GeofenceUtils;

public class GeofenceEditOverlay extends Overlay {

    private static final String TAG = "GeofenceEditOverlay";

    public static final int MOTION_CIRCLE_STOP = 100;
    private IGeoPoint point;

    private float radiusInPixels;
    private int radiusInMeters = 500;

    private float centerXInPixels;
    private float centerYInPixels;

    private int status = 0;

    private float smallCircleX;
    private float smallCircleY;
    private float smallCircleRadius = 10;

    private float angle = 0;

    private Handler handler;

    // Color
    Paint paintBorder;
    Paint paintCenter;
    Paint paintText;
    Paint paintArrow;
    
    // Cache
    private Point touchPoint = new Point();

    public GeofenceEditOverlay(Context context, IGeoPoint center, Handler handler) {
        super(context);
        point = center;
        this.handler = handler;
        initPaint();
    }

  
    
    private void initPaint() {
        // Circle Border
        paintBorder = new Paint();
//        paintBorder.setARGB(100, 147, 186, 228);
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
//        paintArrow.setARGB(255, 147, 186, 228);
        paintArrow.setARGB(255, 228, 0, 147);
        paintArrow.setStrokeWidth(2);
        paintArrow.setAntiAlias(true);
        paintArrow.setStrokeCap(Cap.ROUND);
        paintArrow.setStyle(Paint.Style.FILL);
    }

    public void moveCenter(IGeoPoint point) {
        this.point = point;
    }

    public void setRadius(int meters) {
        this.radiusInMeters = meters;
    }

    public int getRadius() {
        return this.radiusInMeters;
    }

    public IGeoPoint getPoint() {
        return this.point;
    }


    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        try {
            Projection astral = mapView.getProjection();
            Point screenPixels = astral.toPixels(this.point, null);
            this.radiusInPixels = astral.metersToEquatorPixels(this.radiusInMeters);
            this.centerXInPixels = screenPixels.x;
            this.centerYInPixels = screenPixels.y;

            canvas.drawCircle(screenPixels.x, screenPixels.y, this.radiusInPixels, paintBorder);

            canvas.drawCircle(screenPixels.x, screenPixels.y, this.radiusInPixels, paintCenter);
            // Recompute Text Size
            paintText.setTextSize(this.radiusInPixels / 4);
            String text;
            if (this.radiusInMeters > 1000) {
                int km = this.radiusInMeters / 1000;
                int m = this.radiusInMeters % 1000;
                text = Integer.toString(km) + " km, " + Integer.toString(m) + " m";
            } else {
                text = Integer.toString(this.radiusInMeters) + " m";
            }

            float x = (float) (this.centerXInPixels + this.radiusInPixels * Math.cos(Math.PI));
            float y = (float) (this.centerYInPixels + this.radiusInPixels * Math.sin(Math.PI));

            // lol
          
            Path tPath = new Path();
            tPath.moveTo(x, y + this.radiusInPixels / 3);
            tPath.lineTo(x + this.radiusInPixels * 2, y + this.radiusInPixels / 3);
            canvas.drawTextOnPath(text, tPath, 0, 0, paintText);
            canvas.drawPath(tPath, paintText);

            drawArrow(canvas, screenPixels, this.radiusInPixels, angle);
        } catch (Exception e) {

        }
        // super.draw(canvas, mapView, shadow);
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
    public boolean onDoubleTapEvent(final MotionEvent e, final MapView mapView) { 
        Projection pj = mapView.getProjection();
        IGeoPoint center =  pj.fromPixels((int) e.getX(), (int) e.getY() );
        Log.d(TAG, "onDoubleTapEvent : center=" + center );
        moveCenter(center);
        handler.sendEmptyMessage(MOTION_CIRCLE_STOP);
        return true;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent e, MapView mapView) {
        // float x = e.getX();
        // float y = e.getY();
        Projection pj = mapView.getProjection();
        Point p = pj.fromMapPixels((int) e.getX(), (int) e.getY(), touchPoint);

        float x = p.x;
        float y = p.y;

        int action = e.getAction();

        boolean onCircle = GeofenceUtils.isOnCircle(x, y, this.smallCircleX, this.smallCircleY, this.smallCircleRadius + 20);
        Log.d(TAG, "onCircle = " + onCircle + " : x=" + x + ", y=" + y + ", smallCircleX=" + smallCircleX + ", smallCircleY=" + smallCircleY);

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (onCircle) {
                this.status = 1;
            } else
                this.status = 0;
            Log.d(TAG, "MotionEvent.ACTION_DOWN : status = " + status);
            break;
        case MotionEvent.ACTION_UP:
            if (this.status == 1) {
                this.status = 0;
                handler.sendEmptyMessage(MOTION_CIRCLE_STOP);
            }
            Log.d(TAG, "MotionEvent.ACTION_UP : status = " + status);
            break;
        case MotionEvent.ACTION_MOVE:
            if (this.status == 1) {
                Log.d(TAG, "MotionEvent.ACTION_MOVE : status = " + status);
                double dist = Math.sqrt(Math.pow(Math.abs(this.centerXInPixels - x), 2) + Math.pow(Math.abs(this.centerYInPixels - y), 2));
                this.radiusInMeters = (int) ((int) (dist * this.radiusInMeters) / this.radiusInPixels);
                Log.d(TAG, "MotionEvent.ACTION_MOVE : radiusInMeters = " + radiusInMeters);
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

                handler.sendEmptyMessage(MOTION_CIRCLE_STOP);
            }
            break;
        }
        return this.status == 1 ? true : super.onTouchEvent(e, mapView);
    }

}
