package eu.ttbox.smstraker;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

import eu.ttbox.smstraker.core.AppConstant;
import eu.ttbox.smstraker.domain.TrackPoint;
import eu.ttbox.smstraker.domain.TrackingBDD;

/**
 * @see http://mobiforge.com/developing/story/using-google-maps-android
 * @author deostem
 * 
 */
public class ShowMapActivity extends MapActivity {

	private MapController mapController;
	private MapView mapView;
	private LocationManager locationManager;
	private TrackingBDD trackingBDD;

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

		List<TrackPoint> points;
		Location location;
		Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
		
		public TrackOverlay(List<TrackPoint> trackPoints) {
			this.points = trackPoints;
		}

		public void addOverlay(TrackPoint point) {
			points.add(point);
		}

		/**
		 * @see http
		 *      ://stackoverflow.com/questions/2176397/drawing-a-line-path-on
		 *      -google-maps
		 */
		public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {

			Point lastPoint = null;
//			Path path = new Path();
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
			for (TrackPoint point : points) {
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
//					path.moveTo((float) lastPoint.x, (float) lastPoint.y);
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

	public void addOverlay(List<TrackPoint> points) {
		// Toast.makeText(this, "Add Overlay lat (" + loc .getLatitude() + ", "
		// + loc.getLongitude() + ") " , Toast.LENGTH_SHORT).show();
		TrackOverlay trakPoint = new TrackOverlay(points);
		mapView.getOverlays().add(trakPoint);
		mapView.invalidate();
	}

	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.map); // bind the layout to the activity
		// Local DB
		trackingBDD = new TrackingBDD(this);

		// create a map view
		// RelativeLayout linearLayout = (RelativeLayout)
		// findViewById(R.id.maplayout);
		mapView = (MapView) findViewById(R.id.mapview);
		// mapView.setBuiltInZoomControls(true);
		// mapView.setSatellite(true);
		// mapView.setStreetView(true);
		mapController = mapView.getController();
		mapController.setZoom(14); // Zoon 1 is world view
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new GeoUpdateHandler());

		// Init Value
		trackingBDD.open();
		List<TrackPoint> points = trackingBDD.getTrakPointWithTitre(AppConstant.LOCAL_DB_KEY);
		trackingBDD.close();
		if (points != null && !points.isEmpty()) {
			Toast.makeText(this, "Add Overlay size " + points.size(), Toast.LENGTH_SHORT).show();
			addOverlay(points);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected boolean isLocationDisplayed() {
		return true;
	}

	public class GeoUpdateHandler implements LocationListener {

		public void onLocationChanged(Location location) {
			int lat = (int) (location.getLatitude() * 1E6);
			int lng = (int) (location.getLongitude() * 1E6);
			GeoPoint point = new GeoPoint(lat, lng);
			mapController.animateTo(point); //
			mapController.setCenter(point);
		}

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}

	}

}
