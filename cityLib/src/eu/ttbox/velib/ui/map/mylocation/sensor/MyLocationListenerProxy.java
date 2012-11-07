package eu.ttbox.velib.ui.map.mylocation.sensor;

import java.util.List;

import org.osmdroid.util.GeoPoint;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import eu.ttbox.velib.map.geo.GeoLocHelper;

public class MyLocationListenerProxy implements LocationListener {

	private static final String TAG = "LocationListenerProxy";


	private final LocationManager locationManager;
	private LocationListener mListener = null;

	// Config Data
	private long pUpdateTime = 0L;
	private float pUpdateDistance = 0f;
 
	// Instance Data
	private Location lastFix;
	private GeoPoint lastFixAsGeoPoint;

	// to avoid allocations during onDraw
	boolean isFixLocation = false;
	
	public MyLocationListenerProxy(final LocationManager pLocationManager) {
		locationManager = pLocationManager;
	}

	public boolean startListening(final LocationListener pListener) {
		boolean result = false;
		mListener = pListener;
		// Do not use .getProviders(true) for receiving activation notifications
		List<String> providers = locationManager.getAllProviders();
		if (providers != null && !providers.isEmpty()) {
			for (final String provider : providers) {
				// if (LocationManager.GPS_PROVIDER.equals(provider)
				// || LocationManager.NETWORK_PROVIDER.equals(provider)) {
				result = true;
				locationManager.requestLocationUpdates(provider, pUpdateTime, pUpdateDistance, this);
				if (Log.isLoggable(TAG, Log.DEBUG))
					Log.d(TAG, String.format("requestLocationUpdates for provider : [%s]", provider));
				// }pLocationManager
			}
		}
		return result;
	}

	public void stopListening() {
		mListener = null;
		locationManager.removeUpdates(this);
	}

	// ===========================================================
	// Location Listener Methods
	// ===========================================================

	@Override
	public void onLocationChanged(final Location location) {
		if (Log.isLoggable(TAG, Log.DEBUG))
			Log.d(TAG, String.format("onLocationChanged  [%s] : %s", location.getProvider(), location));

		// ??? ignore temporary non-gps fix ???
		// if (mIgnorer.shouldIgnore(location.getProvider(),
		// System.currentTimeMillis())) {
		// Log.d("Ignore temporary non-gps location");
		// return;
		// }
		if (!LocationUtils.isBetterLocation(location, lastFix)) {
			return;
		}
		if (!isFixLocation) {
			isFixLocation = true;
		}
		// Save to local cache
		defineLocation(location);

		// Propagate the Location
		if (mListener != null) {
			mListener.onLocationChanged(location);
		}
		 
	}

	@Override
	public void onStatusChanged(final String provider, final int status, final Bundle extras) {
		if (Log.isLoggable(TAG, Log.DEBUG))
			Log.d(TAG, String.format("onStatusChanged  [%s] : %s - %s", provider, status, extras));
		if (mListener != null) {
			mListener.onStatusChanged(provider, status, extras);
		}
	}

	@Override
	public void onProviderDisabled(final String provider) {
		// if (Log.isLoggable(TAG, Log.INFO))
		if (Log.isLoggable(TAG, Log.DEBUG))
			Log.d(TAG, "Remove Localisation provider : " + provider);

		if (mListener != null) {
			mListener.onProviderDisabled(provider);
		}
	}

	@Override
	public void onProviderEnabled(final String provider) {
		if (Log.isLoggable(TAG, Log.DEBUG))
			Log.d(TAG, "Add Localisation provider : " + provider);
		// Notify listener
		if (mListener != null) {
			mListener.onProviderEnabled(provider);
		}
	}

	// ===========================================================
	// Local Setters
	// ===========================================================

	private void defineLocation(Location location) {
		this.lastFix = location;
		this.lastFixAsGeoPoint = GeoLocHelper.convertLocationAsGeoPoint(location, this.lastFixAsGeoPoint);
	}

	// ===========================================================
	// Public Accessors
	// ===========================================================

	public boolean isMyLocationEnabled() {
		return mListener != null;
	}
	
	public boolean isFixLocation() {
		return isFixLocation;
	}
	public Location getLastFix() {
		return lastFix;
	}

	public GeoPoint getLastFixAsGeoPoint() {
		return lastFixAsGeoPoint;
	}

	// ===========================================================
	// Last Known Location
	// ===========================================================
 

	public Location getLastKnownLocation() {
		Location lastKnownLocation = LocationUtils.getLastKnownLocation(locationManager); 
		if (lastKnownLocation != null) {
			if (Log.isLoggable(TAG, Log.DEBUG))
				Log.d(TAG, String.format("Use LastKnownLocation with provider [%s] : %s", lastKnownLocation.getProvider(), lastKnownLocation));
			if (lastFix == null) {
				defineLocation(lastKnownLocation);
			}
		}
		return lastKnownLocation;
	}

	public GeoPoint getLastKnownLocationAsGeoPoint() {
		Location lastKnownLocation = this.getLastKnownLocation();
		GeoPoint myGeoPoint = GeoLocHelper.convertLocationAsGeoPoint(lastKnownLocation);
		return myGeoPoint;
	}

	// ===========================================================
	// Other
	// ===========================================================

}
