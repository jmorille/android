package eu.ttbox.geoping.ui.map;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase;
import eu.ttbox.geoping.ui.map.mylocation.MyLocationOverlay;

/**
 * @see http://mobiforge.com/developing/story/using-google-maps-android
 * @author deostem
 * 
 */
public class ShowMapActivity extends MapActivity {

    private MapController mapController;
    private MapView mapView;
    private LocationManager locationManager;
    private GeoTrackDatabase trackingBDD;

    // Overlay
    private MyLocationOverlay myLocation;

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuTypeMapSatelite:
            mapView.setSatellite(!mapView.isSatellite());
            return true;
        case R.id.menuTypeMapTraffic:
            mapView.setTraffic(!mapView.isTraffic());
            return true;
        }
        return false;
    }

    class TrackOverlay extends com.google.android.maps.Overlay {

        List<GeoTrack> points;
        Location location;
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.marker);

        public TrackOverlay(List<GeoTrack> trackPoints) {
            this.points = trackPoints;
        }

        public void addOverlay(GeoTrack point) {
            points.add(point);
        }

        /**
         * @see http 
         *      ://stackoverflow.com/questions/2176397/drawing-a-line-path-on
         *      -google-maps
         */
        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {

            Point lastPoint = null;
            // Path path = new Path();
            // Paint Line
            Paint paintLine = new Paint();
            paintLine.setStrokeWidth(5);
            paintLine.setColor(Color.BLUE);
            paintLine.setStyle(Paint.Style.FILL);
            // Paint texte
            Paint paint = new Paint();
            paint.setStrokeWidth(1);
            paint.setTextSize(20);
            // paint.setARGB(255, 255, 255, 255);
            // paint.setColor(Color.GREEN);
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.FILL_AND_STROKE);

            int zoonLevel = mapView.getZoomLevel();
            for (GeoTrack point : points) {
                // Converts lat/lng-Point to OUR coordinates on the screen.
                Point myScreenCoords = new Point();
                mapView.getProjection().toPixels(point.asGeoPoint(), myScreenCoords);
                canvas.drawBitmap(bmp, myScreenCoords.x, myScreenCoords.y, paint);
                if (zoonLevel > 19) {
                    canvas.drawText(point.getTimeAsDate().toLocaleString(), myScreenCoords.x, myScreenCoords.y, paint);
                }
                // Draw line

                if (lastPoint == null) {
                    lastPoint = myScreenCoords;
                } else {
                    // path.moveTo((float) lastPoint.x, (float) lastPoint.y);
                    canvas.drawLine((float) lastPoint.x, (float) lastPoint.y, (float) myScreenCoords.x, (float) myScreenCoords.y, paintLine);

                }
            }
            return super.draw(canvas, mapView, shadow, when);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mapView) {
            // ---when user lifts his finger---
            if (event.getAction() == 1) {
                // mapView.invalidate();
                // GeoPoint p = mapView.getProjection().fromPixels((int)
                // event.getX(), (int) event.getY());
                // Toast.makeText(getBaseContext(), p.getLatitudeE6() / 1E6 +
                // "," + p.getLongitudeE6() / 1E6, Toast.LENGTH_SHORT).show();
            }
            return false;
        }

    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.map); // bind the layout to the activity
        // Local DB
        trackingBDD = new GeoTrackDatabase(this);

        // create a map view
        // RelativeLayout linearLayout = (RelativeLayout)
        // findViewById(R.id.maplayout);
        mapView = (MapView) findViewById(R.id.mapview);
        // mapView.setBuiltInZoomControls(true);
        // mapView.setSatellite(true);
        // mapView.setStreetView(true);
        mapController = mapView.getController();
        mapController.setZoom(17); // Zoon 1 is world view
        // Overlay
        // this.myLocation = new MyLocationOverlay(this.getBaseContext(),
        // this.mapView, mResourceProxy);

    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    protected boolean isLocationDisplayed() {
        return true;
    }

}
