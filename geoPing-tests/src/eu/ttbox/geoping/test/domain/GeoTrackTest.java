package eu.ttbox.geoping.test.domain;

import org.osmdroid.util.GeoPoint;

import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.domain.GeoTrack;

public class GeoTrackTest extends AndroidTestCase {

	public static final String TAG = "GeoTrackTest";
	
	 public void testAsGeoPoint() {
		 GeoTrack geoTrack = new GeoTrack();
		 int latE6 = 48917589;
		 int lngE6 = 2352167;
		 geoTrack.setLatitudeE6(latE6).setLongitudeE6(lngE6);
		 assertEquals(latE6, geoTrack.getLatitudeE6());
		 assertEquals(lngE6, geoTrack.getLongitudeE6());
		 // Wsg84
		 assertEquals(48.917589d, geoTrack.getLatitude());
		 assertEquals( 2.352167d, geoTrack.getLongitude());
		 // GeoPoint
		 GeoPoint geoPoint = geoTrack.asGeoPoint();
		 Log.d(TAG, String.format("GeoPoint(%s, %s)", geoPoint.getLatitudeE6(), geoPoint.getLongitudeE6()));
		 assertEquals(latE6, geoPoint.getLatitudeE6());
		 assertEquals(lngE6, geoPoint.getLongitudeE6());
	 }
}
