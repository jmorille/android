package eu.ttbox.velib.model;

import java.util.Date;

import org.osmdroid.util.GeoPoint;

import android.location.Location;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.model.geo.GeoPointProvider;
import eu.ttbox.velib.model.geo.LatLngE6Provider;
import eu.ttbox.velib.model.geo.LatLngProvider;

public class Station implements GeoPointProvider, LatLngE6Provider, LatLngProvider {

	private final static double E6 = AppConstants.E6;

	// Velo Data
	int id = AppConstants.UNSET_ID;
	int provider;
	String number;// ="901"
	String name;// ="00901 - STATION MOBILE 1"
	String nameAlias;// ="00901 - STATION MOBILE 1"
	String address;// ="ALLEE DU BELVEDERE PARIS 19 - 0 75000 Paris -"
	String fullAddress;// ="ALLEE DU BELVEDERE PARIS 19 - 0 75000 Paris - 75000 PARIS"
	int latitudeE6;// ="48.892745582406675";
	int longitudeE6;// ="2.391255159886939";
	boolean open;// ="1";//
	boolean bonus;// ="0"

	// Config
	boolean favory = false;
	private FavoriteIconEnum favoriteType;

	// Dispos
	private int veloTotal = -1;
	private int stationCycle = -1;
	private int stationParking = -1;
	private int veloTicket = -1;
	private long veloUpdated = -1;

	// Dispos delta transient
	private int stationCycleDelta = -1;
	private int stationParkingDelta = -1;
	private int veloTicketDelta = -1;

	// Cache Value transient
	private GeoPoint cachedGeoPoint;
	private VelibProvider cachedVeloProvider;
	private Date cachedVeloUpdatedDate;

	// Runtile value transient
	boolean askRefreshDispo = false;

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
		return latitudeE6 / E6;
	}

	public void setLatitude(double lat) {
		this.latitudeE6 = (int) (lat * E6);
		this.cachedGeoPoint = null;
	}

	@Override
	public double getLongitude() {
		return longitudeE6 / E6;
	}

	public void setLongitude(double lng) {
		this.longitudeE6 = (int) (lng * E6);
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

	public void setName(String name) {
		this.name = name;
	}

	public VelibProvider getVeloProvider() {
		if (cachedVeloProvider == null) {
			cachedVeloProvider = VelibProvider.getVelibProvider(provider);
		}
		return cachedVeloProvider;
	}

	public int getProvider() {
		return provider;
	}

	public void setProvider(int provider) {
		this.provider = provider;
		this.cachedVeloProvider = null;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getFullAddress() {
		return fullAddress;
	}

	public void setFullAddress(String fullAddress) {
		this.fullAddress = fullAddress;
	}

	public boolean getOpen() {
		return open;
	}

	public void setOpen(boolean open) {
		this.open = open;
	}

	public boolean getBonus() {
		return bonus;
	}

	public void setBonus(boolean bonus) {
		this.bonus = bonus;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVeloTotal() {
		return veloTotal;
	}

	public void setVeloTotal(int veloTotal) {
		this.veloTotal = veloTotal;
	}

	public int getStationCycle() {
		return stationCycle;
	}

	public void setStationCycle(int cycle) {
		this.stationCycleDelta = this.stationCycle - cycle;
		this.stationCycle = cycle;
	}

	public int getStationParking() {
		return stationParking;
	}

	public void setStationParking(int parking) {
		this.stationParkingDelta = this.stationParking - parking;
		this.stationParking = parking;
	}

	public int getVeloTicket() {
		return veloTicket;
	}

	public void setVeloTicket(int veloTicket) {
		this.veloTicketDelta = this.veloTicket - veloTicket;
		this.veloTicket = veloTicket;
	}

	public long getVeloUpdated() {
		return veloUpdated;
	}

	public Date getVeloUpdatedDate() {
		if (cachedVeloUpdatedDate == null) {
			cachedVeloUpdatedDate = new Date(this.veloUpdated);
		}
		return cachedVeloUpdatedDate;
	}

	public void setVeloUpdated(long veloUpdated) {
		this.veloUpdated = veloUpdated;
		this.cachedVeloUpdatedDate = null;
	}

	public boolean isFavory() {
		return favory;
	}

	public void setFavory(boolean favory) {
		this.favory = favory;
	}

	public boolean isAskRefreshDispo() {
		return askRefreshDispo;
	}

	public void setAskRefreshDispo(boolean askRefresh) {
		this.askRefreshDispo = askRefresh;
	}

	public int getStationCycleDelta() {
		return stationCycleDelta;
	}

	public int getStationParkingDelta() {
		return stationParkingDelta;
	}

	public int getVeloTicketDelta() {
		return veloTicketDelta;
	}

	public FavoriteIconEnum getFavoriteType() {
		return favoriteType;
	}

	public void setFavoriteType(FavoriteIconEnum favoriteType) {
		this.favoriteType = favoriteType;
	}

	public void setFavoriteType(int favoriteTypeId) {
		if (favoriteTypeId > 0) {
			favoriteType = FavoriteIconEnum.values()[favoriteTypeId];
		}
	}

	public void setFavoriteType(String favoriteTypeId) {
		if (favoriteTypeId != null && favoriteTypeId.length() > 0) {
			favoriteType = FavoriteIconEnum.getFromName(favoriteTypeId);
		}
	}

	public String getFavoriteTypeId() {
		String favId = null;
		if (this.favoriteType != null && this.favoriteType != FavoriteIconEnum.DEFAULT_ICON) {
			favId = favoriteType.name();
		}
		return favId;
	}

	public String getNameAlias() {
		return nameAlias;
	}

	public void setNameAlias(String nameAlias) {
		this.nameAlias = nameAlias;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Station other = (Station) obj;
		if (id != other.id)
			return false;
		return true;
	}

	public float[] getDistance(Location loc) {
		if (loc != null) {
			float[] results = new float[3];
			Location.distanceBetween(loc.getLatitude(), loc.getLongitude(), getLatitude(), getLongitude(), results);
			return results;
		}
		return null;
	}

}
