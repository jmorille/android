package eu.ttbox.velib.model;

import org.osmdroid.util.GeoPoint;

import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.model.geo.GeoPointProvider;
import eu.ttbox.velib.model.geo.LatLngE6Provider;
import eu.ttbox.velib.model.geo.LatLngProvider;

public class MyPoi implements GeoPointProvider, LatLngE6Provider, LatLngProvider  {

	// Velo Data
    int id;
	String name;
	int latitudeE6;// ="48.892745582406675";
	int longitudeE6;// ="2.391255159886939";
	
	boolean favory = false;
	private FavoriteIconEnum favoriteType;
	
	// Cache Value transient
	private GeoPoint cachedGeoPoint;
	

	
	// ===========================================================
    // Lat / Lng Accessor
    // ===========================================================
	
	public GeoPoint asGeoPoint() {
		if (cachedGeoPoint == null) {
			GeoPoint point = new GeoPoint((int) latitudeE6, (int) longitudeE6);
			cachedGeoPoint = point;
		}
		return cachedGeoPoint;
	}
	
	@Override
	public double getLatitude() {
		return latitudeE6 /  AppConstants.E6;
	}

	public void setLatitude(double lat) {
		this.latitudeE6 = (int) (lat *  AppConstants.E6);
		this.cachedGeoPoint = null;
	}
	
	@Override
	public double getLongitude() {
		return longitudeE6 / AppConstants.E6;
	}

	public void setLongitude(double lng) {
		this.longitudeE6 = (int) (lng * AppConstants.E6);
		this.cachedGeoPoint = null;
	}
	
	@Override
	public int getLatitudeE6() {
		return latitudeE6;
	}

	public void setLatitudeE6(int latitudeE6) {
		this.latitudeE6 = latitudeE6;
		this.cachedGeoPoint = null;	
	}
	
	@Override
	public int getLongitudeE6() {
		return longitudeE6;
	}

	public void setLongitudeE6(int longitudeE6) {
		this.longitudeE6 = longitudeE6;
		this.cachedGeoPoint = null;
	}
	// ===========================================================
    // Accessor
    // ===========================================================

	public String getName() {
		return name;
	}

	public MyPoi setName(String name) {
		this.name = name;
		return this;
	}

	public int getId() {
		return id;
	}

	public MyPoi setId(int id) {
		this.id = id;
		return this;
	}

	public boolean isFavory() {
		return favory;
	}

	public MyPoi setFavory(boolean favory) {
		this.favory = favory;
		return this;
	}

	public FavoriteIconEnum getFavoriteType() {
		return favoriteType;
	}

	public MyPoi setFavoriteType(FavoriteIconEnum favoriteType) {
		this.favoriteType = favoriteType;
		return this;
	}

	

	// ===========================================================
    // Other
    // ===========================================================

	
	

}
