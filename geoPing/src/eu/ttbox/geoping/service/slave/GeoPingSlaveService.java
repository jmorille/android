package eu.ttbox.geoping.service.slave;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.CellLocation;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.SmsLogProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.domain.model.Pairing;
import eu.ttbox.geoping.domain.model.PairingAuthorizeTypeEnum;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.domain.pairing.PairingHelper;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.core.ContactHelper;
import eu.ttbox.geoping.service.core.ContactVo;
import eu.ttbox.geoping.service.core.WorkerService;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageIntentEncoderHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.slave.receiver.AuthorizePhoneTypeEnum;
import eu.ttbox.osm.ui.map.mylocation.sensor.MyLocationListenerProxy;

public class GeoPingSlaveService extends WorkerService {

    private static final String TAG = "GeoPingSlaveService";

    private static final int SHOW_GEOPING_REQUEST_NOTIFICATION_ID = AppConstants.PER_PERSON_ID_MULTIPLICATOR * R.id.show_notification_new_geoping_request_confirm;

    private final IBinder binder = new LocalBinder();
    // Constant

    // Services
    private NotificationManager mNotificationManager;
    private LocationManager locationManager;
    private MyLocationListenerProxy myLocation;
    private ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
    private SharedPreferences appPreferences;
    private TelephonyManager telephonyManager;
 
    // Instance Data
    private List<GeoPingRequest> geoPingRequestList;
    private MultiGeoRequestLocationListener multiGeoRequestListener;

    private int batterLevelInPercent = -1;

    // Config
    boolean displayGeopingRequestNotification = false;
    boolean saveInLocalDb = false;

    // Set<String> secuAuthorizeNeverPhoneSet;
    // Set<String> secuAuthorizeAlwaysPhoneSet;

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
        this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        this.telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        this.myLocation = new MyLocationListenerProxy(locationManager);
        this.geoPingRequestList = new ArrayList<GeoPingRequest>();
        this.multiGeoRequestListener = new MultiGeoRequestLocationListener(geoPingRequestList);
    
