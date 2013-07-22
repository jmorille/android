package eu.ttbox.geoping.service;

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

import java.util.ArrayList;
import java.util.Map;

import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.SmsLogProvider;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.service.encoder.MessageEncoderHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.encoder.adpater.BundleEncoderAdapter;
import eu.ttbox.geoping.service.receiver.MessageAcknowledgeReceiver;

public class SmsSenderHelper {

    private static final String TAG = "SmsSenderHelper";


    public static final String EXTRA_MSG_PART_COUNT = MessageAcknowledgeReceiver.EXTRA_ACK_MSG_PART_COUNT;
    public static final String EXTRA_MSG_PART_ID = MessageAcknowledgeReceiver.EXTRA_ACK_MSG_PART_ID;

    public static Bundle completeRequestTimeOutFromPrefs(SharedPreferences appPreferences, Bundle params) {
        Bundle result = params == null ? new Bundle() : params;
        if (!result.containsKey(SmsMessageLocEnum.TIME_IN_S.type.dbFieldName)) {
            int timeOut = appPreferences.getInt(AppConstants.PREFS_REQUEST_TIMEOUT_S, -1);
            if (timeOut > -1) {
                result.putInt(SmsMessageLocEnum.TIME_IN_S.type.dbFieldName, timeOut);
            }
        }
        if (!result.containsKey(SmsMessageLocEnum.ACCURACY.type.dbFieldName)) {
            int accuracy = appPreferences.getInt(AppConstants.PREFS_REQUEST_ACCURACY_M, -1);
            if (accuracy > -1) {
                result.putInt(SmsMessageLocEnum.ACCURACY.type.dbFieldName, accuracy);
            }
        }
        return result;
    }

    public static Uri sendSmsAndLogIt(Context context, SmsLogSideEnum side, String phone, MessageActionEnum action, Bundle params) {
        Uri isSend = null;
        String encrypedMsg = MessageEncoderHelper.encodeSmsMessage(action, params);
        Log.d(TAG, String.format("Send Request SmsMessage to %s : %s (%s)", phone, action, encrypedMsg));
        if (encrypedMsg != null && encrypedMsg.length() > 0) {
            SmsManager smsManager = SmsManager.getDefault();
            // Compute Messages
            ArrayList<String> msgsplit = smsManager.divideMessage(encrypedMsg);
            int msgSplitCount = msgsplit.size();
            // Log It
            ContentResolver cr = context.getContentResolver();
            Uri logUri = logSmsMessage(cr, side, SmsLogTypeEnum.SEND_REQ, phone, action, params, msgSplitCount, encrypedMsg, null);

            // Shot Message Send
            if (msgSplitCount==1) {
                // Acknowledge
                PendingIntent sendIntent = PendingIntent.getBroadcast(context, 0, //
                        new Intent(MessageAcknowledgeReceiver.ACTION_SEND_ACK).setData(logUri) //
                                .putExtra(EXTRA_MSG_PART_COUNT, msgSplitCount).putExtra(EXTRA_MSG_PART_ID, 1) //
                        , 0);
                PendingIntent deliveryIntent = PendingIntent.getBroadcast(context, 0, //
                        new Intent(MessageAcknowledgeReceiver.ACTION_DELIVERY_ACK).setData(logUri) //
                                .putExtra(EXTRA_MSG_PART_COUNT, msgSplitCount).putExtra(EXTRA_MSG_PART_ID, 1) //
                        , 0);
                // Send Message
                smsManager.sendTextMessage(phone, null, encrypedMsg, sendIntent, deliveryIntent);
                isSend = logUri;
                Log.d(TAG, String.format("Send SmsMessage (%s chars, args) : %s", encrypedMsg.length(), encrypedMsg));
            } else {
                // Acknowledge
                ArrayList<PendingIntent> sentIntents = new ArrayList<PendingIntent>(msgSplitCount);
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<PendingIntent>(msgSplitCount);
                for (int msgId = 1; msgId <= msgSplitCount; msgId++) {
                     // Acknowledge
                    PendingIntent sendIntent = PendingIntent.getBroadcast(context, 0, //
                            new Intent(MessageAcknowledgeReceiver.ACTION_SEND_ACK).setData(logUri) //
                                    .putExtra(EXTRA_MSG_PART_COUNT, msgSplitCount).putExtra(EXTRA_MSG_PART_ID, msgId) //
                            , 0); //  PendingIntent.FLAG_CANCEL_CURRENT
                    PendingIntent deliveryIntent = PendingIntent.getBroadcast(context, 0, //
                            new Intent(MessageAcknowledgeReceiver.ACTION_DELIVERY_ACK).setData(logUri) //
                                    .putExtra(EXTRA_MSG_PART_COUNT, msgSplitCount).putExtra(EXTRA_MSG_PART_ID, msgId) //
                            , 0);
                    sentIntents.add(sendIntent);
                    deliveryIntents.add(deliveryIntent);
                }
                // Send Message
                smsManager.sendMultipartTextMessage(phone, null, msgsplit, sentIntents, deliveryIntents);
                isSend = logUri;
                Log.d(TAG, String.format("Send Long SmsMessage (%s chars) : %s", encrypedMsg.length(), encrypedMsg));
            }
        }
        return isSend;
    }


    // ===========================================================
    // Log Sms message
    // ===========================================================

    /*
    @Deprecated
    public static Uri logSmsMessage(ContentResolver cr, SmsLogSideEnum side, SmsLogTypeEnum type, GeoPingMessage geoMessage, int smsWeight, String encrypedMsg) {
        return logSmsMessage(cr, side, type, geoMessage.phone, geoMessage.action, geoMessage.params, smsWeight, encrypedMsg);
    }

    @Deprecated
    public static Uri logSmsMessage(ContentResolver cr, SmsLogSideEnum side, SmsLogTypeEnum type, String phone, SmsMessageActionEnum action, Bundle params, int smsWeight, String encrypedMsg) {
        ContentValues values = SmsLogHelper.getContentValues(side, type, phone, action, params, encrypedMsg);
        values.put(SmsLogColumns.COL_MSG_COUNT, smsWeight);
        Uri logUri = cr.insert(SmsLogProvider.Constants.CONTENT_URI, values);
        return logUri;
    }
*/

    public static Uri logSmsMessage(ContentResolver cr, SmsLogSideEnum side, SmsLogTypeEnum type
            , BundleEncoderAdapter geoMessage
            , int smsWeight, String encrypedMsg
            , Uri parentUri) {
        return logSmsMessage(cr, side, type, geoMessage.getPhone(), geoMessage.getAction(), geoMessage.getMap(), smsWeight, encrypedMsg, parentUri);
    }

    public static Uri logSmsMessage(ContentResolver cr, SmsLogSideEnum side, SmsLogTypeEnum type, String phone
            , MessageActionEnum action, Bundle params
            , int smsWeight, String encrypedMsg
            , Uri parentUri  ) {
        ContentValues values = SmsLogHelper.getContentValues(side, type, phone, action, params, encrypedMsg);
        values.put(SmsLogColumns.COL_MSG_COUNT, smsWeight);
        if (parentUri!=null) {
            String logParentId = parentUri.getLastPathSegment();
            values.put(SmsLogColumns.COL_PARENT_ID, logParentId);
        }
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
