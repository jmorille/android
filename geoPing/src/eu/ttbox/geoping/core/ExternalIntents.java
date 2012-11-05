package eu.ttbox.geoping.core;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class ExternalIntents {

	public static void startActivityMarketDetails(Context context, String marketPackage) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.setData(Uri.parse("market://details?id=" + marketPackage));
		context.startActivity(intent); 
	}

	public static void startActivityGpsSettings(Context context) {
		Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		context.startActivity(intent);
	}

	public static void startActivityGpsStatus(Context context) {
		try {
			Intent gpsStatusIntent = new Intent("com.eclipsim.gpsstatus.VIEW");
			gpsStatusIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(gpsStatusIntent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, "GPS Status not found. Launching Market for you.", Toast.LENGTH_SHORT).show();
			// get http://www.cyrket.com/package/com.eclipsim.gpsstatus2 from
			startActivityMarketDetails(context, "com.eclipsim.gpsstatus2");
		} 
	}

	public static void startActivityNavigationTo(Context context, double lat, double lng) {
		try {
			// http://maps.google.com/maps?saddr=20.344,34.34&daddr=20.5666,45.345
			Uri uri = Uri.parse(String.format("google.navigation:q=%s,%s", lat, lng));
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
			// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			startActivityMarketDetails(context, "com.google.android.apps.maps");
		}
	}

	// TODO
	// https://developers.google.com/maps/documentation/directions/?hl=fr-FR
	// http://maps.googleapis.com/maps/api/directions/json?origin=Adelaide,SA&destination=Adelaide,SA&waypoints=optimize:true|Barossa+Valley,SA|Clare,SA|Connawarra,SA|McLaren+Vale,SA&sensor=false
	/**
	 * @see http://developer.android.com/guide/appendix/g-app-intents.html
	 * 
	 */
	public static void startActivityStreetView(Context context, double lat, double lng) {
		try {
			// google.streetview:cbll=lat,lng&cbp=1,yaw,,pitch,zoom&mz=mapZoom

			// Panorama center-of-view in degrees clockwise from North.
			// Note: The two commas after the yaw parameter are required. They
			// are present for backwards-compatibility reasons
			int yaw = 0;
			// Panorama center-of-view in degrees from -90 (look straight up) to
			// 90 (look straight down.)
			int pitch = 0;
			// Panorama zoom. 1.0 = normal zoom, 2.0 = zoomed in 2x, 3.0 =
			// zoomed in 4x, and so on.
			int zoom = 1;
			// The map zoom of the map location associated with this panorama.
			// This value is passed on to the Maps activity when the Street View
			// "Go to Maps"
			// menu item is chosen. It corresponds to the z parameter in the
			// geo: intent.
			int mapZoom = 17;
			String urlString = String.format("google.streetview:cbll=%s,%s&cbp=1,%s,,%s,%s&mz=%s", lat, lng, yaw, pitch, zoom, mapZoom);
			Uri uri = Uri.parse(urlString);
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			startActivityMarketDetails(context, "com.google.android.street");
		}
	}

}
