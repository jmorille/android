package eu.ttbox.velib.service.ws.direction.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import android.util.Log;
import eu.ttbox.velib.service.ws.direction.model.GoogleDirection;
import eu.ttbox.velib.service.ws.direction.model.GoogleDirectionLeg;
import eu.ttbox.velib.service.ws.direction.model.GoogleDirectionRoute;
import eu.ttbox.velib.service.ws.direction.model.GoogleDirectionStep;

/**
 * Api https://developers.google.com/maps/documentation/directions/?hl=fr-FR
 * 
 * sample request @see http://maps.googleapis.com/maps/api/directions/json?origin=41.8781,-87.62979&destination=34.052,-118.24&sensor=false
 *  
 * 
 */
public class GoogleDirectionParser {

	private final String TAG = GoogleDirectionParser.class.getSimpleName();

	private final String TAG_STATUS = "status";
	// private final String TAG_BOUNDYBOX = "bounds";

	private final String LAT = "lat";
	private final String LNG = "lng";
	private final String VALUE = "value"; // indicates the distance in meters
	private final String TEXT = "text"; // contains a human-readable representation of the distance, displayed in units as used at the origin (
	private final String POINTS = "points";

	private final String TAG_ROUTES = "routes";

	private final String TAG_ROUTES_SUMMARY = "summary";
	private final String TAG_ROUTES_LEGS = "legs";
	private final String TAG_ROUTES_WAYPOINT_ORDER = "waypoint_order";
	private final String TAG_ROUTES_BOUNDYBOX = "bounds";
	private final String TAG_ROUTES_BOUNDYBOX_UP = "northeast";
	private final String TAG_ROUTES_BOUNDYBOX_DOWN = "southwest";
	private final String TAG_ROUTES_COPYRIGHTS = "copyrights";
	private final String TAG_ROUTES_POLYLINE = "overview_polyline";

	private final String TAG_LEGS_STEPS = "steps";
	private final String TAG_LEGS_DISTANCE = "distance"; // indicates the total distance covered by this leg, as a field with the following elements:
	private final String TAG_LEGS_DURATION = "duration"; // indicates the total duration of this leg, as a field with the following elements:
	private final String TAG_LEGS_LOC_START = "start_location";
	private final String TAG_LEGS_LOC_END = "end_location";
	private final String TAG_LEGS_ADDR_START = "start_address";
	private final String TAG_LEGS_ADDR_END = "end_address";

	private final String TAG_STEPS_INST_HTML = "html_instructions";
	private final String TAG_STEPS_POLYLINE = "polyline";
	private final String TAG_STEPS_DISTANCE = "distance";
	private final String TAG_STEPS_DURATION = "duration";
	private final String TAG_STEPS_LOC_START = "start_location";
	private final String TAG_STEPS_LOC_END = "end_location";

	public GoogleDirection parseInputStream(InputStream in) throws IOException, JSONException {
		String httpContent = consumeInputStream(in);
		JSONObject jsonRoot = new JSONObject(httpContent);
		//
		GoogleDirection directions = null;
		String statusString = jsonRoot.getString(TAG_STATUS);
		if (statusString != null && GoogleDirectionStatusEnum.OK.name().equals(statusString)) {
			directions = new GoogleDirection();
			directions.status = GoogleDirectionStatusEnum.OK;
			JSONArray routes = jsonRoot.getJSONArray(TAG_ROUTES);
			for (int i = 0; i < routes.length(); i++) {
				JSONObject jsonRoute = routes.getJSONObject(i);
				GoogleDirectionRoute route = parseJsonRoute(jsonRoute);
				directions.addRoutes(route);
			}

		} else {
			Log.e(TAG, "Service status Error : " + statusString);
			return null;
		}

		return directions;
	}

	private double[] parseJsonLatLng(JSONObject jsonRoot) throws JSONException {
		double lat = jsonRoot.getDouble(LAT);
		double lng = jsonRoot.getDouble(LNG);
		return new double[] { lat, lng };
	}

