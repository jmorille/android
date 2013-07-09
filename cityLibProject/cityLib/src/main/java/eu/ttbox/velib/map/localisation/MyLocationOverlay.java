package eu.ttbox.velib.map.localisation;

import org.osmdroid.util.GeoPoint;

public interface MyLocationOverlay {

	GeoPoint getLastKnownLocationAsGeoPoint();

	boolean enableMyLocation();

	boolean enableCompass();

	boolean isGpsLocationProviderIsEnable();

	void disableCompass();

	void disableMyLocation();

	boolean runOnFirstFix(Runnable runnable);

	GeoPoint getMyLocation();

}
