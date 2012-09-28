package eu.ttbox.geoping.service.slave;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.service.core.ContactVo;
import eu.ttbox.geoping.service.core.WorkerService;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageIntentEncoderHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.slave.receiver.PhoneAuthorizeTypeEnum;
import eu.ttbox.geoping.ui.map.mylocation.sensor.MyLocationListenerProxy;

public class GeoPingSlaveService extends WorkerService {

    private static final String TAG = "GeoPingSlaveService";

    private static final int SHOW_GEOPING_REQUEST_NOTIFICATION_ID = R.id.show_notification_new_geoping_request_confirm;

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

    // Config
    boolean displayGeopingRequestNotification = false;
    Set<String> secuAuthorizeNeverPhoneSet;
    Set<String> secuAuthorizeAlwaysPhoneSet;

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
        loadPrefConfig();
        Log.d(TAG, "#################################");
        Log.d(TAG, "### GeoPing Service Started.");
        Log.d(TAG, "#################################");
    }

    private void loadPrefConfig() {
        this.displayGeopingRequestNotification = appPreferences.getBoolean(AppConstants.PREFS_GEOPING_REQUEST_NOTIFYME, false);

        // Read Security Set
        this.secuAuthorizeNeverPhoneSet = readPrefPhoneSet(AppConstants.PREFS_PHONES_SET_AUTHORIZE_NEVER);
        this.secuAuthorizeAlwaysPhoneSet = readPrefPhoneSet(AppConstants.PREFS_PHONES_SET_AUTHORIZE_ALWAYS);
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
        // SmsMessageActionEnum intentAction =
        // SmsMessageActionEnum.getByIntentName(action);
        // switch (intentAction) {
        // case GEOPING_REQUEST:
        //
        // break;
        // case ACTION_GEO_PAIRING:
        //
        // break;
        //
        // default:
        // break;
        // }
        //
        Log.d(TAG, String.format("onHandleIntent for action %s : %s", action, intent));
        if (Intents.ACTION_SMS_GEOPING_REQUEST_HANDLER.equals(action)) {
            String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
            // Request
            // registerGeoPingRequest(phone, params);
            if (isAuthorizePhoneAlways(phone)) {
                registerGeoPingRequest(phone, params);
            } else if (!isAuthorizePhoneNever(phone)) {
                showNotificationNewPingRequestConfirm(phone, params, false);
            } else {
                Log.i(TAG, "Ignore Never Authorize Geoping request from phone " + phone);
            }

        } else if (Intents.ACTION_SLAVE_GEOPING_PHONE_AUTHORIZE.equals(action)) {
            manageAuthorizeIntent(intent.getExtras());
        } else if (Intents.ACTION_SMS_PAIRING_RESQUEST.equals(action)) {
            String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
            if (isAuthorizePhoneAlways(phone)) {
                long personId = SmsMessageLocEnum.MSGKEY_PERSON_ID.readLong(params, -1l);
                sendPairingResponse(phone, personId);
            } else if (!isAuthorizePhoneNever(phone)) {
                showNotificationNewPingRequestConfirm(phone, params, true);
                // TODO Send pairing Response
            }
        }
    }

    // ===========================================================
    // Pairing
    // ===========================================================

    private void manageAuthorizeIntent(Bundle extras) {
        // Init
        String phone = extras.getString(Intents.EXTRA_SMS_PHONE);
        Bundle params = extras.getBundle(Intents.EXTRA_SMS_PARAMS);
        boolean onlyPairing = extras.getBoolean(Intents.EXTRA_PAIRING_ONLY);
        int typeOrdinal = extras.getInt(Intents.EXTRA_AUTHORIZE_PHONE_TYPE_ORDINAL);
        PhoneAuthorizeTypeEnum type = PhoneAuthorizeTypeEnum.values()[typeOrdinal];
        // Cancel Notification
        int notifId = extras.getInt(Intents.EXTRA_NOTIF_ID, -1);
        Log.w(TAG, "Remove Notification Id : " + notifId);
        if (notifId != -1) {
            NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notifManager.cancel(notifId);
        }
        // Manage case
        boolean isPairing = false;
        Log.d(TAG, String.format("manageAuthorizeIntent for phone %s with security policy %s (%s)", phone, type, typeOrdinal));
        switch (type) {
        case NEVER:
            Log.d(TAG, "Need to authorizePhoneNever case Never");
            // authorizePhoneNever(phone);
        case NO:
            Log.d(TAG, "Need to authorizePhoneNever case No");
            Log.i(TAG, "Ignore Geoping request from phone " + phone);
            break;
        case ALWAYS:
            Log.d(TAG, "Need to authorizePhoneNever case ALWAYS");
            // authorizePhoneAlways(phone);
            isPairing = true;
        case YES:
            Log.d(TAG, "Need to authorizePhoneNever case Yes");
            if (!onlyPairing) {
                registerGeoPingRequest(phone, params);
            }
            break;
        default:
            Log.w(TAG, "Not manage PhoneAuthorizeTypeEnum for " + type);
            break;
        }
        // Send Pairing response
        if (isPairing) {
            long personId = SmsMessageLocEnum.MSGKEY_PERSON_ID.readLong(params, -1l);
            sendPairingResponse(phone, personId);
        }
    }

    private void sendPairingResponse(String phone, long personId) {
        Bundle params = null;
        if (personId != -1l) {
            params = SmsMessageLocEnum.MSGKEY_PERSON_ID.writeToBundle(null, personId);
        }
        sendSms(phone, SmsMessageActionEnum.ACTION_GEO_PAIRING_RESPONSE, params);
    }

    // ===========================================================
    // GeoPing Security
    // ===========================================================

    private void authorizePhoneNever(String phone) {
        if (!secuAuthorizeNeverPhoneSet.contains(phone)) {
            secuAuthorizeNeverPhoneSet.add(phone);
            Editor editor = appPreferences.edit();
            // Never
            String neverPhoneString = convertPhoneSetAsString(secuAuthorizeNeverPhoneSet);
            editor.putString(AppConstants.PREFS_PHONES_SET_AUTHORIZE_NEVER, neverPhoneString);
            // Always
            if (secuAuthorizeAlwaysPhoneSet.contains(phone)) {
                secuAuthorizeAlwaysPhoneSet.remove(phone);
                String alwaysPhoneString = convertPhoneSetAsString(secuAuthorizeAlwaysPhoneSet);
                editor.putString(AppConstants.PREFS_PHONES_SET_AUTHORIZE_ALWAYS, alwaysPhoneString);
            }
            Log.i(TAG, "Security Phone Authorize NEVER for Phone : " + phone);
            editor.commit();
        }
    }

    private boolean isAuthorizePhoneAlways(String phone) {
        return secuAuthorizeAlwaysPhoneSet.contains(phone);
    }

    private boolean isAuthorizePhoneNever(String phone) {
        return secuAuthorizeNeverPhoneSet.contains(phone);
    }

    private void authorizePhoneAlways(String phone) {
        if (!secuAuthorizeAlwaysPhoneSet.contains(phone)) {
            secuAuthorizeAlwaysPhoneSet.add(phone);
            Editor editor = appPreferences.edit();
            // Never
            String alwayPhoneString = convertPhoneSetAsString(secuAuthorizeAlwaysPhoneSet);
            editor.putString(AppConstants.PREFS_PHONES_SET_AUTHORIZE_ALWAYS, alwayPhoneString);
            // Always
            if (secuAuthorizeNeverPhoneSet.contains(phone)) {
                secuAuthorizeNeverPhoneSet.remove(phone);
                String neverPhoneString = convertPhoneSetAsString(secuAuthorizeNeverPhoneSet);
                editor.putString(AppConstants.PREFS_PHONES_SET_AUTHORIZE_NEVER, neverPhoneString);
            }
            Log.i(TAG, "Security Phone Authorize ALWAYS for Phone : " + phone);
            editor.commit();
        }
    }

    private Set<String> readPrefPhoneSet(String key) {
        String phoneSet = appPreferences.getString(key, null);
        HashSet<String> result = new HashSet<String>();
        if (phoneSet != null && phoneSet.length() > 0) {
            int phoneSetSize = phoneSet.length();
            int pos = 0;
            int end = 0;
            while ((end = phoneSet.indexOf(AppConstants.PHONE_SEP, pos)) >= 0) {
                result.add(phoneSet.substring(pos, end));
                pos = end + 1;
            }
            if (pos < phoneSetSize) {
                result.add(phoneSet.substring(pos, phoneSetSize));
            }
        }
        return result;
    }

    private String convertPhoneSetAsString(Set<String> phoneSet) {
        StringBuilder sb = new StringBuilder();
        boolean addSep = false;
        for (String phone : phoneSet) {
            if (addSep) {
                sb.append(AppConstants.PHONE_SEP);
            }
            sb.append(phone);
            addSep = true;
        }
        return sb.toString();
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

    private void sendSmsLocation(String phone, Location location) {
        GeoTrack geotrack = new GeoTrack(null, location);
        Bundle params = GeoTrackHelper.getBundleValues(geotrack);
        sendSms(phone, SmsMessageActionEnum.ACTION_GEO_LOC, params);
    }

    private void sendSms(String phone, SmsMessageActionEnum action, Bundle params) {
        String encrypedMsg = SmsMessageIntentEncoderHelper.encodeSmsMessage(action, params);
        if (encrypedMsg != null && encrypedMsg.length() > 0 && encrypedMsg.length() <= AppConstants.SMS_MAX_SIZE) {
            SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, null, null);
        } else {
            Log.e(TAG, String.format("Too long SmsMessage (%s chars, args) : %s", encrypedMsg.length(), encrypedMsg));
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

    private void showNotificationNewPingRequestPost(String phone, Bundle params) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Create Notifiation
        Notification notification = new NotificationCompat.Builder(this) //
                .setDefaults(Notification.DEFAULT_ALL) //
                .setSmallIcon(R.drawable.ic_stat_notif_icon) //
                .setWhen(System.currentTimeMillis()) //
                .setAutoCancel(true) //
                .setContentTitle(getString(R.string.notif_geoping_response)) //
                .setContentText(phone) //
                .setPriority(Notification.PRIORITY_DEFAULT) //
                .build();
        // Show
        mNotificationManager.notify(SHOW_GEOPING_REQUEST_NOTIFICATION_ID, notification);
    }

    private void showNotificationNewPingRequestConfirm(String phone, Bundle params, boolean onlyPairing) {
        String phoneNumber = phone;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notif_geoping_request_register);
        // Contact Name
        ContactVo contact = searchContactForPhone(phone);
        if (contact != null) {
            if (contact.displayName != null && contact.displayName.length() > 0) {
                phoneNumber = contact.displayName;
            }
            Bitmap photo = openPhotoBitmap(contact.id);
            if (photo != null) {
                 contentView.setImageViewBitmap(R.id.notif_geoping_photo,  photo);
//                
            }  
        }
        // Title
        String title = "GeoPing Request";
        if (onlyPairing) {
            contentView.setViewVisibility(R.id.notif_geoping_confirm_button_yes, View.GONE);
            title = "Pairing Request";
        }
        // TODO Generate Notification ID per Person
        int notifId = SHOW_GEOPING_REQUEST_NOTIFICATION_ID  + phone.hashCode();
      Log.d(TAG,String.format(  "GeoPing Notification Id : %s for phone %s", notifId, phone));
        // View
        contentView.setTextViewText(R.id.notif_geoping_title, title);
        contentView.setTextViewText(R.id.notif_geoping_phone, phoneNumber);
        // Manage Button Confirmation
        contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_no, PendingIntent.getService(this, 0, //
                Intents.authorizePhone(this, phone, params, PhoneAuthorizeTypeEnum.NO, notifId, onlyPairing),//
                PendingIntent.FLAG_UPDATE_CURRENT));
        contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_never, PendingIntent.getService(this, 1, //
                Intents.authorizePhone(this, phone, params, PhoneAuthorizeTypeEnum.NEVER, notifId, onlyPairing),//
                PendingIntent.FLAG_UPDATE_CURRENT));
        contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_yes, PendingIntent.getService(this, 2, //
                Intents.authorizePhone(this, phone, params, PhoneAuthorizeTypeEnum.YES, notifId, onlyPairing),//
                PendingIntent.FLAG_UPDATE_CURRENT));
        contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_always, PendingIntent.getService(this, 3, //
                Intents.authorizePhone(this, phone, params, PhoneAuthorizeTypeEnum.ALWAYS, notifId, onlyPairing),//
                PendingIntent.FLAG_UPDATE_CURRENT));

        // Create Notifiation
        Notification notification = new NotificationCompat.Builder(this) //
                .setDefaults(Notification.DEFAULT_ALL) //
                .setSmallIcon(R.drawable.ic_stat_notif_icon) //
                .setWhen(System.currentTimeMillis()) //
                .setAutoCancel(true) //
                .setContentTitle(title) //
                .setContentText(phoneNumber) //
                .setContent(contentView) //
                .build();
        // Show
        mNotificationManager.notify(notifId, notification);
    }

    private boolean isPermissionReadContact() {
        return PackageManager.PERMISSION_GRANTED == getPackageManager().checkPermission("android.permission.READ_CONTACTS", getPackageName());
    }

    private ContactVo searchContactForPhone(String phoneNumber) {
        String contactName = null;
        long contactId = -1l;
        if (isPermissionReadContact()) {
            Log.d(TAG, String.format("Search Contact Name for Phone [%s]", phoneNumber));
            Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cur = getContentResolver().query(uri, new String[] { PhoneLookup.DISPLAY_NAME, PhoneLookup._ID }, null, null, null);

            try {
                if (cur != null && cur.moveToFirst()) {
                    contactName = cur.getString(cur.getColumnIndex(PhoneLookup.DISPLAY_NAME));
                    contactId = cur.getLong(cur.getColumnIndexOrThrow(PhoneLookup._ID));
                }
            } finally {
                cur.close();
            }
        }
        Log.d(TAG, String.format("Found Contact %s Name for Phone [%s] : %s", contactId, phoneNumber, contactName));
        ContactVo result = null;
        if (contactId != -1l) {
            result = new ContactVo(contactId, contactName);
        }
        return result;
    }

    public Bitmap openPhotoBitmap(long contactId) {
        Bitmap photo = null;
        InputStream is = openPhoto(Long.valueOf(contactId));
        if (is != null) {
            photo = BitmapFactory.decodeStream(is);
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close Contact Photo Input Stream");
            }
        }
        return photo;
    }

    public InputStream openPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = getContentResolver().query(photoUri, new String[] { Contacts.Photo.PHOTO }, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
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
