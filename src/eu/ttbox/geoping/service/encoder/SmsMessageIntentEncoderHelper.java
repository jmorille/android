package eu.ttbox.geoping.service.encoder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.core.Intents;

public class SmsMessageIntentEncoderHelper {

	public static final String TAG = "SmsMessageIntentEncoderHelper";

	// ===========================================================
	// Encoder
	// ===========================================================
	public static String encodeSmsMessage(SmsMessageActionEnum action, Bundle params) {
		String encrypedMsg = SmsMessageEncoderHelper.encodeSmsMessage(action.smsAction, params);
		return encrypedMsg;
	}

	// ===========================================================
	// Decoder
	// ===========================================================

	
	public static Intent decodeAsIntent(Context context, String phone, String encryped) {
		GeoPingMessage clearMsg = SmsMessageEncoderHelper.decodeSmsMessage(phone, encryped);
		Intent intent = convertForIntentGeoPingMessage(context, clearMsg);
		return intent;
	}

	public static GeoPingMessage decodeAsGeoPingMessage(Context context, String phone, String encryped) {
		return  SmsMessageEncoderHelper.decodeSmsMessage(phone, encryped);
	}
	
	
	public static Intent convertForIntentGeoPingMessage(Context context, GeoPingMessage msg) {
		SmsMessageActionEnum action = msg.action;
		if (msg==null || action == null) {
			Log.w(TAG, String.format("Ignore for No Action the GeoPingMessage : %s", msg));
			return null;
		}
		Log.d(TAG, String.format("Create Intent from %s", msg));
		Intent intent = new Intent(context, action.serviceClass) //
				.setAction(action.intentAction);//
		if (msg.params != null && !msg.params.isEmpty()) {
			intent.putExtra(Intents.EXTRA_SMS_PARAMS, msg.params);
		}
		intent.putExtra(Intents.EXTRA_SMS_ACTION, msg.action);
		intent.putExtra(Intents.EXTRA_SMS_PHONE, msg.phone); //
		return intent;
	}
}
