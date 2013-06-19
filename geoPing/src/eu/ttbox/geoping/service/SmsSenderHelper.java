package eu.ttbox.geoping.service;

import java.util.Map;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.SmsLogProvider;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.encoder.GeoPingMessage;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.encoder.helper.SmsMessageIntentEncoderHelper;
import eu.ttbox.geoping.service.receiver.MessageAcknowledgeReceiver;

public class SmsSenderHelper {

    private static final String TAG = "SmsSenderHelper";

    public static Bundle completeRequestTimeOutFromPrefs(SharedPreferences appPreferences, Bundle params) {
        Bundle result = params ==null ? new Bundle() : params;
        if (!result.containsKey(SmsMessageLocEnum.TIME_IN_S.dbFieldName)) {
            int timeOut =  appPreferences.getInt(AppConstants.PREFS_REQUEST_TIMEOUT_S, -1);
            if (timeOut > -1) {
                result.putInt(SmsMessageLocEnum.TIME_IN_S.dbFieldName, timeOut);
            }
        }
        if (!result.containsKey(SmsMessageLocEnum.ACCURACY .dbFieldName)) {
            int accuracy =  appPreferences.getInt(AppConstants.PREFS_REQUEST_ACCURACY_M, -1);
            if (accuracy > -1) {
                result.putInt(SmsMessageLocEnum.ACCURACY.dbFieldName, accuracy);
            }
        }
        return result;
    }

    public static Uri sendSmsAndLogIt(Context context, SmsLogSideEnum side, String phone, SmsMessageActionEnum action, Bundle params) {
        Uri isSend = null;
        String encrypedMsg = SmsMessageIntentEncoderHelper.encodeSmsMessage(action, params);
        Log.d(TAG, String.format("Send Request SmsMessage to %s : %s (%s)", phone, action, encrypedMsg));
        if (encrypedMsg != null && encrypedMsg.length() > 0 && encrypedMsg.length() <= AppConstants.SMS_MAX_SIZE_7BITS) {
            // Log It
            ContentResolver cr = context.getContentResolver();
            Uri logUri = logSmsMessage(cr, side, SmsLogTypeEnum.SEND_REQ, phone, action, params, 1, encrypedMsg);
            Log.d(TAG, "SmsMessage Request save Log to : " + logUri);
            // Acknowledge
            PendingIntent sendIntent = PendingIntent.getBroadcast(context, 0, //
                    new Intent(MessageAcknowledgeReceiver.ACTION_SEND_ACK).setData(logUri) //
                    , 0);
            PendingIntent deliveryIntent = PendingIntent.getBroadcast(context, 0, //
                    new Intent(MessageAcknowledgeReceiver.ACTION_DELIVERY_ACK).setData(logUri) //
                    , 0);
            // Send Message
            SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, sendIntent, deliveryIntent);
            isSend = logUri;
            Log.d(TAG, String.format("Send SmsMessage (%s chars, args) : %s", encrypedMsg.length(), encrypedMsg));
        } else {
            // TODO display Too long messsage
             Log.e(TAG, String.format("Too long SmsMessage (%s chars, args) : %s", encrypedMsg.length(), encrypedMsg));
        }
        return isSend;
    }

 

    // ===========================================================
    // Log Sms message
    // ===========================================================

    public static Uri logSmsMessage(ContentResolver cr, SmsLogSideEnum side, SmsLogTypeEnum type, GeoPingMessage geoMessage, int smsWeight, String encrypedMsg) {
        return logSmsMessage(cr, side, type, geoMessage.phone, geoMessage.action, geoMessage.params, smsWeight, encrypedMsg);
    }

    public static Uri logSmsMessage(ContentResolver cr, SmsLogSideEnum side, SmsLogTypeEnum type, String phone, SmsMessageActionEnum action, Bundle params, int smsWeight, String encrypedMsg) {
        ContentValues values = SmsLogHelper.getContentValues(side, type, phone, action, params, encrypedMsg);
        values.put(SmsLogColumns.COL_SMS_WEIGHT, smsWeight);
        Uri logUri = cr.insert(SmsLogProvider.Constants.CONTENT_URI, values);
        return logUri;
    }

    private void printContentValues(ContentValues values) {
        for (Map.Entry<String, Object> key : values.valueSet()) {
            Object val = key.getValue();
            Log.d(TAG, "SaveLog ContentValues : " + key.getKey() + " = " + val);
        }
    }
}
