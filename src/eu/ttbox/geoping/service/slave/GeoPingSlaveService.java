package eu.ttbox.geoping.service.slave;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.service.core.WorkerService;
import eu.ttbox.geoping.service.encoder.GeoPingMessage;
import eu.ttbox.geoping.service.encoder.SmsMessageEncoderHelper;
import eu.ttbox.geoping.ui.map.mylocation.sensor.MyLocationListenerProxy;

public class GeoPingSlaveService extends WorkerService {

    private static final String TAG = "GeoPingSlaveService";

    private final IBinder binder = new LocalBinder();
    // Constant
    
    
    // Services
    private LocationManager locationManager;
    private MyLocationListenerProxy myLocation;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private SharedPreferences appPreferences;

    // Instance Data
    private List<GeoPingRequest> geoPingRequestList;
    private MultiGeoRequestLocationListener multiGeoRequestListener;

    private int batterLevelInPercent = -1;

    // ===========================================================
    // Constructors
    // ===========================================================

    public GeoPingSlaveService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // service
        this.appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.myLocation = new MyLocationListenerProxy(locationManager);
        this.geoPingRequestList = new ArrayList<GeoPingRequest>();
        this.multiGeoRequestListener = new MultiGeoRequestLocationListener(geoPingRequestList);

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

    // ===========================================================
    // Intent Handler
    // ===========================================================

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, String.format("onHandleIntent for action %s : %s", action, intent));
        if (Intents.ACTION_SMS_GEOPING_REQUEST_HANDLER.equals(action)) {
            String phoneNumber = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
            // Request
            int timeOutInSeconde = 30;
            GeoPingRequest request = new GeoPingRequest(phoneNumber, params);
            // schedule it
            registerGeoPingRequest(request);
            executorService.schedule(request, timeOutInSeconde, TimeUnit.SECONDS);
        }
    }

    // ===========================================================
    // GeoPing Request
    // ===========================================================
    private void test() {
        appPreferences.getStringSet();
    }

    // ===========================================================
    // Other
    // ===========================================================

    public boolean registerGeoPingRequest(GeoPingRequest request) {
        Location initLastLoc = myLocation.getLastKnownLocation();
        geoPingRequestList.add(request);
        // TODO Bad for multi request
        boolean locProviderEnabled = myLocation.startListening(multiGeoRequestListener);
        return locProviderEnabled;
    }

    public void unregisterGeoPingRequest(GeoPingRequest request) {
        boolean isRemove = geoPingRequestList.remove(request);
        if (isRemove) {

        } else {
            Log.e(TAG, "Could not remove expected GeoPingRequest. /!\\ Emmergency Stop Service /!\\");
            geoPingRequestList.clear();
        }
        if (geoPingRequestList.isEmpty()) {
            Log.d(TAG, "No GeoPing Request in list, do Stop Service");
            myLocation.stopListening();
            stopSelf();
        }
    }

    private void sendSmsLocation(String phone, Location location) {
        GeoTrack geotrack = new GeoTrack(phone, location);
        Bundle params = GeoTrackHelper.getBundleValues(geotrack);
        GeoPingMessage smsMsg = new GeoPingMessage(phone, SmsMessageEncoderHelper.ACTION_GEO_LOC, params);
        sendSms(phone, smsMsg);
    }

    private void sendSms(String phone, GeoPingMessage smsMsg) {
        String encrypedMsg = SmsMessageEncoderHelper.encodeSmsMessage(smsMsg);
        if (smsMsg != null && !encrypedMsg.isEmpty() && encrypedMsg.length() <= AppConstants.SMS_MAX_SIZE) {
            SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, null, null);
        }
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
        }

        @Override
        public Boolean call() throws Exception {
            Location lastLocation = myLocation.getLastFix();
            if (lastLocation != null) {
                sendSmsLocation(smsPhoneNumber, lastLocation);
                unregisterGeoPingRequest(this);
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
    // Notification
    // ===========================================================

    private void showNotificationNewPingRequest(Uri geoTrackData, ContentValues values) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, Intents.showOnMap(this, geoTrackData, values), PendingIntent.FLAG_CANCEL_CURRENT);

        String phone = values.getAsString(GeoTrackColumns.COL_PHONE_NUMBER);

        Notification notification = new NotificationCompat.Builder(this) //
                .setSmallIcon(R.drawable.icon_notif) //
                .setWhen(System.currentTimeMillis()) //
                .setAutoCancel(true) //
                .setContentTitle("GeoPing Request" + phone) //
                .setContentText("GeoTrack point") //
                .setContentIntent(pendingIntent)//
                .build();

    }

    // ===========================================================
    // Binder
    // ===========================================================

    public class LocalBinder extends Binder {
        public GeoPingSlaveService getService() {
            return GeoPingSlaveService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

}
