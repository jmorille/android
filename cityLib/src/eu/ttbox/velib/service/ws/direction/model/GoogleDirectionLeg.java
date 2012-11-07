package eu.ttbox.velib.service.ws.direction.model;

import java.util.ArrayList;

public class GoogleDirectionLeg {

	public long distanceInM;
	public String distanceText;
	public long durationInS;
	public String durationText;

	public ArrayList<GoogleDirectionStep> steps = new ArrayList<GoogleDirectionStep>();
	public double[] latLngStart;
	public double[] latLngEnd;
	public String addressStart;
	public String addressEnd;

	public void addStep(GoogleDirectionStep step) {
		steps.add(step);
	}

}
