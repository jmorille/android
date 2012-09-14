package eu.ttbox.geoping.service.receiver;

import java.util.Date;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.GeoTrackSmsMsg;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.service.SmsMsgActionHelper;
import eu.ttbox.geoping.service.SmsMsgEncryptHelper;

/**
 * @see http://www.tutos-android.com/broadcast-receiver-android {link
 *      http://mobiforge.com/developing/story/sms-messaging-android}
 * @author deostem
 * 
 */
public class SMSReceiver extends BroadcastReceiver implements OnSharedPreferenceChangeListener {

    private static final String TAG = "SMSReceiver";

   
    private final String ACTION_RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_RECEIVE_SMS)) {
            Log.d(TAG, "SMSReceiver : " + intent);
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");

                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                if (messages.length > 0) {
                    SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean deleteSms = appPreferences.getBoolean(AppConstants.PREFS_SMS_DELETE_ON_MESSAGE, true);
                    appPreferences.registerOnSharedPreferenceChangeListener(this);
                    // context.getSharedPreferences("smsMonitorDelete",
                    // Context.MODE_PRIVATE).getBoolean(key, defValue);
                    for (SmsMessage message : messages) {
                        boolean isConsume = consumeMessage(context, appPreferences, message);
                        if (!isConsume) {
                            deleteSms = false;
                        }
                        if (deleteSms) {
                            Log.w(TAG, "Cancel wanting abortBroadcast for unexpected Sms Message " + message.getMessageBody());
                        }
                    }
                    if (deleteSms) {
                         abortBroadcast();
                    }

                }
            }
        }
    }

    private boolean consumeMessage(Context context, SharedPreferences appPreferences, SmsMessage message) {
        boolean isConsume = false;
        final String messageBody = message.getMessageBody();
        final String phoneNumber = message.getDisplayOriginatingAddress();
        Log.w(TAG, "Consume SMS Geo Action : " + phoneNumber + " / " + messageBody);
        // Decrypt Msg
        GeoTrackSmsMsg clearMsg = SmsMsgEncryptHelper.decodeSmsMessage(phoneNumber, messageBody);
        if (clearMsg != null && clearMsg.action != null) { 
            if (SmsMsgEncryptHelper.ACTION_GEO_LOC.equals(clearMsg.action)) {
                Location loc = SmsMsgActionHelper.fromSmsMessage(clearMsg.body);
                if (loc != null) {
                    manangeNewLocation(context, phoneNumber, loc);
                    isConsume = true;
                }
            } else if (SmsMsgEncryptHelper.ACTION_GEO_PING.equals(clearMsg.action)) {
                // Manage Notification
                boolean notifyRequest = appPreferences.getBoolean(AppConstants.PREFS_SMS_REQUEST_NOTIFY_ME, false); 
                if (notifyRequest) {
                    displayPingRequestNotification(clearMsg);
                }
                // Send request
                Intent intent = Intents.sendGeoPingResponse(context, phoneNumber);
                context.startService(intent);
                isConsume = true;
            }
        }
        return isConsume;
    }
    
    private void displayPingRequestNotification(GeoTrackSmsMsg clearMsg ) {
//        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private void manangeNewLocation(Context context, String phoneNumber, Location loc) {
        if (loc != null) {
            GeoTrack geoPoint = new GeoTrack(phoneNumber, loc);
            ContentValues values = GeoTrackHelper.getContentValues(geoPoint);
            context.getContentResolver().insert(GeoTrackerProvider.Constants.CONTENT_URI, values);
//            Toast.makeText(context,
//                    "Message : " + new Date(loc.getTime()).toLocaleString() + " (" + loc.getLatitude() + "," + loc.getLongitude() + " ~ " + loc.getAccuracy() + ") from " + phoneNumber,
//                    Toast.LENGTH_LONG).show();
        }

    }


}
