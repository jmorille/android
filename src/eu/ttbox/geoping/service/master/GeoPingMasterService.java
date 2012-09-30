package eu.ttbox.geoping.service.master;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.SmsManager;
import android.util.Log;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.SmsTrakerActivity;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.Person;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.service.core.ContactHelper;
import eu.ttbox.geoping.service.core.ContactVo;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageIntentEncoderHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;

public class GeoPingMasterService extends IntentService {

    private static final String TAG = "GeoPingMasterService";

    private static final int SHOW_ON_NOTIFICATION_ID = AppConstants.PER_PERSON_ID_MULTIPLICATOR * R.id.show_notification_new_geoping_response;

    private final IBinder binder = new LocalBinder();

    // Service
    private SharedPreferences appPreferences;

    // config
    boolean notifyGeoPingResponse = false;

    // Instance Data

    // ===========================================================
    // Constructors
    // ===========================================================

    public GeoPingMasterService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // service
        this.appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.notifyGeoPingResponse = appPreferences.getBoolean(AppConstants.PREFS_SMS_REQUEST_NOTIFY_ME, false);

        Log.d(TAG, "#################################");
        Log.d(TAG, "### GeoPingMasterService Service Started.");
        Log.d(TAG, "#################################");
    }

    // ===========================================================
    // Binder
    // ===========================================================

    public class LocalBinder extends Binder {
        public GeoPingMasterService getService() {
            return GeoPingMasterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // ===========================================================
    // Intent Handler
    // ===========================================================

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, String.format("onHandleIntent for action %s : %s", action, intent));
        if (Intents.ACTION_SMS_GEOPING_REQUEST_SENDER.equals(action)) {
            String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
            sendSmsGeoPingRequest(phone, params);
        } else if (Intents.ACTION_SMS_GEOPING_RESPONSE_HANDLER.equals(action)) {
            consumeGeoPingResponse(intent.getExtras());
        } else if (Intents.ACTION_SMS_PAIRING_RESQUEST.equals(action)) {
            String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            long userId = intent.getLongExtra(Intents.EXTRA_SMS_USER_ID, -1);
            sendSmsPairingRequest(phone, userId);
        } else if (Intents.ACTION_SMS_PAIRING_RESPONSE.equals(action)) {
            String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
            long userId = SmsMessageLocEnum.MSGKEY_PERSON_ID.readLong(params, -1);
            consumeSmsPairingResponse(phone, userId);
        }

    }

    // ===========================================================
    // Send GeoPing Request
    // ===========================================================

    private void consumeSmsPairingResponse(String phone, long userId) {
        if (userId != -1l) {
            // Search Phone
            String personPhone = null;
            Uri uri = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI, String.valueOf(userId));
            String[] cols = new String[] { PersonColumns.COL_PHONE };
            Cursor cur = getContentResolver().query(uri, cols, null, null, null);
            try {
                if (cur != null && cur.moveToFirst()) {
                    personPhone = cur.getString(1);
                }
            } finally {
                cur.close();
            }
            Log.d(TAG, String.format("Paring response for person Id : %s with phone [%s] =?= Sms [%s]", userId, personPhone, phone));
            // Update
            if (personPhone == null || !personPhone.equals(phone)) {
                ContentValues values = new ContentValues(1);
                values.put(PersonColumns.COL_PHONE, phone);
                getContentResolver().update(uri, values, null, null);
            }
        } else {
            Log.w(TAG, "Paring response Canceled for no userId");
        }

    }

    private void sendSmsPairingRequest(String phone, long userId) {
        Bundle params = SmsMessageLocEnum.MSGKEY_PERSON_ID.writeToBundle(null, userId);
        sendSms(phone, SmsMessageActionEnum.ACTION_GEO_PAIRING, params);

    }

    private void sendSmsGeoPingRequest(String phone, Bundle params) {
        sendSms(phone, SmsMessageActionEnum.GEOPING_REQUEST, params);
        Log.d(TAG, String.format("Send SMS GeoPing %s : %s", phone, params));
    }

    private void sendSms(String phone, SmsMessageActionEnum action, Bundle params) {
        String encodeddMsg = SmsMessageIntentEncoderHelper.encodeSmsMessage(action, params);
        if (encodeddMsg != null && encodeddMsg.length() > 0 && encodeddMsg.length() <= AppConstants.SMS_MAX_SIZE) {
            SmsManager.getDefault().sendTextMessage(phone, null, encodeddMsg, null, null);
        }
    }

    // ===========================================================
    // Consume Localisation
    // ===========================================================
    private void consumeSmsLog(Bundle bundle) {
        // TODO
    }

    private boolean consumeGeoPingResponse(Bundle bundle) {
        boolean isConsume = false;
        String phone = bundle.getString(Intents.EXTRA_SMS_PHONE);
        Bundle params = bundle.getBundle(Intents.EXTRA_SMS_PARAMS);
        GeoTrack geoTrack = GeoTrackHelper.getEntityFromBundle(params);
        geoTrack.setPhone(phone);
        if (geoTrack != null) {
            if (!geoTrack.hasPhone()) {

            }
            ContentValues values = GeoTrackHelper.getContentValues(geoTrack);
            Uri uri = getContentResolver().insert(GeoTrackerProvider.Constants.CONTENT_URI, values);
            if (uri != null) {
                Log.d(TAG, String.format("Send Broadcast Notification for New GeoTrack %s ", uri));
                // BroadCast Response
                Intent intent = Intents.newGeoTrackInserted(uri, values);
                sendBroadcast(intent);
                // Display Notification
                showNotificationGeoPing(uri, values);
            }
            isConsume = true;
        }
        return isConsume;
    }

    private Person searchPersonForPhone(String phoneNumber) {
        Person person = null;
        Log.d(TAG, String.format("Search Contact Name for Phone [%s]", phoneNumber));
        Uri uri = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI_PHONE_FILTER, Uri.encode(phoneNumber));
        Cursor cur = getContentResolver().query(uri, null, null, null, null);
        try {
            if (cur != null && cur.moveToFirst()) {
                PersonHelper helper = new PersonHelper().initWrapper(cur);
                person = helper.getEntity(cur);
            }
        } finally {
            cur.close();
        }
        return person;
    }

    private String searchPersonForPersonId(long personId) {
        String entityId = String.valueOf(personId);
        Uri uri = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI, entityId);
        String[] cols = new String[] { PersonColumns.COL_PHONE };
        Cursor cur = getContentResolver().query(uri, cols, null, null, null);
        try {
            if (cur != null && cur.moveToFirst()) {
                String value = cur.getString(1);
                return value;
            }
        } finally {
            cur.close();
        }
        return null;
    }

    // ===========================================================
    // Notification
    // ===========================================================


    @SuppressLint("NewApi")
    private void showNotificationGeoPing(Uri geoTrackData, ContentValues values) {
        String phone = values.getAsString(GeoTrackColumns.COL_PHONE_NUMBER);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Contact Name
        ContactVo contact = ContactHelper.searchContactForPhone(this, phone);
        String contactDisplayName = phone;
        Bitmap photo = null;
        if (contact != null) {
            if (contact.displayName != null && contact.displayName.length() > 0) {
                contactDisplayName = contact.displayName;
            }
            photo = ContactHelper.openPhotoBitmap(this, contact.id);
        }
        // Create Notif Intent response
        PendingIntent pendingIntent = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            pendingIntent = PendingIntent.getActivities(this, 0, //
                    new Intent[] { new Intent(this, SmsTrakerActivity.class), Intents.showOnMap(this, geoTrackData, values) }, //
                    PendingIntent.FLAG_CANCEL_CURRENT);
        } else {
            pendingIntent = PendingIntent.getActivity(this, 0, //
                    Intents.showOnMap(this, geoTrackData, values), //
                    PendingIntent.FLAG_CANCEL_CURRENT);
        }
        // Create Notifiation
        Builder notificationBuilder = new NotificationCompat.Builder(this) //
                .setDefaults(Notification.DEFAULT_ALL) //
                .setSmallIcon(R.drawable.ic_stat_notif_icon) //
                .setWhen(System.currentTimeMillis()) //
                .setAutoCancel(true) //
                 .setContentIntent(pendingIntent)//
                .setContentTitle(getString(R.string.notif_geoping)) //
                .setContentText(contactDisplayName); // 
        if (photo != null) {
            notificationBuilder.setLargeIcon(photo);
        } else {
            Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_notif_icon);
            notificationBuilder.setLargeIcon(icon);
        }
        Notification notification = notificationBuilder.build();
        // Show
      int notifId = SHOW_ON_NOTIFICATION_ID + phone.hashCode();
      Log.d(TAG, String.format("GeoPing Notification Id : %s for phone %s", notifId, phone));

        mNotificationManager.notify(notifId, notification);
    }
    
