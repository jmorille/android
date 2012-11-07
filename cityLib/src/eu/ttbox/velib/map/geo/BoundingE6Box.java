package eu.ttbox.velib.map.geo;

import java.io.Serializable;
import java.util.ArrayList;

import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import android.graphics.Rect;
import eu.ttbox.velib.model.geo.GeoPointProvider;

public class BoundingE6Box<E extends GeoPointProvider> implements Serializable {

	private static final long serialVersionUID = -1L;

	private ArrayList<E> stations;

	private int minLatE6;
	private int maxLatE6;
	private int minLngE6;
	private int maxLngE6;

	private long lastBoxUpdated;
	private long lastStationsUpdated;

	// Compute values
	private ArrayList<E> boundyBoxStations = new ArrayList<E>();
	private ArrayList<E> favoriteStations = new ArrayList<E>();

	public BoundingE6Box(ArrayList<E> stations) {
		super();
		this.stations = stations;
	}

	// public BoundingE6Box(GeoPoint p1, GeoPoint p2) {
	// super();
	// updateBoundingE6Box(p1, p2, System.currentTimeMillis());
	// }

	private Rect outRect = new Rect();

	public void updateBoundingE6Box(MapView mapView, long nowInMs) {
		mapView.getScreenRect(outRect); 
		BoundingBoxE6 boundyE6= mapView.getBoundingBox();
		updateBoundingE6Box(boundyE6, nowInMs);
		
// 		Projection projection = mapView.getProjection();
//		GeoPoint p1 = (GeoPoint)projection.fromPixels(outRect.left, outRect.top);
//		GeoPoint p2 =  (GeoPoint)projection.fromPixels(outRect.right, outRect.bottom);
//		updateBoundingE6Box(p1, p2, nowInMs);
	}

	public void updateBoundingE6Box(BoundingBoxE6 boxE6, long nowInMs) {
		int minLatE6 = boxE6.getLatSouthE6();
		int maxLatE6 =  boxE6.getLatNorthE6();
		// Lng
		int minLngE6 =   boxE6.getLonWestE6(); 
		int maxLngE6 = boxE6.getLonEastE6();
		updateBoundingE6Box(  minLatE6,   minLngE6,   maxLatE6,   maxLngE6,   nowInMs) ;
	}
	
	public void updateBoundingE6Box(GeoPoint p1, GeoPoint p2, long nowInMs) {
		// Lat
		int minLatE6 = Math.min(p1.getLatitudeE6(), p2.getLatitudeE6());
		int maxLatE6 = Math.max(p1.getLatitudeE6(), p2.getLatitudeE6());
		// Lng
		int minLngE6 = Math.min(p1.getLongitudeE6(), p2.getLongitudeE6());
		int maxLngE6 = Math.max(p1.getLongitudeE6(), p2.getLongitudeE6());
		  updateBoundingE6Box(  minLatE6,   minLngE6,   maxLatE6,   maxLngE6,   nowInMs) ;
	}
		
		
	public void updateBoundingE6Box(int minLatE6, int minLngE6, int maxLatE6, int maxLngE6, long nowInMs) {
		// Define values
		boolean needRefilter = false;
		if (!isSameBox(minLatE6, maxLatE6, minLngE6, maxLngE6)) {
			lastBoxUpdated = nowInMs;
			lastStationsUpdated = 0;
			needRefilter = true;
		}
		// Update The Boudy Boyx
		this.minLatE6 = minLatE6;
		this.maxLatE6 = maxLatE6;
		this.minLngE6 = minLngE6;
		this.maxLngE6 = maxLngE6;
		if (needRefilter) {
			filterStationInBoundyBox();
		}
	}

	// public boolean checkForRefresh(long checkDeltaInMs, long nowInMs) {
	// if ((lastStationsUpdated + checkDeltaInMs) < nowInMs) {
	// lastStationsUpdated = nowInMs;
	// return true;
	// }
	// return false;
	// }

