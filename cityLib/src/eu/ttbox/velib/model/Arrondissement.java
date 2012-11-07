package eu.ttbox.velib.model;

import eu.ttbox.velib.core.AppConstants;

public class Arrondissement {

	String number;

	long updated;

	double minLatitude;
	double minLongitude;

	double maxLatitude;
	double maxLongitude;

	public Arrondissement() {
		super();
	}

	public Arrondissement(long updated) {
		super();
		this.updated = updated;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public double getMinLatitude() {
		return minLatitude;
	}

	public void setMinLatitude(double minLatitude) {
		this.minLatitude = minLatitude;
	}

	public double getMinLongitude() {
		return minLongitude;
	}

	public void setMinLongitude(double minLongitude) {
		this.minLongitude = minLongitude;
	}

	public double getMaxLatitude() {
		return maxLatitude;
	}

	public void setMaxLatitude(double maxLatitude) {
		this.maxLatitude = maxLatitude;
	}

	public double getMaxLongitude() {
		return maxLongitude;
	}

	public void setMaxLongitude(double maxLongitude) {
		this.maxLongitude = maxLongitude;
	}

	public long getUpdated() {
		return updated;
	}

	public void setUpdated(long updated) {
		this.updated = updated;
	}

	public double[] getBoundyBoxE6() {
		double[] boudyE6 = new double[4];
		boudyE6[0] = minLatitude * AppConstants.E6;
		boudyE6[1] = minLongitude * AppConstants.E6;
		boudyE6[2] = maxLatitude * AppConstants.E6;
		boudyE6[3] = maxLongitude * AppConstants.E6;
		return boudyE6;
	}

	public double[] getBoundyBox() {
		double[] boudyE6 = new double[4];
		boudyE6[0] = minLatitude;
		boudyE6[1] = minLongitude;
		boudyE6[2] = maxLatitude;
		boudyE6[3] = maxLongitude;
		return boudyE6;
	}
}
