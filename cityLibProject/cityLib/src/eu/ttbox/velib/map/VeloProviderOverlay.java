package eu.ttbox.velib.map;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import eu.ttbox.velib.R;
import eu.ttbox.velib.VelibMapActivity;
import eu.ttbox.velib.model.VelibProvider;

@Deprecated
public class VeloProviderOverlay extends Overlay {

    private Context context;
    private Bitmap marker;
    private int markerCenterX;
    private int markerCenterY;
    private VelibMapActivity velibMapViewver;
    private Paint circlePaint;

    public VeloProviderOverlay(Context context, VelibMapActivity velibMapViewver) {
        super(context);
        this.context = context;
        this.velibMapViewver = velibMapViewver;
        initVelibTrackOverlay();
    }

    private void initVelibTrackOverlay() {
        // marker = BitmapFactory.decodeResource(velibMapViewver.getResources(),
        // R.drawable.velo_crop);

        marker = BitmapFactory.decodeResource(context.getResources(), R.drawable.android_cycle);
        markerCenterX = marker.getWidth() / 2;
        markerCenterY = marker.getHeight() / 2;
    }

    private boolean isInBox(GeoPoint refPoint, GeoPoint p, int boxRadius) {
        return (p.getLatitudeE6() <= refPoint.getLatitudeE6() + boxRadius && p.getLatitudeE6() >= refPoint.getLatitudeE6() - boxRadius)
                && (p.getLongitudeE6() <= refPoint.getLongitudeE6() + boxRadius && p.getLongitudeE6() >= refPoint.getLongitudeE6() - boxRadius);
    }

    GeoPoint lastGeoPoint;
    long lastTime;
    long DELTA_TIME_IN_MS = 1000;

    boolean isDoubleTap = false;

    // @Override
    // public boolean onTap(GeoPoint p, MapView mapView) {
    // long now = System.currentTimeMillis();
    // if (lastGeoPoint != null && (lastTime + DELTA_TIME_IN_MS <= now)) {
    // // DOUBLE TAP
    // isDoubleTap = true;
    // }
    // // Keep last poit
    // this.lastGeoPoint = p;
    // this.lastTime = now;
    //
    // return super.onTap(p, mapView);
    // }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        drawMapLocations(canvas, mapView, shadow);
        // return super.draw(canvas, mapView, shadow, when);
    }

    private void drawMapLocations(Canvas canvas, MapView mapView, boolean shadow) {
        if (!shadow) {
            int zoonLevel = mapView.getZoomLevel();
            if (zoonLevel <= 13) {
                Paint paint = getCirclePaint(); 
                for (VelibProvider point : VelibProvider.values()) {
                    Point myScreenCoords = new Point();
                    mapView.getProjection().toPixels(point.asGeoPoint(), myScreenCoords);
                    canvas.drawBitmap(marker, myScreenCoords.x - markerCenterX, myScreenCoords.y - markerCenterY, paint);
                }
            }
        }

    }

    public Paint getCirclePaint() {
        if (circlePaint == null) {
            circlePaint = new Paint();
            circlePaint.setColor(Color.BLUE);
            circlePaint.setAntiAlias(true);
        }
        return circlePaint;
    }

}
