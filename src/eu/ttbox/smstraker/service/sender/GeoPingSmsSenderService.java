package eu.ttbox.smstraker.service.sender;

import java.util.List;

import eu.ttbox.smstraker.adapter.TrackerLocationHelper;
import eu.ttbox.smstraker.core.Intents;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

public class GeoPingSmsSenderService extends IntentService {

	private static final String TAG = "GeoPingSmsSenderService";

	private LocationManager locationManager;

	private Location lastLocation;

	public GeoPingSmsSenderService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if (Intents.ACTION_SMS_GEOPING.equals(action)) {
			String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE_NUMBER);
			GeoPingSmsLocationListener locationListener = new GeoPingSmsLocationListener(phone);
			enableMyLocation(locationListener);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// service
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		Log.d(TAG, "Service Started.");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "Service Destroyed.");
	}

	public boolean enableMyLocation(LocationListener locationListener) {
		List<String> providers = locationManager.getAllProviders();
		boolean locProviderEnabled = false;
		for (String provider : providers) {
			locationManager.requestLocationUpdates(provider, 0L, 0L, locationListener);
			locProviderEnabled = true;
		}
		return locProviderEnabled;
	}

	public void disableMyLocation(LocationListener locationListener) {
		this.locationManager.removeUpdates(locationListener);
	}

	private void sendSms(String phone, Location location) {
		String smsMsg = SmsLocationHelper.toSmsMessage(location);
		if (smsMsg != null && !smsMsg.isEmpty() && smsMsg.length() <= 255) {
			SmsManager.getDefault().sendTextMessage(phone, null, smsMsg, null, null);
		}
	}

	public class GeoPingSmsLocationListener implements LocationListener {

		private String smsPhoneNumber;

		public GeoPingSmsLocationListener(String smsPhoneNumber) {
			super();
			this.smsPhoneNumber = smsPhoneNumber;
		}

		@Override
		public void onLocationChanged(Location location) {
			if (TrackerLocationHelper.isBetterLocation(location, lastLocation)) {
				lastLocation = location;
			}
			sendSms(smsPhoneNumber, lastLocation);
			disableMyLocation(this);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}
		

	};

}
