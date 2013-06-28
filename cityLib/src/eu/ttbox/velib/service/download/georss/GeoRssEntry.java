package eu.ttbox.velib.service.download.georss;

import org.osmdroid.util.GeoPoint;

import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.model.geo.GeoPointProvider;
import eu.ttbox.velib.model.geo.LatLngE6Provider;
import eu.ttbox.velib.model.geo.LatLngProvider;

public class GeoRssEntry implements GeoPointProvider, LatLngE6Provider, LatLngProvider {

	private final static double E6 = AppConstants.E6;

	private String entry;
	private String title;
	private String link;
	private String summary;
	private int latitudeE6;
	private int longitudeE6;
	private String category;

	// Cache Value transient
	private GeoPoint cachedGeoPoint;

	public GeoPoint asGeoPoint() {
		if (cachedGeoPoint == null) {
			GeoPoint point = new GeoPoint((int) latitudeE6, (int) longitudeE6);
			cachedGeoPoint = point;
		}
		return cachedGeoPoint;
	}

	@Override
	public double getLatitude() {
		return latitudeE6 / E6;
	}

	public void setLatitude(double lat) {
		this.latitudeE6 = (int)(lat * E6);
		this.cachedGeoPoint = null;
	}

	public void setLatitudeE6(int latitudeE6) {
		this.latitudeE6 = latitudeE6;
		this.cachedGeoPoint = null;
	}

	@Override
	public double getLongitude() {
		return longitudeE6 / E6;
	}

	public void setLongitude(double lng) {
		this.longitudeE6 = (int)(lng * E6);
		this.cachedGeoPoint = null;
	}

	public void setLongitudeE6(int longitudeE6) {
		this.longitudeE6 = longitudeE6;
		this.cachedGeoPoint = null;
	}

	@Override
	public int getLatitudeE6() {
		return this.latitudeE6;
	}

	@Override
	public int getLongitudeE6() {
		return this.longitudeE6;
	}

	@Override
	public boolean isFavory() {
		return false;
	}

	public String getEntry() {
		return entry;
	}

	public void setEntry(String entry) {
		this.entry = entry;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

}
