package eu.ttbox.geoping.domain;

import java.util.Date;

import org.osmdroid.util.GeoPoint;

import android.location.Location;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.cache.ZoomLevelComputeCache;

public class GeoTrack {

    public long id = -1l;
    public String phone;
    public String provider;
    public long time = -1l;
    private int latitudeE6;
    private int longitudeE6;
    public String address;

    private boolean hasLatitude;
    private boolean hasLlongitude;

    // Optionnal
    public int altitude;
    public int accuracy;
    public int bearing;
    public int speed;

    private boolean hasAltitude;
    private boolean hasAccuracy;
    private boolean hasBearing;
    private boolean hasSpeed;

    // Other
    public String titre;

    private GeoPoint cachedGeoPoint;
    private transient ZoomLevelComputeCache cachedZoomLevelComputeCache;

    public GeoTrack() {
    }

    public GeoTrack(String phone, Location loc) {
        this.phone = phone;
        this.provider = loc.getProvider();
        this.time = loc.getTime();
        this.latitudeE6 = (int) (loc.getLatitude() * AppConstants.E6);
        this.longitudeE6 = (int) (loc.getLongitude() * AppConstants.E6);
        this.accuracy = (int) loc.getAccuracy();
        if (loc.hasAltitude()) {
            this.altitude = (int) loc.getAltitude();
        }
        if (loc.hasBearing()) {
            bearing = (int) loc.getBearing();
        }
        if (loc.hasSpeed()) {
            this.speed = (int) loc.getSpeed();
        }
    }

    public Location asLocation() {
        Location loc = new Location(provider);
        loc.setTime(time);
        loc.setLatitude(latitudeE6 / AppConstants.E6);
        loc.setLongitude(longitudeE6 / AppConstants.E6);
        loc.setAccuracy(accuracy);

        loc.setAltitude(altitude);
        loc.setBearing(bearing);
        loc.setSpeed(speed);

        return loc;
    }

    public GeoPoint asGeoPoint() {
        if (cachedGeoPoint == null) {
            cachedGeoPoint = new GeoPoint(latitudeE6, longitudeE6, altitude);
        }
        return cachedGeoPoint;
    }

    public long getId() {
        return id;
    }

    public GeoTrack setId(long id) {
        this.id = id;
        return this;
    }

    public String getProvider() {
        return provider;
    }

    public GeoTrack setProvider(String provider) {
        this.provider = provider;
        return this;
    }

    public long getTime() {
        return time;
    }

    public Date getTimeAsDate() {
        Date timeAsDate = new Date(time);
        return timeAsDate;
    }

    public GeoTrack setTime(long time) {
        this.time = time;
        return this;
    }

    public double getLatitude() {
        return latitudeE6 / AppConstants.E6;
    }

    public boolean hasLatitude() {
        return hasLatitude;
    }

    public GeoTrack setLatitude(double latitude) {
        return setLatitudeE6((int) (latitude * AppConstants.E6));
    }

    public GeoTrack setLatitudeE6(int latitudeE6) {
        this.latitudeE6 = latitudeE6;
        this.hasLatitude = true;
        clearLatLngCache();
        return this;
    }

    public double getLongitude() {
        return longitudeE6 / AppConstants.E6;
    }

    public boolean hasLongitude() {
        return hasLlongitude;
    }

    public GeoTrack setLongitude(double longitude) {
        return setLongitudeE6((int) (longitude * AppConstants.E6));
    }

    public GeoTrack setLongitudeE6(int longitudeE6) {
        this.longitudeE6 = longitudeE6;
        this.hasLlongitude = true;
        clearLatLngCache();
        return this;
    }

    public int getLatitudeE6() {
        return latitudeE6;
    }

    public int getLongitudeE6() {
        return longitudeE6;
    }

    public boolean hasAltitude() {
        return this.altitude != -1;
    }

    public int getAltitude() {
        return altitude;
    }

    public GeoTrack setAltitude(double altitude) {
        this.altitude = (int) altitude;
        return this;
    }

    public boolean hasAccuracy() {
        return this.accuracy != -1;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public GeoTrack setAccuracy(int accuracy) {
        this.accuracy = accuracy;
        return this;
    }

    public boolean hasBearing() {
        return this.bearing != 1;
    }

    public int getBearing() {
        return bearing;
    }

    public GeoTrack setBearing(int bearing) {
        this.bearing = bearing;
        return this;
    }

    public int getSpeed() {
        return speed;
    }

    public GeoTrack setSpeed(int speed) {
        this.speed = speed;
        return this;
    }

    public boolean hasSpeed() {
        return this.speed != -1;
    }

    public String getTitre() {
        return titre;
    }

    public GeoTrack setTitre(String titre) {
        this.titre = titre;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public GeoTrack setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    // Business
    private void clearLatLngCache() {
        cachedGeoPoint = null;
        cachedZoomLevelComputeCache = null;
    }

    public float computeGroundResolutionInMForZoomLevel(int zoomLevel) {
        if (cachedZoomLevelComputeCache == null) {
            cachedZoomLevelComputeCache = new ZoomLevelComputeCache(getLatitude());
        }
        return cachedZoomLevelComputeCache.computeGroundResolutionInMForZoomLevel(zoomLevel);
    }

    // Override

    public GeoTrack setAddress(String address) {
        this.address = address;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append("GeoTrack [");
        sb.append("id=").append(id)//
                .append(", phone=").append(phone)//
                .append(", provider=").append(provider)//
                .append(", latitudeE6=").append(latitudeE6)//
                .append(", longitudeE6=").append(longitudeE6)//
                .append(", time=").append(time) //
                .append(", time=").append(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS,%1$tL", time));

        sb.append("]");
        return sb.toString();
    }

}