	public boolean isBoundyBoxFix(long checkDeltaInMs, long nowInMs) {
		if ((lastBoxUpdated + checkDeltaInMs) < nowInMs) {
			lastStationsUpdated = nowInMs;
			return true;
		}
		return false;
	}

	private void filterStationInBoundyBox() {
		boundyBoxStations.clear();
		favoriteStations.clear();
		for (E station : stations) {
			// Boundy
			boolean isInBoundyBox = contains(station.asGeoPoint());
			 
			if (isInBoundyBox) {
				boundyBoxStations.add(station);
			}
			// Favorite
			if (station.isFavory()) {
				favoriteStations.add(station);
			}
		}
		// Sort by center
		if (false) {
			int centerLat = minLatE6 + ((maxLatE6 - minLatE6) / 2);
			int centerLng = minLngE6 + ((maxLngE6 - minLngE6) / 2);
			// Collections.sort(boundyBoxStations, new StationDistanceComparator());
		}
	}

	public ArrayList<E> getBoundyBoxStations() {
		return boundyBoxStations;
	}

	public ArrayList<E> getBoundyBoxFavoriteStations() {
		return boundyBoxStations;
	}

	public GeoPoint getUpperLeft() {
		return new GeoPoint((int) maxLatE6, (int) minLngE6);
	}

	public GeoPoint getLowerRight() {
		return new GeoPoint((int) minLatE6, (int) maxLngE6);
	}

	public boolean contains(GeoPoint point) {
		return (point.getLatitudeE6() >= this.minLatE6) && (point.getLongitudeE6() >= this.minLngE6) && (point.getLatitudeE6() <= this.maxLatE6)
				&& (point.getLongitudeE6() <= this.maxLngE6);
	}

	public boolean containsE6(double testedLatE6, double testedLngE6) {
		return (testedLatE6 >= this.minLatE6) && (testedLngE6 >= this.minLngE6) && (testedLatE6 <= this.maxLatE6) && (testedLngE6 <= this.maxLngE6);
	}

	public boolean intersects(BoundingE6Box other) {
		return !(other.minLngE6 > this.maxLngE6 || other.maxLngE6 < this.minLngE6 || other.minLatE6 > this.maxLatE6 || other.maxLatE6 < this.minLatE6);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(maxLatE6);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(maxLngE6);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minLatE6);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(minLngE6);
		result = prime * result + (int) (temp ^ (temp >>> 32));
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
		BoundingE6Box other = (BoundingE6Box) obj;
		if (Double.doubleToLongBits(maxLatE6) != Double.doubleToLongBits(other.maxLatE6))
			return false;
		if (Double.doubleToLongBits(maxLngE6) != Double.doubleToLongBits(other.maxLngE6))
			return false;
		if (Double.doubleToLongBits(minLatE6) != Double.doubleToLongBits(other.minLatE6))
			return false;
		if (Double.doubleToLongBits(minLngE6) != Double.doubleToLongBits(other.minLngE6))
			return false;
		return true;
	}

	private boolean isSameBox(double minLatE6, double maxLatE6, double minLngE6, double maxLngE6) {
		if (Double.doubleToLongBits(this.maxLatE6) != Double.doubleToLongBits(maxLatE6))
			return false;
		if (Double.doubleToLongBits(this.maxLngE6) != Double.doubleToLongBits(maxLngE6))
			return false;
		if (Double.doubleToLongBits(this.minLatE6) != Double.doubleToLongBits(minLatE6))
			return false;
		if (Double.doubleToLongBits(this.minLngE6) != Double.doubleToLongBits(minLngE6))
			return false;
		return true;
	}
	
	private boolean isSameBox(int minLatE6, int maxLatE6, int minLngE6, int maxLngE6) {
		if ( this.maxLatE6  !=  maxLatE6 )
			return false;
		if ( this.maxLngE6  !=  maxLngE6 )
			return false;
		if ( this.minLatE6  !=  minLatE6 )
			return false;
		if ( this.minLngE6  !=  minLngE6 )
			return false;
		return true;
	}

	public long getLastBoxUpdated() {
		return lastBoxUpdated;
	}

}
