package eu.ttbox.geoping.service.master;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.util.Log;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageIntentEncoderHelper;

public class GeoPingMasterService extends IntentService {

    private static final String TAG = "GeoPingMasterService";

    private static final int SHOW_ON_NOTIFICATION_ID = R.id.show_notification_new_geoping_response;

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
        }

    }

    // ===========================================================
    // Send GeoPing Request
    // ===========================================================

    private void sendSmsPairingRequest(String phone, long userId) {
        Bundle params = new Bundle();
        params.putLong(GeoTrackColumns.COL_PERSON_ID, userId); 
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
                showNotificationNewPingResponse(uri, values);
            }
            isConsume = true;
        }
        return isConsume;
    }

    private void searchPersonForPhone(String person) {

    }

    private String searchContactForPhone(String phoneNumber) {
        Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cur = getContentResolver().query(uri, new String[] { PhoneLookup.DISPLAY_NAME }, null, null, null);
        try {
            if (cur != null && cur.moveToFirst()) {
                String value = cur.getString(cur.getColumnIndex(PhoneLookup.DISPLAY_NAME));
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

    private void showNotificationNewPingResponse(Uri geoTrackData, ContentValues values) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Create Notif
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, Intents.showOnMap(this, geoTrackData, values), PendingIntent.FLAG_CANCEL_CURRENT);

        // construct the Notification object.
        String phone = values.getAsString(GeoTrackColumns.COL_PHONE_NUMBER);
        // Style style = new NotificationCompat.InboxStyle()//
        // .addLine(getString(R.string.notif_geoping_response)) //
        // .addLine(phone) //
        // .setSummaryText("sumary");
        Notification notification = new NotificationCompat.Builder(this) //
                .setDefaults(Notification.DEFAULT_ALL) //
                .setSmallIcon(R.drawable.icon_notif) //
                .setWhen(System.currentTimeMillis()) //
                // .setStyle(style) //
                .setAutoCancel(true) //
                .setContentTitle(getString(R.string.notif_geoping_response)) //
                .setContentText(phone) //
                .setContentIntent(pendingIntent)//
                .setPriority(Notification.PRIORITY_DEFAULT) //
                .build();

        mNotificationManager.notify(SHOW_ON_NOTIFICATION_ID, notification);
    }

    // ===========================================================
    // Other
    // ===========================================================

}
