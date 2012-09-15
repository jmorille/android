package eu.ttbox.geoping.service.slave.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
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
public class SMSReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSReceiver";

    private final String ACTION_RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";

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

                    for (SmsMessage message : messages) {
                        boolean isConsume = consumeMessage(context,  message);
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

    private boolean consumeMessage(Context context,   SmsMessage message) {
        boolean isConsume = false;
        final String messageBody = message.getMessageBody();
        final String phoneNumber = message.getDisplayOriginatingAddress();
        Log.w(TAG, "Consume SMS Geo Action : " + phoneNumber + " / " + messageBody);
        // Decrypt Msg
        GeoTrackSmsMsg clearMsg = SmsMsgEncryptHelper.decodeSmsMessage(phoneNumber, messageBody);
        if (clearMsg != null && clearMsg.action != null) {
            if (SmsMsgEncryptHelper.ACTION_GEO_LOC.equals(clearMsg.action)) {
                // GeoPing Response
                isConsume = consumeGeoPingResponse(context, clearMsg);
            } else if (SmsMsgEncryptHelper.ACTION_GEO_PING.equals(clearMsg.action)) {
                // GeoPing Request
                isConsume = consumeGeoPingRequest(context, clearMsg);
            }
        }
        return isConsume;
    }

    private boolean consumeGeoPingRequest(Context context, GeoTrackSmsMsg clearMsg) {
        boolean isConsume = false; 
        context.startService(Intents.consumeSmsGeoPingRequestHandler(context, clearMsg ));
        isConsume = true;
        return isConsume;
    }

    private boolean consumeGeoPingResponse(Context context, GeoTrackSmsMsg clearMsg) {
        boolean isConsume = false;
        context.startService(Intents.consumerSmsGeoPingResponsetHandler(context, clearMsg )); 
        isConsume = true;
//        Location loc = SmsMsgActionHelper.fromSmsMessage(clearMsg.body);
//        if (loc != null) {
//            GeoTrack geoPoint = new GeoTrack(clearMsg.phone, loc);
//            ContentValues values = GeoTrackHelper.getContentValues(geoPoint);
//            context.getContentResolver().insert(GeoTrackerProvider.Constants.CONTENT_URI, values);
//            isConsume = true;
//        }
        return isConsume;
    }

}
