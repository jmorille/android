package eu.ttbox.velib.service.ws.direction.model;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

public class GoogleDirectionStep {

	public double[] latLngStart;
	public double[] latLngEnd;
	public String htmlInstructions;
	public long distanceInM;
	public String distanceText;
	public long durationInS;
	public String durationText;
	public ArrayList<GeoPoint> polyline;

}
