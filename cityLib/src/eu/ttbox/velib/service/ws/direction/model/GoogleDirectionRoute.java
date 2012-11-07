package eu.ttbox.velib.service.ws.direction.model;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

public class GoogleDirectionRoute {

	public ArrayList<GoogleDirectionLeg> legs = new ArrayList<GoogleDirectionLeg>();

	public String summary;

	public String copyrights;

	public ArrayList<GeoPoint> polyline;

	public void addLegs(GoogleDirectionLeg leg) {
		legs.add(leg);
	}

}
