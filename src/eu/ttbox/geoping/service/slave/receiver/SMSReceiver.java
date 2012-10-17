package eu.ttbox.geoping.service.slave.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.SmsLogProvider;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.encoder.GeoPingMessage;
import eu.ttbox.geoping.service.encoder.SmsMessageIntentEncoderHelper;

/**
 * @see http://www.tutos-android.com/broadcast-receiver-android {link
 *      http://mobiforge.com/developing/story/sms-messaging-android}
 * @author deostem
 * 
 */
public class SMSReceiver extends BroadcastReceiver {

    private static final String TAG = "SMSReceiver";

    public  static final  String ACTION_RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";
    public  static final  String EXTRA_PDUS = "pdus";
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction().equals(ACTION_RECEIVE_SMS)) {
            Log.d(TAG, "SMSReceiver : " + intent);
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get(EXTRA_PDUS);

                final SmsMessage[] messages = new SmsMessage[pdus.length];
                for (int i = 0; i < pdus.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
                if (messages.length > 0) {
                    SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                    boolean deleteSms = appPreferences.getBoolean(AppConstants.PREFS_SMS_DELETE_ON_MESSAGE, true);

                    for (SmsMessage message : messages) {
                        boolean isConsume = consumeMessage(context, message);
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

    private boolean consumeMessage(Context context, SmsMessage message) {
        boolean isConsume = false;
        final String messageBody = message.getMessageBody();
        final String phoneNumber = message.getDisplayOriginatingAddress();
        Log.w(TAG, "Consume SMS Geo Action : " + phoneNumber + " / " + messageBody);
        // Decrypt Msg
        GeoPingMessage geoMsg = SmsMessageIntentEncoderHelper.decodeAsGeoPingMessage(  context,   phoneNumber, messageBody);
        Intent intent = SmsMessageIntentEncoderHelper.convertForIntentGeoPingMessage(context, geoMsg);
        if (intent != null) { 
            isConsume = true;
            context.startService(intent);
            // Log It
            logSmsMessage(context,SmsLogTypeEnum.RECEIVE,  geoMsg);
        } 
        return isConsume;
    }
    
	// ===========================================================
	// Log Sms message
	// ===========================================================

	private void logSmsMessage(Context context, SmsLogTypeEnum type,   GeoPingMessage geoMsg) {
		ContentValues values = SmsLogHelper.getContentValues(type, geoMsg);
		context.getContentResolver().insert(SmsLogProvider.Constants.CONTENT_URI, values);
	}


}
