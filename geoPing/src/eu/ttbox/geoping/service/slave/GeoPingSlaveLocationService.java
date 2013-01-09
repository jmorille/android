package eu.ttbox.geoping.service.slave;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.telephony.CellLocation;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.service.SmsSenderHelper;
import eu.ttbox.geoping.service.core.WorkerService;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.osm.ui.map.mylocation.sensor.MyLocationListenerProxy;

public class GeoPingSlaveLocationService extends WorkerService implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = "GeoPingSlaveLocationService";

	private static final String LOCK_NAME = "GeoPingSlaveLocationService";

	public static final String ACTION_FIND_LOCALISATION_AND_SEND_SMS_GEOPING = "ACTION_FIND_LOCALISATION_AND_SEND_SMS_GEOPING";

	private final IBinder binder = new LocalBinder();

	// Services
	private TelephonyManager telephonyManager;
	private LocationManager locationManager;
	private MyLocationListenerProxy myLocation;
	private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private SharedPreferences appPreferences;

	// Config
	private boolean saveInLocalDb = false;

	// Instance Data
	private List<GeoPingRequest> geoPingRequestList;
	private MultiGeoRequestLocationListener multiGeoRequestListener;

	private int batterLevelInPercent = -1;

	// ===========================================================
	// Lock
	// ===========================================================

	private static volatile PowerManager.WakeLock lockStatic = null;

	// public static void runIntentInService(Context context, Intent intent) {
	// PowerManager.WakeLock lock = getLock(context);
	// lock.acquire();
	// intent.setClassName(context, GeoPingSlaveService.class.getName());
	// context.startService(intent);
	// }
	private synchronized static PowerManager.WakeLock getLock(Context context) {
		if (lockStatic == null) {
			PowerManager mgr = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			lockStatic = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, LOCK_NAME);
			lockStatic.setReferenceCounted(true);
		}
		return (lockStatic);
	}

	// ===========================================================
	// Constructors
	// ===========================================================

	public GeoPingSlaveLocationService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// service
		this.appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		this.telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		this.myLocation = new MyLocationListenerProxy(locationManager);
		this.geoPingRequestList = new ArrayList<GeoPingRequest>();
		this.multiGeoRequestListener = new MultiGeoRequestLocationListener(geoPingRequestList);

		loadPrefConfig();
		// register listener
		appPreferences.registerOnSharedPreferenceChangeListener(this);

		Log.d(TAG, "#####################################");
		Log.d(TAG, "### GeoPing Location Service Started.");
		Log.d(TAG, "#####################################");
	}

	private void loadPrefConfig() {
		this.saveInLocalDb = appPreferences.getBoolean(AppConstants.PREFS_LOCAL_SAVE, false);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(AppConstants.PREFS_LOCAL_SAVE)) {
			this.saveInLocalDb = appPreferences.getBoolean(AppConstants.PREFS_LOCAL_SAVE, false);
		}
	}

	@Override
	public void onDestroy() {
		appPreferences.unregisterOnSharedPreferenceChangeListener(this);
		this.myLocation.stopListening();
		geoPingRequestList.clear();
		super.onDestroy();
		Log.d(TAG, "#######################################");
		Log.d(TAG, "### GeoPing Location Service Destroyed.");
		Log.d(TAG, "#######################################");
	}

	// ===========================================================
	// Intent Handler
	// ===========================================================

	 public static void runFindLocationAndSendInService(Context context, String phone, Bundle params) {
//		PowerManager.WakeLock lock = getLock(context);
//		lock.acquire();
		 Intent intent = new Intent(context, GeoPingSlaveLocationService.class);
		 intent.setAction(ACTION_FIND_LOCALISATION_AND_SEND_SMS_GEOPING);
		context.startService(intent);
	}
		 
	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if (ACTION_FIND_LOCALISATION_AND_SEND_SMS_GEOPING.equals(action)) {
			// GeoPing Request
			String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
			Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
			registerGeoPingRequest(phone, params);
		}
		
	}

	// ===========================================================
	// Cell Id
	// ===========================================================

	/**
	 * {link http://www.devx.com/wireless/Article/40524/0/page/2}
	 */
	private void getCellId() {
		CellLocation cellLoc = telephonyManager.getCellLocation();
		if (cellLoc instanceof GsmCellLocation) {
			GsmCellLocation gsmLoc = (GsmCellLocation) cellLoc;
			int lac = gsmLoc.getLac();
			int cellId = gsmLoc.getCid();
			Log.d(TAG, String.format("Cell Id : %s  / Lac : %s", cellId, lac));
		}
	}

	// ===========================================================
	// Geocoding Request
	// ===========================================================

	public boolean registerGeoPingRequest(String phoneNumber, Bundle params) {
		// Acquire Lock
		PowerManager.WakeLock lock = getLock(this.getApplicationContext());
		lock.acquire();
		Log.d(TAG, "*** Lock Acquire: " + lock);
		// Register request
		Location initLastLoc = myLocation.getLastKnownLocation();
		GeoPingRequest request = new GeoPingRequest(phoneNumber, params);
		geoPingRequestList.add(request);
		// TODO Bad for multi request
		boolean locProviderEnabled = myLocation.startListening(multiGeoRequestListener);
		// schedule it for time out
		// TODO
		int timeOutInSeconde = SmsMessageLocEnum.TIME_IN_S.readInt(params, 30);
		executorService.schedule(request, timeOutInSeconde, TimeUnit.SECONDS);

		return locProviderEnabled;
	}

	public void unregisterGeoPingRequest(GeoPingRequest request) {
		boolean isRemove = geoPingRequestList.remove(request);
		if (isRemove) {
			Log.d(TAG, "Remove GeoPing Request in list, do Stop Service");
		} else {
			Log.e(TAG, "Could not remove expected GeoPingRequest. /!\\ Emmergency Stop Service /!\\");
			geoPingRequestList.clear();
		}
		// Release Lock
		PowerManager.WakeLock lock = getLock(this.getApplicationContext());
		if (lock.isHeld()) {
			lock.release();
		}

		Log.d(TAG, "*** Lock Release: " + lock);
		// Stop Service if necessary
		if (geoPingRequestList.isEmpty()) {
			Log.d(TAG, "No GeoPing Request in list, do Stop Service");
			myLocation.stopListening();
			// Stop Service
			stopSelf();
		}
	}

	// ===========================================================
	// Sensor Listener
	// ===========================================================

	/**
	 * Computes the battery level by registering a receiver to the intent
	 * triggered by a battery status/level change. <br/>
	 * {@link http
	 * ://developer.android.com/training/monitoring-device-state/battery
	 * -monitoring.html}
	 */

	private void batteryLevel() {
		BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				context.unregisterReceiver(this);
				int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				int level = -1;
				if (rawlevel >= 0 && scale > 0) {
					level = (rawlevel * 100) / scale;
				}
				Log.d(TAG, "Battery Level Remaining: " + level + "%");
				batterLevelInPercent = level;
			}
		};

		IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		registerReceiver(batteryLevelReceiver, batteryLevelFilter);
	}

	public class GeoPingRequest implements Callable<Boolean>, LocationListener {

		public String smsPhoneNumber;
		public Bundle params;

		public GeoPingRequest() {
			super();
		}

		public GeoPingRequest(String phoneNumber, Bundle params) {
			super();
			this.smsPhoneNumber = phoneNumber;
			this.params = params;
			// register Listener for Battery Level
			batteryLevel();
		}

		@Override
		public Boolean call() throws Exception {
			Location lastLocation = myLocation.getLastFix();
			if (lastLocation != null) {
				sendSmsLocation(smsPhoneNumber, lastLocation);
				unregisterGeoPingRequest(GeoPingRequest.this);
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

	// ===========================================================
	// Sender Sms message
	// ===========================================================

	private void sendSmsLocation(String phone, Location location) {
		GeoTrack geotrack = new GeoTrack(null, location);
		geotrack.batteryLevelInPercent = batterLevelInPercent;
		Bundle params = GeoTrackHelper.getBundleValues(geotrack);
		ContentResolver cr = getContentResolver();
		SmsSenderHelper.sendSms(cr, phone, SmsMessageActionEnum.ACTION_GEO_LOC, params);
		if (saveInLocalDb) {
			geotrack.requesterPersonPhone = phone;
			saveInLocalDb(geotrack);
		}
	}

	private void saveInLocalDb(GeoTrack geotrack) {
		if (geotrack == null) {
			return;
		}
		ContentValues values = GeoTrackHelper.getContentValues(geotrack);
		values.put(GeoTrackColumns.COL_PHONE, AppConstants.KEY_DB_LOCAL);
		getContentResolver().insert(GeoTrackerProvider.Constants.CONTENT_URI, values);
	}

	// ===========================================================
	// Binder
	// ===========================================================

	public class LocalBinder extends Binder {
		public GeoPingSlaveLocationService getService() {
			return GeoPingSlaveLocationService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

}
