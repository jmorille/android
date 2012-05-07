package eu.ttbox.smstraker.domain;

import java.util.Date;

import com.google.android.maps.GeoPoint;

import eu.ttbox.smstraker.core.AppConstant;

import android.location.Location;

public class TrackPoint {

	private int id;
	private String userId;
	private String provider;
	private long time;
	private int latitudeE6;
	private int longitudeE6;

	private double altitude;
	private float accuracy;
	private float bearing;
	private float speed;

	private String titre;

	private GeoPoint cachedGeoPoint;

	public TrackPoint() {
	}

	public TrackPoint(String userId, Location loc) {
		this.userId = userId;
		this.provider = loc.getProvider();
		this.time = loc.getTime();
		this.latitudeE6 = (int)(loc.getLatitude()/AppConstant.E6);
		this.longitudeE6 = (int)(loc.getLongitude()/AppConstant.E6);
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
		loc.setLatitude(latitudeE6*AppConstant.E6);
		loc.setLongitude(longitudeE6*AppConstant.E6);
		loc.setAccuracy(accuracy);

		loc.setAltitude(altitude);
		loc.setBearing(bearing);
		loc.setSpeed(speed);

		return loc;
	}

	public GeoPoint asGeoPoint() {
		if (cachedGeoPoint == null) {
			GeoPoint point = new GeoPoint(latitudeE6,longitudeE6);
			cachedGeoPoint = point;
		}
		return cachedGeoPoint;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public long getTime() {
		return time;
	}

	public Date getTimeAsDate() {
		Date timeAsDate = new Date(time);
		return timeAsDate;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getLatitude() {
		return latitudeE6/AppConstant.E6;
	}

	public void setLatitude(double latitude) {
		this.latitudeE6 = (int)(latitude*AppConstant.E6);
		cachedGeoPoint = null;
	}

	public double getLongitude() {
		return longitudeE6/AppConstant.E6;
	}

	public void setLongitude(double longitude) {
		this.longitudeE6 = (int)(longitude*AppConstant.E6);
		cachedGeoPoint = null;
	} 
	

	public int getLatitudeE6() {
		return latitudeE6;
	}

	public void setLatitudeE6(int latitudeE6) {
		this.latitudeE6 = latitudeE6;
		cachedGeoPoint = null;
	}

	public int getLongitudeE6() {
		return longitudeE6;
	}

	public void setLongitudeE6(int longitudeE6) {
		this.longitudeE6 = longitudeE6;
		cachedGeoPoint = null;
 	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public float getAccuracy() {
		return accuracy;
	}

	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}

	public float getBearing() {
		return bearing;
	}

	public void setBearing(float bearing) {
		this.bearing = bearing;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public String getTitre() {
		return titre;
	}

	public void setTitre(String titre) {
		this.titre = titre;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}
