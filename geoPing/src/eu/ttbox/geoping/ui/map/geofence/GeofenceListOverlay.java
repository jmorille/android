package eu.ttbox.geoping.ui.map.geofence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import microsoft.mappoint.TileSystem;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.osm.core.AppConstants;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Cap;
import android.os.Handler;

public class GeofenceListOverlay extends Overlay {

    private static final String TAG = "GeofenceListOverlay";

    // Instance
    List<CircleGeofence> geofences = new ArrayList<CircleGeofence>();

    // Color
    Paint paintBorder;
    Paint paintCenter;
    Paint paintText;

    // Cache
    private Point drawPoint = new Point();

    // ===========================================================
    // Constructors
    // ===========================================================

    public GeofenceListOverlay(Context context, Handler handler) {
        super(context);
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

}
