package eu.ttbox.geoping.service;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.SmsLogProvider;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageIntentEncoderHelper;

public class SmsSenderHelper {

	private static final String TAG = "SmsSenderHelper";

	public static void sendSms(ContentResolver cr, String phone, SmsMessageActionEnum action, Bundle params) {
		Log.d(TAG, String.format("Send Request SmsMessage to %s : %s", phone, action));
		String encrypedMsg = SmsMessageIntentEncoderHelper.encodeSmsMessage(action, params);
		Log.d(TAG, String.format("Send Request SmsMessage to %s : %s", phone, encrypedMsg));
		if (encrypedMsg != null && encrypedMsg.length() > 0 && encrypedMsg.length() <= AppConstants.SMS_MAX_SIZE) {
			SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, null, null);
			Log.d(TAG, String.format("Send SmsMessage (%s chars, args) : %s", encrypedMsg.length(), encrypedMsg));
			// Log It
			logSmsMessage(cr, SmsLogTypeEnum.SEND, phone, action, params, 1);
		} else {
			Log.e(TAG, String.format("Too long SmsMessage (%s chars, args) : %s", encrypedMsg.length(), encrypedMsg));
		}
	}

	// ===========================================================
	// Log Sms message
	// ===========================================================
 
	public static void logSmsMessage(ContentResolver cr, SmsLogTypeEnum type, String phone, SmsMessageActionEnum action, Bundle params, int smsWeight) {
		ContentValues values = SmsLogHelper.getContentValues(type, phone, action, params);
		values.put(SmsLogColumns.COL_SMS_WEIGHT, smsWeight);
		cr.insert(SmsLogProvider.Constants.CONTENT_URI, values);
	}
}
