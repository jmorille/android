package eu.ttbox.smstraker.domain;

import java.util.Date;

import android.location.Location;

import com.google.android.maps.GeoPoint;

import eu.ttbox.smstraker.core.AppConstant;

public class GeoTrack {

    public long id = -1;
    public String userId;
    public String provider;
    public long time;
    private int latitudeE6;
    private int longitudeE6;

    public double altitude;
    public float accuracy;
    public float bearing;
    public float speed;

    public String titre;

    private GeoPoint cachedGeoPoint;

    public GeoTrack() {
    }

    public GeoTrack(String userId, Location loc) {
        this.userId = userId;
        this.provider = loc.getProvider();
        this.time = loc.getTime();
        this.latitudeE6 = (int) (loc.getLatitude() / AppConstant.E6);
        this.longitudeE6 = (int) (loc.getLongitude() / AppConstant.E6);
        this.accuracy = loc.getAccuracy();
        if (loc.hasAltitude()) {
            this.altitude = loc.getAltitude();
        }
        if (loc.hasBearing()) {
            bearing = loc.getBearing();
        }
        if (loc.hasSpeed()) {
            this.speed = loc.getSpeed();
        }
    }

    public Location asLocation() {
        Location loc = new Location(provider);
        loc.setTime(time);
        loc.setLatitude(latitudeE6 * AppConstant.E6);
        loc.setLongitude(longitudeE6 * AppConstant.E6);
        loc.setAccuracy(accuracy);

        loc.setAltitude(altitude);
        loc.setBearing(bearing);
        loc.setSpeed(speed);

        return loc;
    }

    public GeoPoint asGeoPoint() {
        if (cachedGeoPoint == null) {
            GeoPoint point = new GeoPoint(latitudeE6, longitudeE6);
            cachedGeoPoint = point;
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
        return latitudeE6 / AppConstant.E6;
    }

    public GeoTrack setLatitude(double latitude) {
        this.latitudeE6 = (int) (latitude * AppConstant.E6);
        cachedGeoPoint = null;
        return this;
    }

    public double getLongitude() {
        return longitudeE6 / AppConstant.E6;
    }

    public GeoTrack setLongitude(double longitude) {
        this.longitudeE6 = (int) (longitude * AppConstant.E6);
        cachedGeoPoint = null;
        return this;
    }

    public int getLatitudeE6() {
        return latitudeE6;
    }

    public GeoTrack setLatitudeE6(int latitudeE6) {
        this.latitudeE6 = latitudeE6;
        cachedGeoPoint = null;
        return this;
    }

    public int getLongitudeE6() {
        return longitudeE6;
    }

    public GeoTrack setLongitudeE6(int longitudeE6) {
        this.longitudeE6 = longitudeE6;
        cachedGeoPoint = null;
        return this;
    }

    public double getAltitude() {
        return altitude;
    }

    public GeoTrack setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public GeoTrack setAccuracy(float accuracy) {
        this.accuracy = accuracy;
        return this;
    }

    public float getBearing() {
        return bearing;
    }

    public GeoTrack setBearing(float bearing) {
        this.bearing = bearing;
        return this;
    }

    public float getSpeed() {
        return speed;
    }

    public GeoTrack setSpeed(float speed) {
        this.speed = speed;
        return this;
    }

    public String getTitre() {
        return titre;
    }

    public GeoTrack setTitre(String titre) {
        this.titre = titre;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public GeoTrack setUserId(String userId) {
        this.userId = userId;
        return this;
    }

}
