package eu.ttbox.velib.model.geo;

import org.osmdroid.util.GeoPoint;

public interface GeoPointProvider {
	GeoPoint asGeoPoint();

	boolean isFavory();
}
