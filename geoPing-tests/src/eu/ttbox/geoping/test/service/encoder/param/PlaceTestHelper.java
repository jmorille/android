package eu.ttbox.geoping.test.service.encoder.param;

import java.util.Calendar;

import android.location.Location;
import eu.ttbox.geoping.domain.model.GeoTrack;

public class PlaceTestHelper {

    public static final String PROVIDER_NETWORK = "network";
    public static final String PROVIDER_GPS = "gps";
    
    public enum WorldGeoPoint {
        SYNDNEY("-33.856111,151.1925"), //
        TOKYO("35.670724,139.771907"), //
        PARIS("48.856578,2.351828"), //
        NEW_YORK("40.713361,-74.005594"); //

        public final String latLng;

        WorldGeoPoint(String coord) {
            this.latLng = coord;
        }

    }
 
    private static void setLatLng(Location loc, WorldGeoPoint geoPoint) {
        String latLng = geoPoint.latLng;
        int idx = latLng.indexOf(',');
        String latString = latLng.substring(0, idx);
        String lngString = latLng.substring(idx + 1, latLng.length());
        Double lat = Double.valueOf(latString);
        Double lng = Double.valueOf(lngString);
        loc.setLatitude(lat.doubleValue());
        loc.setLongitude(lng.doubleValue());
    }

    public static GeoTrack getMessageLoc(String provider, WorldGeoPoint geoPoint) {
        Location loc = new Location(provider);
        loc.setTime(getDate( System.currentTimeMillis()  ));
        setLatLng(loc, geoPoint); 
        loc.setAccuracy(120.258446418974f);
        if (PROVIDER_GPS.equals(provider)) {
            loc.setAltitude(124.6546533464d);
            loc.setBearing(257.16416464646446464646413f);
            loc.setSpeed(125.1464646464468946444646f);
            loc.setAccuracy(20.258446418974f);
        }
        GeoTrack geo = new GeoTrack(null, loc);
        return geo;
    }

    public static long getDate(long now) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        cal.clear(Calendar.MILLISECOND);
        return cal.getTimeInMillis();
    }

}
