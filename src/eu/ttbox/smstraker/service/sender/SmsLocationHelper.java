package eu.ttbox.smstraker.service.sender;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.location.Location;
import android.util.Log;
import eu.ttbox.smstraker.core.AppConstant;

public class SmsLocationHelper {

	private final static String MSGID = "smsTracker#";

	private final static String MSGKEY_PROVIDER = "p";
	private final static String MSGKEY_TIME = "t";
	private final static String MSGKEY_LATITUDE_E6 = "la";
	private final static String MSGKEY_LONGITUDE_E6 = "ln";
	private final static String MSGKEY_ALTITUDE = "al";
	private final static String MSGKEY_ACCURACY = "ac";
	private final static String MSGKEY_BEARING = "b";
	private final static String MSGKEY_SPEAD = "s";

	public static String toSmsMessage(Location location) {
		if (location != null) {
			String body = convertLocationAsJsonString(location);
			String msg = new StringBuffer(255).append( MSGID).append( body).toString();
			return msg;
		}
		return null;
	}

	public static Location fromSmsMessage(String smsMessage) {
		if (smsMessage != null && smsMessage.startsWith(MSGID)) {
			String body = smsMessage.substring(MSGID.length(), smsMessage.length());
			return convertJsonLocAsLocation(body);
		}
		return null;
	}

	public static String convertLocationAsJsonString(Location location) {
		if (location == null) {
			return null;
		}
		try {
			JSONObject object = new JSONObject();
			object.put(MSGKEY_PROVIDER, location.getProvider());
			object.put(MSGKEY_TIME, location.getTime());
			// Lat Lng
			int latE6 = (int)(location.getLatitude()*AppConstant.E6);
			int lngE6 = (int)(location.getLongitude()*AppConstant.E6); 
			object.put(MSGKEY_LATITUDE_E6, latE6);
			object.put(MSGKEY_LONGITUDE_E6, lngE6);
			// altitude
			if (location.hasAltitude()) {
				int alt = (int) location.getAltitude();
				object.put(MSGKEY_ALTITUDE,alt);
			}
			object.put(MSGKEY_ACCURACY, location.getAccuracy());
			if (location.hasBearing()) {
				int bearing = (int) location.getBearing();
				object.put(MSGKEY_BEARING, bearing);
			}
			if (location.hasSpeed()) {
				int speed = (int) location.getSpeed();
				object.put(MSGKEY_SPEAD, speed);
			}
			return object.toString();
		} catch (JSONException e) {
			Log.e(SmsLocationHelper.class.getSimpleName(), e.getMessage());
		}
		return null;
	}

	public static Location convertJsonLocAsLocation(String jsonLocation) {
		if (jsonLocation == null || jsonLocation.length() < 1) {
			return null;
		}
		try {
			JSONObject object = (JSONObject) new JSONTokener(jsonLocation).nextValue();
			String provider = object.getString(MSGKEY_PROVIDER);
			Location loc = new Location(provider);
			// Value
			long time = object.getLong(MSGKEY_TIME);
			// LatLng
			int latE6 =  object.getInt(MSGKEY_LATITUDE_E6);
			int lngE6 = object.getInt(MSGKEY_LONGITUDE_E6);
 			double latitude =latE6 /AppConstant.E6;
			double longitude = lngE6 / AppConstant.E6;
			// Accuracy
			float accuracy = Double.valueOf(object.getDouble(MSGKEY_ACCURACY)).floatValue();
			loc.setTime(time);
			loc.setLatitude(latitude);
			loc.setLongitude(longitude);
			loc.setAccuracy(accuracy);
			// Optionnal
			if (object.has(MSGKEY_ALTITUDE)) {
				double altitude = object.getDouble(MSGKEY_ALTITUDE);
				loc.setAltitude(altitude);
			}
			if (object.has(MSGKEY_BEARING)) {
				float bearing = Double.valueOf(object.getDouble(MSGKEY_BEARING)).floatValue();
				loc.setBearing(bearing);
			}
			if (object.has(MSGKEY_SPEAD)) {
				float speed = Double.valueOf(object.getDouble(MSGKEY_SPEAD)).floatValue();
				loc.setSpeed(speed);
			}
			return loc;
		} catch (JSONException e) {
			Log.e(SmsLocationHelper.class.getSimpleName(), e.getMessage());
		}
		return null;
	}

}
