package eu.ttbox.geoping.domain;

import java.util.Date;

import org.osmdroid.util.GeoPoint;

import android.location.Location;
import eu.ttbox.geoping.core.AppConstants;

public class GeoTrack {

	public long id = -1;
	public String userId;
	public String provider;
	public long time;
	private int latitudeE6;
	private int longitudeE6;

	public int altitude;
	public int accuracy;
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
		this.latitudeE6 = (int) (loc.getLatitude() * AppConstants.E6);
		this.longitudeE6 = (int) (loc.getLongitude() * AppConstants.E6);
		this.accuracy = (int)loc.getAccuracy();
		if (loc.hasAltitude()) {
			this.altitude = (int)loc.getAltitude();
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

	public GeoTrack setLatitude(double latitude) {
		this.latitudeE6 = (int) (latitude * AppConstants.E6);
		cachedGeoPoint = null;
		return this;
	}

	public double getLongitude() {
		return longitudeE6 / AppConstants.E6;
	}

	public GeoTrack setLongitude(double longitude) {
		this.longitudeE6 = (int) (longitude * AppConstants.E6);
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

    public boolean hasAltitude() { 
        return this.altitude != -1;
    }
    
	public int getAltitude() {
		return altitude;
	}

	public GeoTrack setAltitude(double altitude) {
		this.altitude = (int)altitude;
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

	public String getUserId() {
		return userId;
	}

	public GeoTrack setUserId(String userId) {
		this.userId = userId;
		return this;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder().append("GeoTrack [");
		sb.append("id=").append(id)//
				.append(", userId=").append(userId)//
				.append(", provider=").append(provider)//
				.append(", latitudeE6=").append(latitudeE6)//
				.append(", longitudeE6=").append(longitudeE6)//
				.append(", time=").append(time) //
				.append(", time=").append(String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS,%1$tL", time));

		sb.append("]");
		return sb.toString();
	}


}
