package eu.ttbox.velib.service.filter.geo.query;

import java.util.List;

import eu.ttbox.velib.service.filter.geo.GeoHash;
import eu.ttbox.velib.service.filter.geo.WGS84Point;

public interface GeoHashQuery {

	/**
	 * check wether a geohash is within the hashes that make up this query.
	 */
	public boolean contains(GeoHash hash);

	/**
	 * returns whether a point lies within a query.
	 */
	public boolean contains(WGS84Point point);

	/**
	 * should return the hashes that re required to perform this search.
	 */
	public List<GeoHash> getSearchHashes();

	public String getWktBox();

}
