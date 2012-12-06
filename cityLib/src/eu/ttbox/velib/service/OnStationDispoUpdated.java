package eu.ttbox.velib.service;

import java.util.ArrayList;

import eu.ttbox.velib.model.Station;

/**
 * @see http://tseng-blog.nge-web.net/blog/2009/02/17/how-implement-your-own-listener-android-java/
 *  
 */
public interface OnStationDispoUpdated {

	void stationDispoUpdated(Station updatedStaion);

	void stationDispoUpdated(ArrayList<Station> station);

}