//    @SuppressLint("NewApi")
//    private void showNotificationNewPingResponse(Uri geoTrackData, ContentValues values) {
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        // Create Notif Intent response
//        PendingIntent pendingIntent = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            pendingIntent = PendingIntent.getActivities(this, 0, //
//                    new Intent[] { new Intent(this, SmsTrakerActivity.class), Intents.showOnMap(this, geoTrackData, values) }, //
//                    PendingIntent.FLAG_CANCEL_CURRENT);
//        } else {
//            pendingIntent = PendingIntent.getActivity(this, 0, //
//                    Intents.showOnMap(this, geoTrackData, values), //
//                    PendingIntent.FLAG_CANCEL_CURRENT);
//        }
//
//        // construct the Notification object.
//        String phone = values.getAsString(GeoTrackColumns.COL_PHONE_NUMBER);
//
//        // Generate Notification ID per Person
//        int notifId = SHOW_ON_NOTIFICATION_ID + phone.hashCode();
//        Log.d(TAG, String.format("GeoPing Notification Id : %s for phone %s", notifId, phone));
//
//        // Photo
//
//        // Style style = new NotificationCompat.InboxStyle()//
//        // .addLine(getString(R.string.notif_geoping_response)) //
//        // .addLine(phone) //
//        // .setSummaryText("sumary");
//        Notification notification = new NotificationCompat.Builder(this) //
//                .setDefaults(Notification.DEFAULT_ALL) //
//                .setSmallIcon(R.drawable.ic_stat_notif_icon) //
//                .setWhen(System.currentTimeMillis()) //
//                // .setStyle(style) //
//                .setAutoCancel(true) //
//                .setContentTitle(getString(R.string.notif_geoping_response)) //
//                .setContentText(phone) //
//                .setContentIntent(pendingIntent)//
//                .setPriority(Notification.PRIORITY_DEFAULT) //
//                .build();
//
//        mNotificationManager.notify(notifId, notification);
//    }
    // ===========================================================
    // Other
    // ===========================================================

}