	private GoogleDirectionRoute parseJsonRoute(JSONObject jsonRoot) throws JSONException {
		GoogleDirectionRoute route = new GoogleDirectionRoute();
		// Boundy Box
		JSONObject jsonBound = jsonRoot.getJSONObject(TAG_ROUTES_BOUNDYBOX);
		JSONObject jsonBoundUp = jsonBound.getJSONObject(TAG_ROUTES_BOUNDYBOX_UP);
		JSONObject jsonBoundDown = jsonBound.getJSONObject(TAG_ROUTES_BOUNDYBOX_DOWN);
		double[] latlngUp = parseJsonLatLng(jsonBoundUp);
		double[] latlngDown = parseJsonLatLng(jsonBoundDown);
		//
		route.summary = jsonRoot.getString(TAG_ROUTES_SUMMARY);
		route.copyrights = jsonRoot.getString(TAG_ROUTES_COPYRIGHTS);

		// Order

		// Warning

		// Legs
		JSONArray legs = jsonRoot.getJSONArray(TAG_ROUTES_LEGS);
		for (int i = 0; i < legs.length(); i++) {
			JSONObject jsonLeg = legs.getJSONObject(i);
			GoogleDirectionLeg leg = parseJsonLeg(jsonLeg);
			route.addLegs(leg);
		}
		// Polyline
		route.polyline = parseJsonPolyline(jsonRoot.getJSONObject(TAG_ROUTES_POLYLINE));

		return route;
	}

	private GoogleDirectionLeg parseJsonLeg(JSONObject jsonRoot) throws JSONException {
		GoogleDirectionLeg leg = new GoogleDirectionLeg();
		// Distance
		JSONObject jsonDistance = jsonRoot.getJSONObject(TAG_LEGS_DISTANCE);
		leg.distanceInM = jsonDistance.getLong(VALUE);
		leg.distanceText = jsonDistance.getString(TEXT);
		// Duration
		JSONObject jsonDuration = jsonRoot.getJSONObject(TAG_LEGS_DURATION);
		leg.durationInS = jsonDuration.getLong(VALUE);
		leg.durationText = jsonDuration.getString(TEXT);

		// Location
		JSONObject jsonLocStart = jsonRoot.getJSONObject(TAG_LEGS_LOC_START);
		JSONObject jsonLocEnd = jsonRoot.getJSONObject(TAG_LEGS_LOC_END);
		leg.latLngStart = parseJsonLatLng(jsonLocStart);
		leg.latLngEnd = parseJsonLatLng(jsonLocEnd);

		// Address
		leg.addressStart = jsonDistance.getString(TAG_LEGS_ADDR_START);
		leg.addressEnd = jsonDistance.getString(TAG_LEGS_ADDR_END);

		// Steps
		JSONArray steps = jsonRoot.getJSONArray(TAG_LEGS_STEPS);
		for (int i = 0; i < steps.length(); i++) {
			JSONObject jsonStep = steps.getJSONObject(i);
			GoogleDirectionStep step = parseJsonStep(jsonStep);
			leg.addStep(step);
		}
		// Result
		return leg;
	}

	private GoogleDirectionStep parseJsonStep(JSONObject jsonRoot) throws JSONException {
		GoogleDirectionStep step = new GoogleDirectionStep();
		// Location
		JSONObject jsonLocStart = jsonRoot.getJSONObject(TAG_STEPS_LOC_START);
		JSONObject jsonLocEnd = jsonRoot.getJSONObject(TAG_STEPS_LOC_END);
		step.latLngStart = parseJsonLatLng(jsonLocStart);
		step.latLngEnd = parseJsonLatLng(jsonLocEnd);
		// Distance
		JSONObject jsonDistance = jsonRoot.getJSONObject(TAG_STEPS_DISTANCE);
		step.distanceInM = jsonDistance.getLong(VALUE);
		step.distanceText = jsonDistance.getString(TEXT);
		// Duration
		JSONObject jsonDuration = jsonRoot.getJSONObject(TAG_STEPS_DURATION);
		step.durationInS = jsonDuration.getLong(VALUE);
		step.durationText = jsonDuration.getString(TEXT);
		// step
		step.htmlInstructions = jsonRoot.getString(TAG_STEPS_INST_HTML);
		// Polyline
		step.polyline = parseJsonPolyline(jsonRoot.getJSONObject(TAG_STEPS_POLYLINE));
		// / result
		return step;
	}

	private ArrayList<GeoPoint> parseJsonPolyline(JSONObject jsonRoot) throws JSONException {
		String points = jsonRoot.getString(POINTS);
		return decodingPoints(points);
	}

	/**
	 * @see http://blog.synyx.de/2010/06/routing-driving-directions-on-android-part-1-get-the-route/
	 * @param points
	 * @return
	 */
	private ArrayList<GeoPoint> decodingPoints(String points) {
		ArrayList<GeoPoint> poly = new ArrayList<GeoPoint>();
		String encoded = points; // points.replace("\\\\", "\\");
		int index = 0, len = encoded.length();
		int lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {

				b = encoded.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6), (int) (((double) lng / 1E5) * 1E6));
			poly.add(p);
		}
		return poly;
	}

	private String consumeInputStream(InputStream in) throws IOException {
		StringBuilder builder = new StringBuilder(2048);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		String line;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		return builder.toString();
	}
}
