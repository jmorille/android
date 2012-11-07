package eu.ttbox.velib.service.filter;

import java.util.ArrayList;

import eu.ttbox.velib.map.geo.BoundingE6Box;
import eu.ttbox.velib.model.Station;

/**
 * @see http://www.synchrosinteractive.com/blog/1-software/38-geohash
 * @author deostem
 * 
 */
public class ProximityFilter {

	public static ArrayList<Station> filter(ArrayList<Station> allStations, BoundingE6Box boundyBox) {
		ArrayList<Station> result = new ArrayList<Station>(allStations.size());
		for (Station station : allStations) {
			boolean isInBox = boundyBox.containsE6(station.getLatitudeE6(), station.getLongitudeE6());
			if (isInBox) {
				result.add(station);
			}
		}
		return result;
	}
}