        loadPrefConfig();
        Log.d(TAG, "#################################");
        Log.d(TAG, "### GeoPing Service Started.");
        Log.d(TAG, "#################################");
    }

    private void loadPrefConfig() {
        this.displayGeopingRequestNotification = appPreferences.getBoolean(AppConstants.PREFS_SHOW_GEOPING_NOTIFICATION, false);
        this.saveInLocalDb = appPreferences.getBoolean(AppConstants.PREFS_LOCAL_SAVE, false);
        // Read Security Set
        // this.secuAuthorizeNeverPhoneSet =
        // readPrefPhoneSet(AppConstants.PREFS_PHONES_SET_AUTHORIZE_NEVER);
        // this.secuAuthorizeAlwaysPhoneSet =
        // readPrefPhoneSet(AppConstants.PREFS_PHONES_SET_AUTHORIZE_ALWAYS);
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
            // GeoPing Request
            String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
            // Request
            // registerGeoPingRequest(phone, params);
            Pairing pairing = getPairingByPhone(phone);
            switch (pairing.authorizeType) {
            case AUTHORIZE_NEVER:
                Log.i(TAG, "Ignore Geoping (Never Authorize) request from phone " + phone);
                // Show Blocking Notification
                if (pairing.showNotification) {
                    showNotificationGeoPing(pairing, params, false);
                }
                break;
            case AUTHORIZE_ALWAYS:
                Log.i(TAG, "Accept Geoping (always Authorize) request from phone " + phone);
                registerGeoPingRequest(phone, params);
                // Display Notification GeoPing
                if (pairing.showNotification) {
                    showNotificationGeoPing(pairing, params, true);
                }
                break;
            case AUTHORIZE_REQUEST:
                GeopingNotifSlaveTypeEnum type = GeopingNotifSlaveTypeEnum.GEOPING_REQUEST_CONFIRM;
                if (AppConstants.UNSET_ID == pairing.id) {
                    type = GeopingNotifSlaveTypeEnum.GEOPING_REQUEST_CONFIRM_FIRST;
                }
                showNotificationNewPingRequestConfirm(pairing, params, GeopingNotifSlaveTypeEnum.GEOPING_REQUEST_CONFIRM);
                break;
            default:
                break;
            }
          
        } else if (Intents.ACTION_SLAVE_GEOPING_PHONE_AUTHORIZE.equals(action)) {
            // GeoPing Pairing User ressponse
            manageNotificationAuthorizeIntent(intent.getExtras());
        } else if (Intents.ACTION_SMS_PAIRING_RESQUEST.equals(action)) {
            // GeoPing Pairing
            String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);

            managePairingRequest(phone, params);

        }
    }

    // ===========================================================
    // Pairing
    // ===========================================================

    private void managePairingRequest(String phone, Bundle params) {
        PairingAuthorizeTypeEnum authorizeType = PairingAuthorizeTypeEnum.AUTHORIZE_REQUEST;
        Pairing pairing = getPairingByPhone(phone);
        if (pairing != null && pairing.authorizeType != null) {
            authorizeType = pairing.authorizeType;
        }
        long personId = SmsMessageLocEnum.PARAM_PERSON_ID.readLong(params, -1l);
        switch (authorizeType) {
        case AUTHORIZE_ALWAYS:// Already pairing, resent the response
            doPairingPhone(pairing, PairingAuthorizeTypeEnum.AUTHORIZE_ALWAYS, personId);
            break;
        case AUTHORIZE_NEVER: // No Auhtorize it !!
            doPairingPhone(pairing, PairingAuthorizeTypeEnum.AUTHORIZE_NEVER, personId);
            break;
        case AUTHORIZE_REQUEST:
            // Open the Notification For asking Yes or fuck
            showNotificationNewPingRequestConfirm(pairing, params, GeopingNotifSlaveTypeEnum.PAIRING);
            break;
        default:
            break;
        }
    }

    private void manageNotificationAuthorizeIntent(Bundle extras) {
        // Read Intent
        String phone = extras.getString(Intents.EXTRA_SMS_PHONE);
        Bundle params = extras.getBundle(Intents.EXTRA_SMS_PARAMS);
        long personId = SmsMessageLocEnum.PARAM_PERSON_ID.readLong(params, -1l);
        GeopingNotifSlaveTypeEnum notifType = GeopingNotifSlaveTypeEnum.getByOrdinal(extras.getInt(Intents.EXTRA_NOTIFICATION_TYPE_ENUM_ORDINAL, -1));
        AuthorizePhoneTypeEnum type = AuthorizePhoneTypeEnum.getByOrdinal(extras.getInt(Intents.EXTRA_AUTHORIZE_PHONE_TYPE_ENUM_ORDINAL));
        String personNewName = extras.getString(Intents.EXTRA_PERSON_NAME);
        // Cancel Notification
        int notifId = extras.getInt(Intents.EXTRA_NOTIF_ID, -1);
        if (notifId != -1) {
            mNotificationManager.cancel(notifId);
        }
        // Read Pairing
        Pairing pairing = getPairingByPhone(phone);
        if (TextUtils.isEmpty(pairing.name) && personNewName != null) {
            pairing.name = personNewName;
        }
        // ### Manage Pairing Type
        // #############################
        Log.d(TAG, String.format("manageAuthorizeIntent for phone %s with User security choice %s", phone, type));
        boolean positifResponse = false;
        switch (type) {
        case NEVER:
            doPairingPhone(pairing, PairingAuthorizeTypeEnum.AUTHORIZE_NEVER, personId);
            break;
        case ALWAYS:
            doPairingPhone(pairing, PairingAuthorizeTypeEnum.AUTHORIZE_ALWAYS, personId);
            positifResponse = true;
            break;
        case YES:
            positifResponse = true;
            if (AppConstants.UNSET_ID == pairing.id) {
                doPairingPhone(pairing, PairingAuthorizeTypeEnum.AUTHORIZE_REQUEST, personId);
            }
            break;
        default:
            Log.w(TAG, "Not manage PhoneAuthorizeTypeEnum for " + type);
            positifResponse = false;
            break;
        }

        // ### Manage Notification Type
        // #############################
        switch (notifType) {
        case GEOPING_REQUEST_CONFIRM:
            if (positifResponse) {
                registerGeoPingRequest(phone, params);
            }
            break;
        default:
            break;
        }
    }

    private void doPairingPhone(Pairing pairing, PairingAuthorizeTypeEnum authorizeType, long personId) {
        // ### Persist Pairing
        // #############################
        if (pairing.id > -1l) {
            if (pairing.authorizeType == null || !pairing.authorizeType.equals(authorizeType)) {
                // update
                ContentValues values = authorizeType.writeTo(null);
                values.put(PairingColumns.COL_PAIRING_TIME, System.currentTimeMillis());
                Uri uri = Uri.withAppendedPath(PairingProvider.Constants.CONTENT_URI, String.valueOf(pairing.id));
                int affectedRow = getContentResolver().update(uri, values, null, null);
                Log.w(TAG, String.format("Change %s Pairing %s : to new  %s", affectedRow, uri, authorizeType));
            } else {
                Log.d(TAG, String.format("Ignore Change Pairing type %s to %s", pairing.authorizeType, authorizeType));
            }
        } else {
            // Create
            pairing.setPairingTime(System.currentTimeMillis());
            ContentValues values = PairingHelper.getContentValues(pairing);
            authorizeType.writeTo(values);
            Uri pairingUri = getContentResolver().insert(PairingProvider.Constants.CONTENT_URI, values);
            if (pairingUri != null) {
                String entityId = pairingUri.getLastPathSegment();
                pairing.setId(Long.valueOf(entityId));
            }
            Log.w(TAG, String.format("Insert new Pairing %s : to new  %s", pairingUri, pairing));
        }
        // ### Send Pairing response
        // #############################
        switch (authorizeType) {
        case AUTHORIZE_NEVER:
            // TODO Check Last Send
            break;
        case AUTHORIZE_ALWAYS:
            sendPairingResponse(pairing.phone, personId, authorizeType);
            break;
        default:
            break;
        }

    }

    private void sendPairingResponse(String phone, long personId, PairingAuthorizeTypeEnum authorizeType) {
        Bundle params = null;
        if (personId != -1l) {
            params = SmsMessageLocEnum.PARAM_PERSON_ID.writeToBundle(null, personId);
        }
        sendSms(phone, SmsMessageActionEnum.ACTION_GEO_PAIRING_RESPONSE, params);
    }

    // ===========================================================
    // GeoPing Security
    // ===========================================================

    private Pairing getPairingByPhone(String phoneNumber) {
        Pairing result = null;
        // Search
        // Log.d(TAG, String.format("Search Painring for Phone [%s]",
        // phoneNumber));
        Uri uri = Uri.withAppendedPath(PairingProvider.Constants.CONTENT_URI_PHONE_FILTER, Uri.encode(phoneNumber));
        Cursor cur = getContentResolver().query(uri, null, null, null, null);
        try {
            if (cur != null && cur.moveToFirst()) {
                PairingHelper helper = new PairingHelper().initWrapper(cur);
                result = helper.getEntity(cur);
            }
        } finally {
            cur.close();
        }
        Log.d(TAG, String.format("Search Painring for Phone [%s] : Found %s", phoneNumber, result));
        // Create It
        if (result == null) {
            // TODO Read Prefs values
            result = new Pairing();
            result.setPhone(phoneNumber);
            result.setShowNotification(displayGeopingRequestNotification);
            result.setAuthorizeType(PairingAuthorizeTypeEnum.AUTHORIZE_REQUEST);
        }
        return result;
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
    // Other
    // ===========================================================

    public boolean registerGeoPingRequest(String phoneNumber, Bundle params) {
        Location initLastLoc = myLocation.getLastKnownLocation();
        GeoPingRequest request = new GeoPingRequest(phoneNumber, params);
        geoPingRequestList.add(request);
        // TODO Bad for multi request
        boolean locProviderEnabled = myLocation.startListening(multiGeoRequestListener);
        // schedule it for time out
        int timeOutInSeconde = 30;
        executorService.schedule(request, timeOutInSeconde, TimeUnit.SECONDS);
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

    // ===========================================================
    // Sender Sms message
    // ===========================================================

    private void sendSmsLocation(String phone, Location location) {
        GeoTrack geotrack = new GeoTrack(null, location);
        geotrack.batteryLevelInPercent = batterLevelInPercent;
        Bundle params = GeoTrackHelper.getBundleValues(geotrack);
        sendSms(phone, SmsMessageActionEnum.ACTION_GEO_LOC, params);
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

    private void sendSms(String phone, SmsMessageActionEnum action, Bundle params) {
        String encrypedMsg = SmsMessageIntentEncoderHelper.encodeSmsMessage(action, params);
        if (encrypedMsg != null && encrypedMsg.length() > 0 && encrypedMsg.length() <= AppConstants.SMS_MAX_SIZE) {
            SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, null, null);
            // Log It
            logSmsMessage(SmsLogTypeEnum.SEND, phone, action, params);
        } else {
            Log.e(TAG, String.format("Too long SmsMessage (%s chars, args) : %s", encrypedMsg.length(), encrypedMsg));
        }
    }

    // ===========================================================
    // Log Sms message
    // ===========================================================

    private void logSmsMessage(SmsLogTypeEnum type, String phone, SmsMessageActionEnum action, Bundle params) {
        ContentValues values = SmsLogHelper.getContentValues(type, phone, action, params);
        getContentResolver().insert(SmsLogProvider.Constants.CONTENT_URI, values);
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

    private void showNotificationGeoPing(Pairing pairing, Bundle params, boolean authorizeIt) {
        String phone = pairing.phone;
        // Contact Name
        ContactVo contact = ContactHelper.searchContactForPhone(this, phone);
        String contactDisplayName = phone;
        Bitmap photo = null;
        if (contact != null) {
            if (contact.displayName != null && contact.displayName.length() > 0) {
                contactDisplayName = contact.displayName;
                if (TextUtils.isEmpty(pairing.name)) {
                    pairing.name = contactDisplayName;
                }
            }
            photo = ContactHelper.openPhotoBitmap(this, contact.id);
        }
        // Create Notifiation
        Builder notificationBuilder = new NotificationCompat.Builder(this) //
                .setDefaults(Notification.DEFAULT_ALL) //
                .setSmallIcon(R.drawable.ic_stat_notif_icon) //
                .setWhen(System.currentTimeMillis()) //
                .setAutoCancel(true) //
                .setContentText(contactDisplayName); //
        if (authorizeIt) {
            notificationBuilder.setContentTitle(getString(R.string.notif_geoping_request)); //
        } else {
            notificationBuilder.setContentTitle(getString(R.string.notif_geoping_request_blocked)); //
        }
        if (photo != null) {
            notificationBuilder.setLargeIcon(photo);
        } else {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_notif_icon);
            notificationBuilder.setLargeIcon(icon);
        }
        Notification notification = notificationBuilder.build();
        // Show
        // int notifId = SHOW_GEOPING_REQUEST_NOTIFICATION_ID +
        // phone.hashCode();
        // Log.d(TAG, String.format("GeoPing Notification Id : %s for phone %s",
        // notifId, phone));
        mNotificationManager.notify(SHOW_GEOPING_REQUEST_NOTIFICATION_ID, notification);
    }

    private void showNotificationNewPingRequestConfirm(Pairing pairing, Bundle params, GeopingNotifSlaveTypeEnum onlyPairing) {
//    	Log.d(TAG,"******************************************************");
//    	Log.d(TAG,"*****  showNotificationNewPingRequestConfirm  ****");
//    	Log.d(TAG,"******************************************************");
        String phone = pairing.phone;
        String contactDisplayName = phone;
        String contactNewName = null;
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notif_geoping_request_register);
        // Contact Name
        ContactVo contact = ContactHelper.searchContactForPhone(this, phone);
        Bitmap photo = null;
        if (contact != null) {
            if (contact.displayName != null && contact.displayName.length() > 0) {

                contactDisplayName = contact.displayName;
                if (TextUtils.isEmpty(pairing.name)) {
                    contactNewName = contact.displayName;
                    pairing.name = contact.displayName;
                }
            }
            photo = ContactHelper.openPhotoBitmap(this, contact.id);
        }
        
        // Generate Notification ID per Person
        int notifId = SHOW_GEOPING_REQUEST_NOTIFICATION_ID + phone.hashCode();
        Log.d(TAG, String.format("GeoPing Notification Id : %s for phone %s", notifId, phone));

         // Content Intent In android 2.3 no Custun View displayble
        //TODO Propose a choice
        PendingIntent contentIntent =  null;

        // Title
        String title;
        switch (onlyPairing) {
        case PAIRING:
            contentView.setViewVisibility(R.id.notif_geoping_confirm_button_no, View.GONE);
            contentView.setTextViewText(R.id.notif_geoping_confirm_button_yes, getText(R.string.notif_confirm_request_eachtime));
            title = getString(R.string.notif_pairing);
            contentIntent =  PendingIntent.getService(this, 0, //
                    Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.ALWAYS, notifId, onlyPairing),//
                    PendingIntent.FLAG_UPDATE_CURRENT);
            break;
        case GEOPING_REQUEST_CONFIRM:
            title = getString(R.string.notif_geoping_request);
            contentView.setViewVisibility(R.id.notif_geoping_confirm_button_never, View.GONE);
            contentView.setViewVisibility(R.id.notif_geoping_confirm_button_always, View.GONE);
            break;
        case GEOPING_REQUEST_CONFIRM_FIRST:
            title = getString(R.string.notif_geoping_request);
            contentView.setViewVisibility(R.id.notif_geoping_confirm_button_yes, View.GONE);
            contentView.setViewVisibility(R.id.notif_geoping_confirm_button_no, View.GONE);
            contentView.setViewVisibility(R.id.notif_geoping_confirm_button_always, View.GONE);
            break;
        default:
            title = getString(R.string.app_name);
            break;
        }


        // View
        contentView.setTextViewText(R.id.notif_geoping_title, title);
        contentView.setTextViewText(R.id.notif_geoping_phone, contactDisplayName);
        // Manage Button Confirmation
        contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_no, PendingIntent.getService(this, 0, //
                Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.NO, notifId, onlyPairing),//
                PendingIntent.FLAG_UPDATE_CURRENT));
        contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_never, PendingIntent.getService(this, 1, //
                Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.NEVER, notifId, onlyPairing),//
                PendingIntent.FLAG_UPDATE_CURRENT));
        contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_yes, PendingIntent.getService(this, 2, //
                Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.YES, notifId, onlyPairing),//
                PendingIntent.FLAG_UPDATE_CURRENT));
        contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_always, PendingIntent.getService(this, 3, //
                Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.ALWAYS, notifId, onlyPairing),//
                PendingIntent.FLAG_UPDATE_CURRENT));
        
        // Content Intent
        if (contentIntent==null) {
	        contentIntent =  PendingIntent.getService(this, 0, //
	                Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.YES, notifId, onlyPairing),//
	                PendingIntent.FLAG_UPDATE_CURRENT);
        }

        
        // Create Notifiation
        Builder notificationBuilder = new NotificationCompat.Builder(this) //
                .setDefaults(Notification.DEFAULT_ALL) //
                .setSmallIcon(R.drawable.ic_stat_notif_icon) //
                .setWhen(System.currentTimeMillis()) //
                .setAutoCancel(true) //
                .setContentTitle(title) //
                .setContentText(contactDisplayName) //
                .setContentIntent(contentIntent) //
                .setContent(contentView); //
        if (photo != null) {
            notificationBuilder.setLargeIcon(photo);
        } else {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_notif_icon);
            notificationBuilder.setLargeIcon(icon);
        }
        Notification notification = notificationBuilder.build();
        
        // Show
        mNotificationManager.notify(notifId, notification);
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
