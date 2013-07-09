package eu.ttbox.velib.map.localisation;

import java.util.List;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import eu.ttbox.velib.map.geo.GeoLocHelper;

/**
 * Source of original classes :
 * 
 * @see http://gitorious.org/android-maps-api/android-maps-api/blobs/42614538ffda1a6985c398933a85fcd9afc752ee/src/com/google/android/maps/MyLocationOverlay.java
 *      calculate-compass-bearing-heading-to-location-in-android
 * @see http://stackoverflow.com/questions/4308262/calculate-compass-bearing-heading-to-location-in-android  
 * 
 */
@Deprecated
public class MyLocationDirectionOverlay extends Overlay implements LocationListener, SensorEventListener, MyLocationOverlay {

	private static final String TAG = "MyLocationOverlay";
	private static final int LOCALISATION_SIGNIFICATY_NEWER_IN_MS = 1000 * 60 * 1;

	// private final Context context;
	private final MapView mapView;
	// Config
	private boolean compassEnabled;
	private boolean myLocationEnabled;
	// Captor
	private SensorManager sensorManager;
	private LocationManager locationManager;
	private Sensor gsensor;
	private Sensor msensor;
	private Sensor osensor;
	private float[] gravity = new float[3];
	private float[] acceleration = new float[3];
	private float[] sensorMagneticData = new float[3];
	// Instance
	private int azimuth = 0;
	// private float[] mOrientation = { 0.0f, 0.0f, 0.0f };
	private Runnable runOnFirstFix = null;
	private Location lastFix = null;

	// Design
	private Paint paintDirection, paintPosAccuracy, paintPos;

	public MyLocationDirectionOverlay(Context context, MapView mapView) {
		super(context);
		// this.context = context;
		this.mapView = mapView;
		// Captor
		sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		// get compass sensor (ie magnetic field)
		gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		osensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		// Conf Dir
		paintDirection = new Paint();
		paintDirection.setColor(Color.RED);
		paintDirection.setAntiAlias(true);
		paintDirection.setStrokeWidth(5);
		paintDirection.setTextSize(20);
		// Paint Position
		paintPosAccuracy = new Paint();
		paintPosAccuracy.setColor(Color.RED);
		paintPosAccuracy.setAntiAlias(true);
		paintPosAccuracy.setAlpha(50);
		// Accuracy
		paintPos = new Paint();
		paintPos.setColor(Color.RED);
		paintPos.setAntiAlias(true);
	}

	/** Compass **/
	public boolean isCompassEnabled() {
		return compassEnabled;
	}

	public synchronized boolean enableCompass() {

		// boolean isgEnabled = sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_GAME);
		// boolean ismEnabled = sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_GAME);
		// return isgEnabled && ismEnabled;
		return sensorManager.registerListener(this, osensor, SensorManager.SENSOR_DELAY_UI);
	}

	public synchronized void disableCompass() {
		sensorManager.unregisterListener(this);
		compassEnabled = false;
	}

	/** Location **/
	public boolean isMyLocationEnabled() {
		return myLocationEnabled;
	}

	/** @see http://www.vogella.de/articles/AndroidLocationAPI/article.html **/
	public boolean isGpsLocationProviderIsEnable() {
		boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		return enabled;
	}

	public synchronized boolean enableMyLocation() {
		List<String> providers = locationManager.getAllProviders();
		boolean locProviderEnabled = false;
		for (String provider : providers) {
			locationManager.requestLocationUpdates(provider, 0L, 0L, this);
			locProviderEnabled = true;
		}
		myLocationEnabled = locProviderEnabled;
		return myLocationEnabled;
	}

	public synchronized void disableMyLocation() {
		if (myLocationEnabled)
			locationManager.removeUpdates(this);
		myLocationEnabled = false;
		lastFix = null;
	}

