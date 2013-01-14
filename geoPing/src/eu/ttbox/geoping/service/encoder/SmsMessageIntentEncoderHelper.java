package eu.ttbox.geoping.service.encoder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.crypto.encrypt.TextEncryptor;

public class SmsMessageIntentEncoderHelper {

	public static final String TAG = "SmsMessageIntentEncoderHelper";

	// ===========================================================
	// Encoder
	// ===========================================================
	public static String encodeSmsMessage(SmsMessageActionEnum action, Bundle params) {
		String encrypedMsg = SmsMessageEncoderHelper.encodeSmsMessage(action, params);
		return encrypedMsg;
	}

	// ===========================================================
	// Decoder
	// ===========================================================

	public static Intent decodeAsIntent(Context context, String phone, String encryped) {
		GeoPingMessage clearMsg = SmsMessageEncoderHelper.decodeSmsMessage(phone, encryped);
		Intent intent = convertSingleGeoPingMessageAsIntent(context, clearMsg);
		return intent;
	}

	public static GeoPingMessage decodeAsGeoPingMessage(Context context, String phone, String encryped, TextEncryptor textEncryptor) {
		return SmsMessageEncoderHelper.decodeSmsMessage(phone, encryped);
	}

	public static boolean isGeoPingEncodedSmsMessageEncrypted(String encryped) {
		return SmsMessageEncoderHelper.isGeoPingEncodedSmsMessageEncrypted(encryped);
	}
	public static boolean isGeoPingEncodedSmsMessageObsuscated(String encryped) {
		return SmsMessageEncoderHelper.isGeoPingEncodedSmsMessageObsuscated(encryped);
	}
	
	// ===========================================================
	// GeoPing Service
	// ===========================================================
 

	public static boolean startServiceGeoPingMessageAsIntent(Context context, GeoPingMessage msg) {
		boolean isConsume = false;
		if (msg == null || msg.action == null) {
			Log.w(TAG, String.format("Ignore for No Action the GeoPingMessage : %s", msg));
			return isConsume;
		}
		Intent intent = startServicetSingleGeoPingMessageAsIntent(context, msg);
		if (intent != null) {
			isConsume = true;
			// Manage MultiMessages
			if (msg.isMultiMessages()) {
				for (GeoPingMessage msgOther : msg.multiMessages) {
					startServicetSingleGeoPingMessageAsIntent(context, msgOther);
				}
			}
		}
		return isConsume;
	}

	private static Intent startServicetSingleGeoPingMessageAsIntent(Context context, GeoPingMessage msg) {
		if (msg == null || msg.action == null) {
			Log.w(TAG, String.format("Ignore for No Action the GeoPingMessage : %s", msg));
			return null;
		}
		Intent intent = convertSingleGeoPingMessageAsIntent(context, msg);
//        SmsMessageActionEnum action = msg.action;
		// Managing Lock
//		if (action.serviceClass.equals(GeoPingSlaveService.class)) {
//			GeoPingSlaveService.runIntentInService(context, intent);
//		} else {
			context.startService(intent);
//		}
		return intent;
	}

	private static Intent convertSingleGeoPingMessageAsIntent(Context context, GeoPingMessage msg) {
		if (msg == null || msg.action == null) {
			Log.w(TAG, String.format("Ignore for No Action the GeoPingMessage : %s", msg));
			return null;
		}
		SmsMessageActionEnum action = msg.action;
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
