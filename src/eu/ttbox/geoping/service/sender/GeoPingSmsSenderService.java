package eu.ttbox.geoping.service.sender;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrackSmsMsg;
import eu.ttbox.geoping.service.SmsMsgActionHelper;
import eu.ttbox.geoping.service.SmsMsgEncryptHelper;
import eu.ttbox.geoping.service.core.WorkerService;
import eu.ttbox.geoping.ui.map.mylocation.sensor.MyLocationListenerProxy;

public class GeoPingSmsSenderService extends WorkerService {

	private static final String TAG = "GeoPingSmsSenderService";

	private LocationManager locationManager;

	private MyLocationListenerProxy myLocation;

	private ScheduledExecutorService executorService = Executors
			.newScheduledThreadPool(1);

	private List<GeoPingRequest> geoPingRequestList;
	private MultiGeoRequestLocationListener multiGeoRequestListener;

	private int batterLevelInPercent = -1;

	public GeoPingSmsSenderService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// service
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.myLocation = new MyLocationListenerProxy(locationManager);
		this.geoPingRequestList = new ArrayList<GeoPingRequest>();
		this.multiGeoRequestListener = new MultiGeoRequestLocationListener(
				geoPingRequestList);

		Log.d(TAG, "#################################");
		Log.d(TAG, "### GeoPing Service Started.");
		Log.d(TAG, "#################################");
	}

	@Override
	public void onDestroy() {
		this.myLocation.stopListening();
		geoPingRequestList.clear();
		super.onDestroy();
		Log.d(TAG, "#################################");
		Log.d(TAG, "### GeoPing Service Destroyed.");
		Log.d(TAG, "#################################");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if (Intents.ACTION_SMS_GEOPING_RESPONSE.equals(action)) {
			String phoneNumber = intent
					.getStringExtra(Intents.EXTRA_SMS_PHONE_NUMBER);
			// String accuracyExpected =
			// intent.getStringExtra(Intents.EXTRA_EXPECTED_ACCURACY);
			// Request
			int timeOutInSeconde = 30;
			GeoPingRequest request = new GeoPingRequest(phoneNumber);
			// schedule it
			registerGeoPingRequest(request);
			executorService.schedule(request, timeOutInSeconde,
					TimeUnit.SECONDS);
		} else if (Intents.ACTION_SMS_GEOPING_REQUEST.equals(action)) {
			String phone = intent
					.getStringExtra(Intents.EXTRA_SMS_PHONE_NUMBER);
			sendSmsPing(phone);
		}
	}

	public boolean registerGeoPingRequest(GeoPingRequest request) {
		Location initLastLoc = myLocation.getLastKnownLocation();
		geoPingRequestList.add(request);
		// TODO Bad for multi request
		boolean locProviderEnabled = myLocation
				.startListening(multiGeoRequestListener);
		return locProviderEnabled;
	}

	public void unregisterGeoPingRequest(GeoPingRequest request) {
		boolean isRemove = geoPingRequestList.remove(request);
		if (isRemove) {

		} else {
			Log.e(TAG,
					"Could not remove expected GeoPingRequest. !! Stop Service !!");
			geoPingRequestList.clear();
		}
		if (geoPingRequestList.isEmpty()) {
			Log.e(TAG, "Ne GeoPing Request in list, Stop Service");
			myLocation.stopListening();
			stopSelf();
		}
	}

	private void sendSmsPing(String phone) {
		GeoTrackSmsMsg clearMsg = SmsMsgActionHelper.geoPingMessage();
		sendSms(phone, clearMsg);
		Log.d(TAG, String.format("Send SMS GeoPing %s : %s", phone, clearMsg));
	}

	private void sendSms(String phone, GeoTrackSmsMsg smsMsg) {
		String encrypedMsg = SmsMsgEncryptHelper.encodeSmsMessage(smsMsg);
		if (smsMsg != null && !encrypedMsg.isEmpty()
				&& encrypedMsg.length() <= AppConstants.SMS_MAX_SIZE) {
			SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg,
					null, null);
		}
	}

	private void sendSmsLocation(String phone, Location location) {
		GeoTrackSmsMsg smsMsg = SmsMsgActionHelper.geoLocMessage(location);
		sendSms(phone, smsMsg);
	}

	/**
	 * Computes the battery level by registering a receiver to the intent
	 * triggered by a battery status/level change. {link
	 * http://mobile.dzone.com/news/getting-battery-level-android}
	 */

	private void batteryLevel() {
		BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				context.unregisterReceiver(this);
				int rawlevel = intent.getIntExtra("level", -1);
				int scale = intent.getIntExtra("scale", -1);
				int level = -1;
				if (rawlevel >= 0 && scale > 0) {
					level = (rawlevel * 100) / scale;
				}
				batterLevelInPercent = level;
			}
		};

		IntentFilter batteryLevelFilter = new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryLevelReceiver, batteryLevelFilter);
	}

	public class GeoPingRequest implements Callable<Boolean>, LocationListener {

		public String smsPhoneNumber;

		public GeoPingRequest() {
			super();
		}

		public GeoPingRequest(String phoneNumber) {
			super();
			this.smsPhoneNumber = phoneNumber;
		}

		@Override
		public Boolean call() throws Exception {
			Location lastLocation = myLocation.getLastFix();
			if (lastLocation != null) {
				sendSmsLocation(smsPhoneNumber, lastLocation);
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO check expected accuracy
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

	}

}