	/** Draw **/
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow ) {
		if (!shadow) {
			if (isMyLocationEnabled() && lastFix != null) {
				GeoPoint myLocation = getMyLocation();
				drawMyLocation(canvas, mapView, lastFix, myLocation );
				// Direction Pointing Line
				drawPointingdirection(canvas, mapView, lastFix, myLocation );
			}
		}
//		return false;
	}

	protected void drawCompass(Canvas canvas, float bearing) {
		// int offset = Math.max(canvas.getHeight(), canvas.getWidth()) / 8;
		// Rect r = new Rect(0, 0, 2 * offset, 2 * offset);
		// canvas.drawBitmap(compassBase, null, r, paint);
		// canvas.rotate(-bearing, offset, offset);
		// canvas.drawBitmap(compassArrow, null, r, paint);
	}

	//
	// @Override
	// protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
	// super.drawMyLocation( canvas, mapView, lastFix, myLocation, when);
	//
	// }

	protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation ) {
		Projection p = mapView.getProjection();
		Point loc = p.toPixels(myLocation, null);
		// Orientation v2
		// Canvas g = new Canvas( compassArrow );
		// Paint p = new Paint( Paint.ANTI_ALIAS_FLAG );

		// Matrix matrix = new Matrix();
		// matrix.postRotate(orientation, (compassArrow.getWidth() / 2), (compassArrow.getHeight() / 2));
		// Bitmap resizedBitmap = Bitmap.createBitmap(compassArrow, 0, 0, compassArrow.getWidth(), compassArrow.getHeight(), matrix, true);

		// canvas.drawBitmap(resizedBitmap, loc.x - (compassArrow.getWidth() / 2), loc.y - (compassArrow.getHeight() / 2), paint);
		// canvas.drawBitmap(compassArrow, loc.x - (compassArrow.getWidth() / 2), loc.y- (compassArrow.getHeight() / 2), paint);
 
		// Original Method
		float accuracy = p.metersToEquatorPixels(lastFix.getAccuracy());
		if (accuracy > 10.0f) {
			canvas.drawCircle(loc.x, loc.y, accuracy, paintPosAccuracy);
		}
		canvas.drawCircle(loc.x, loc.y, 10, paintPos);
	}

	protected void drawPointingdirection(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation ) {
		Projection p = mapView.getProjection();
		Point loc = p.toPixels(myLocation, null);
		int orientation = getOrientation();
		// Pythagore

		double cb = 42;//canvas.getWidth();// Math.hypot(canvas.getWidth(), canvas.getHeight());
		// canvas.drawText("Orientation " + getOrientation(), loc.x, loc.y, paintDirection);
		double orientationRad = Math.toRadians(orientation);
		double ca = Math.cos(orientationRad) * cb;
		double ab = Math.sqrt(cb * cb - ca * ca) * (Math.signum(orientation) * -1);
		// Point
		float startX = loc.x;
		float startY = (float) (loc.y - ca);
		float stopY = loc.y;
		// float stopY = (float) (loc.y + ca);
		float stopX = loc.x;
		if (orientation > 180) {
			startX = (float) (loc.x + ab);
			// stopX = (float) (loc.x - ab);
		} else {
			startX = (float) (loc.x - ab);
			// stopX = (float) (loc.x + ab);
		}
		// paintDirection.setStrokeMiter(5);
		canvas.drawLine(startX, startY, stopX, stopY, paintDirection);
	}

	public Location getLastFix() {
		return lastFix;
	}

	public GeoPoint getLastKnownLocationAsGeoPoint() {
		// Location loc = null;
		// Criteria
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		// Ask Last Location
		Location lastKnownLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, true));
 		lastFix = lastKnownLocation;
		GeoPoint myGeoPoint = GeoLocHelper.convertLocationAsGeoPoint(lastKnownLocation);
		return myGeoPoint;
	}

	public GeoPoint getMyLocation() {
		GeoPoint myGeoPoint = GeoLocHelper.convertLocationAsGeoPoint(lastFix);
		return myGeoPoint;
	}

	public int getOrientation() {
		return azimuth;
	}

	public synchronized void onLocationChanged(Location location) {
//		Log.i(TAG, "onLocationChanged " +location);
		if (isBetterLocation(location, lastFix)) {
			lastFix = location;
			if (runOnFirstFix != null) {
				runOnFirstFix.run();
				runOnFirstFix = null;
			}
			mapView.invalidate();
		}
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public synchronized boolean runOnFirstFix(Runnable runnable) {
		if (lastFix == null) {
			runOnFirstFix = runnable;
			return false;
		} else {
			runnable.run();
			return true;
		}
	}
 

	/**
	 * @see http://www.netmite.com/android/mydroid/cupcake/development/samples/Compass/src/com/example/android/compass/CompassActivity.java
	 * @see http://stackoverflow.com/questions/6676377/azimuth-found-with-sensor-type-magnetic-field-much-more-unstable-than-sensor-typ
	 * @see http 
	 *      ://books.google.fr/books?id=c59gCUniP5gC&pg=PA639&lpg=PA639&dq=TYPE_MAGNETIC_FIELD+mapView&source=bl&ots=4wT7sgdYpa&sig=zgNnk9jrZpVfPZRVHtm5kobgiTQ
	 *      &hl=fr&sa=X&ei=oy53T9LLD4HB8QOLxMm6DQ&ved=0CD4Q6AEwAg#v=onepage&q=TYPE_MAGNETIC_FIELD%20mapView&f=false
	 */
	@Override
	public void onSensorChanged(SensorEvent event) {
		int type = event.sensor.getType();
		// if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
		// if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG, "Could not read SENSOR_STATUS_UNRELIABLE for sennsor " + type);
		// return;
		// }
		float[] data = event.values;
		if (type == Sensor.TYPE_ACCELEROMETER) {
			// alpha is calculated as t / (t + dT)
			// with t, the low-pass filter's time-constant
			// and dT, the event delivery rate
			// final float alpha = 0.9f;
			// gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			// gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			// gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

			acceleration[0] = data[0] - gravity[0];
			acceleration[1] = data[1] - gravity[1];
			acceleration[2] = data[2] - gravity[2];
		} else if (type == Sensor.TYPE_MAGNETIC_FIELD) {
			this.sensorMagneticData = data;
		} else if (type == Sensor.TYPE_ORIENTATION) {
			int oldAzimut = this.azimuth;
			this.azimuth = Math.round(event.values[0]);
			if (oldAzimut != this.azimuth) {
				// TODO
				mapView.invalidate();
			}
			return;
		} else {
			// we should not be here.
			return;
		}
		if (true) {

			float[] matrixRotation = new float[16];
			float[] matrixInclination = new float[16];
			SensorManager.getRotationMatrix(matrixRotation, matrixInclination, acceleration, sensorMagneticData);
			// float[] outR = new float[16];
			// SensorManager.remapCoordinateSystem(matrixRotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
			float[] mOrientation = new float[3];
			SensorManager.getOrientation(matrixRotation, mOrientation);
			// Convert the azimuth to degrees in 0.5 degree resolution.
			int newAzimuth = Math.round(Math.round(Math.toDegrees(mOrientation[0])));
			// Adjust the range: 0 < range <= 360 (from: -180 < range <= 180).
			// newAzimuth = (newAzimuth + 360) % 360;
			// alternative newAzimuth = newAzimuth >= 0 ? newAzimuth : newAzimuth + 360;
			//
			if (azimuth != newAzimuth) {
				azimuth = newAzimuth;
				mapView.invalidate();
			}

			// float incl = SensorManager.getInclination(matrixInclination);

		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

//	@Override
//	public boolean onTap(GeoPoint p, MapView map) {
//		// Projection projection = map.getProjection();
//		// Point tapPoint = projection.toPixels(p, null);
//		// Point myPoint = projection.toPixels(getMyLocation(), null);
//		// if (Math.pow(tapPoint.x - myPoint.x, 2.0) + Math.pow(tapPoint.y - myPoint.y, 2.0) < Math.pow(20.0, 2)) {
//		// // Is it within 20 pixels?
//		// return dispatchTap();
//		// } else {
//		// return false;
//		// }
//		return false;
//	}

//	@Override
//	protected boolean dispatchTap() {
//		return false;
//	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new one
	 */
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		} else if (location == null) {
			return false;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > LOCALISATION_SIGNIFICATY_NEWER_IN_MS;
		boolean isSignificantlyOlder = timeDelta < -LOCALISATION_SIGNIFICATY_NEWER_IN_MS;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
}
