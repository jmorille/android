package eu.ttbox.geoping.service.master;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.GeoTrackSmsMsg;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.service.SmsMsgActionHelper;
import eu.ttbox.geoping.service.SmsMsgEncryptHelper;
import eu.ttbox.geoping.service.encoder.GeoPingMessage;
import eu.ttbox.geoping.service.encoder.SmsMessageEncoderHelper;
import eu.ttbox.geoping.service.encoder.SmsParamEncoderHelper;

public class GeoPingMasterService extends IntentService {

    private static final String TAG = "GeoPingMasterService";

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
        Log.d(TAG,String.format(  "onHandleIntent for action %s : %s", action, intent));
        if (Intents.ACTION_SMS_GEOPING_REQUEST_SENDER.equals(action)) {
            String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
            
            sendSmsGeoPingRequest(phone, params);
        } else if (Intents.ACTION_SMS_GEOPING_RESPONSE_HANDLER.equals(action)) {
            consumeGeoPingResponse(intent.getExtras()); 
        }

    }
    // ===========================================================
    // Send GeoPing Request
    // ===========================================================

    private void sendSmsGeoPingRequest(String phone,   Bundle params ) {
    	GeoPingMessage geoPingMessage = new GeoPingMessage(phone,SmsMessageEncoderHelper.ACTION_GEO_PING, params );
        sendSms(phone, geoPingMessage);
        Log.d(TAG, String.format("Send SMS GeoPing %s : %s", phone, geoPingMessage));
    }
    

    private void sendSms(String phone, GeoPingMessage smsMsg) {
        String encodeddMsg = SmsMessageEncoderHelper.encodeSmsMessage(smsMsg);
        if (smsMsg != null && !encodeddMsg.isEmpty() && encodeddMsg.length() <= AppConstants.SMS_MAX_SIZE) {
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
        GeoTrack geoTrack =  GeoTrackHelper.getEntityFromBundle(params); 
        geoTrack.setUserId(phone);
        if (geoTrack != null) { 
            ContentValues values = GeoTrackHelper.getContentValues(geoTrack);
            Uri uri = getContentResolver().insert(GeoTrackerProvider.Constants.CONTENT_URI, values);
            if (uri != null) {
                Log.d(TAG, String.format("Send Broadcast Notification for New GeoTrack %s ", uri));
//                Intent intent =    new Intent(Intents.ACTION_NEW_GEOTRACK_INSERTED);
                Intent intent =  Intents.newGeoTrackInserted(uri, values);
                sendBroadcast(intent); 
            }
            isConsume = true;
        }
        return isConsume;
    }

    // ===========================================================
    // Other
    // ===========================================================

    private void displayPingRequestNotification(GeoPingMessage clearMsg) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

}
