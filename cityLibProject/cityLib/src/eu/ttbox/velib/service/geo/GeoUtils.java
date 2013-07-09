package eu.ttbox.velib.service.geo;

import java.util.ArrayList;

import org.osmdroid.util.GeoPoint;

import android.util.Log;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.model.geo.LatLngE6Provider;
import eu.ttbox.velib.model.geo.LatLngProvider;

public class GeoUtils {

	private static final String TAG = GeoUtils.class.getSimpleName();

	public static boolean isRedefineBox(double[] orginalBox, double[] redefineBox) {
		boolean isRedifineBoundy = false;
		if (orginalBox != null) {
			if (redefineBox != null) {
				for (int i = 0; i < 4; i++) {
					if (!isRedifineBoundy) {
						isRedifineBoundy = (orginalBox[i] != redefineBox[i]);
					}
				}
			}
		} else {
			isRedifineBoundy = true;
		}
		return isRedifineBoundy;
	}

	public static int[] getBoundybox2BoundyboxE6(double[] boundyBox) {
		int boundyBoxSize = boundyBox.length;
		int[] boundyBoxE6 = new int[boundyBoxSize];
		for (int i = 0; i < boundyBoxSize; i++) {
			boundyBoxE6[i] = (int) (boundyBox[i] * AppConstants.E6);
		}
		return boundyBoxE6;
	}

	public static double[] getBoundyBox(ArrayList<? extends LatLngProvider> stations) {
		double minLatitude = Double.MAX_VALUE;
		double minLongitude = Double.MAX_VALUE;

		double maxLatitude = Double.MIN_VALUE;
		double maxLongitude = Double.MIN_VALUE;
		boolean isFirst = true;
		for (LatLngProvider station : stations) {
			double lat = station.getLatitude();
			double lng = station.getLongitude();
			if (isFirst) {
				isFirst = false;
				minLatitude = maxLatitude = lat;
				minLongitude = maxLongitude = lng;
			}
			minLatitude = Math.min(minLatitude, lat);
			minLongitude = Math.min(minLongitude, lng);
			maxLatitude = Math.max(maxLatitude, lat);
			maxLongitude = Math.max(maxLongitude, lng);
		}
		return new double[] { minLatitude, minLongitude, maxLatitude, maxLongitude };
	}

	public static double[] getBoundyBoxToBoundyBoxE6(double[] boundyBox) {
		double[] boundyboxE6 = null;
		if (boundyBox != null) {
			int bdSize = boundyBox.length;
			if (bdSize > 0) {
				boundyboxE6 = new double[bdSize];
				for (int i = 0; i < bdSize; i++) {
					boundyboxE6[i] = boundyBox[i] * AppConstants.E6;
				}
			}
		}
		return boundyboxE6;
	}

	public static double[] getBoundyBoxE6(ArrayList<? extends LatLngE6Provider> stations, double[] excludeBoundyBoxE6) {
		double minLatitude = Double.MAX_VALUE;
		double minLongitude = Double.MAX_VALUE;

		double maxLatitude = Double.MIN_VALUE;
		double maxLongitude = Double.MIN_VALUE;
		boolean isFirst = true;
		for (LatLngE6Provider station : stations) {
			double lat = station.getLatitudeE6();
			double lng = station.getLongitudeE6();
			if (isValidLatLngForBoundyBox(lat, lng, excludeBoundyBoxE6)) {
				if (isFirst) {
					isFirst = false;
					minLatitude = maxLatitude = lat;
					minLongitude = maxLongitude = lng;
				}
				minLatitude = Math.min(minLatitude, lat);
				minLongitude = Math.min(minLongitude, lng);
				maxLatitude = Math.max(maxLatitude, lat);
				maxLongitude = Math.max(maxLongitude, lng);
			} else {
				Log.w(TAG, String.format("Exclude coord (%s, %s) for LatLngE6Provider %s", lat, lng, station));
			}
		}
		return new double[] { minLatitude, minLongitude, maxLatitude, maxLongitude };
	}

	public static ArrayList<? extends LatLngE6Provider> getLatLngE6ProviderNotInBoundyBoxE6(ArrayList<? extends LatLngE6Provider> stations,
			double[] excludeBoundyBoxE6) {
		ArrayList<LatLngE6Provider> result = new ArrayList<LatLngE6Provider>();
		for (LatLngE6Provider station : stations) {
			double lat = station.getLatitudeE6();
			double lng = station.getLongitudeE6();
			if (!isValidLatLngForBoundyBox(lat, lng, excludeBoundyBoxE6)) {
				result.add(station);
				Log.w(TAG, String.format("Exclude coord (%s, %s) for LatLngE6Provider : %s", lat, lng, station));
			}
		}
		return result;
	}

	public static boolean isValidLatLngForBoundyBox(double lat, double lng, double[] excludeBoundyBox) {
		boolean isValid = true;
		if (excludeBoundyBox != null) {
			int excludeBoundyBoxSize = excludeBoundyBox.length;
			isValid = isValid & excludeBoundyBoxSize > 0 ? lat > excludeBoundyBox[0] : isValid;
			isValid = isValid & excludeBoundyBoxSize > 1 ? lng > excludeBoundyBox[1] : isValid;
			isValid = isValid & excludeBoundyBoxSize > 3 ? lat < excludeBoundyBox[2] : isValid;
			isValid = isValid & excludeBoundyBoxSize > 4 ? lng < excludeBoundyBox[3] : isValid;
		}
		return isValid;
	}

	public static boolean isGeoPointInBoundyBox(double[] boundyBoxE6, GeoPoint testedGeoPoint) {
		if (testedGeoPoint == null) {
			return false;
		}
		return isGeoPointInBoundyBox(boundyBoxE6, testedGeoPoint.getLatitudeE6(), testedGeoPoint.getLongitudeE6());
	}

	public static boolean isGeoPointInBoundyBox(double[] boundyBoxE6, double testedLatE6, double testedLngE6) {
		if (boundyBoxE6 == null || boundyBoxE6.length < 4) {
			return false;
		}
		double minLatE6 = boundyBoxE6[0];
		double minLngE6 = boundyBoxE6[1];
		double maxLatE6 = boundyBoxE6[2];
		double maxLngE6 = boundyBoxE6[3];
		return (testedLatE6 >= minLatE6) && (testedLngE6 >= minLngE6) && (testedLatE6 <= maxLatE6) && (testedLngE6 <= maxLngE6);
	}

}
