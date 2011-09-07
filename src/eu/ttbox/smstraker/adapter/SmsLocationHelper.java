package eu.ttbox.smstraker.adapter;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.location.Location;
import android.util.Log;

public class SmsLocationHelper {

	private final static String MSGID = "smsTracker#";

	private final static String MSGKEY_PROVIDER = "providersuperlongpourvoir";
	private final static String MSGKEY_TIME = "t";
	private final static String MSGKEY_LATITUDE = "la";
	private final static String MSGKEY_LONGITUDE = "ln";
	private final static String MSGKEY_ALTITUDE = "al";
	private final static String MSGKEY_ACCURACY = "ac";
	private final static String MSGKEY_BEARING = "b";
	private final static String MSGKEY_SPEAD = "s";

	public static String toSmsMessage(Location location) {
		if (location != null) {
			String body = convertLocationAsJsonString(location);
			String msg = MSGID + body;
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
			object.put(MSGKEY_LATITUDE, location.getLatitude());
			object.put(MSGKEY_LONGITUDE, location.getLongitude());
			if (location.hasAltitude()) {
				object.put(MSGKEY_ALTITUDE, location.getAltitude());
			}
			object.put(MSGKEY_ACCURACY, location.getAccuracy());
			if (location.hasBearing()) {
				object.put(MSGKEY_BEARING, location.getBearing());
			}
			if (location.hasSpeed()) {
				object.put(MSGKEY_SPEAD, location.getSpeed());
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
			double latitude = object.getDouble(MSGKEY_LATITUDE);
			double longitude = object.getDouble(MSGKEY_LONGITUDE);
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
