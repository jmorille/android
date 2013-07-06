package eu.ttbox.geoping.service.slave.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.crypto.encrypt.TextEncryptor;
import eu.ttbox.geoping.domain.SmsLogProvider;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.SmsSenderHelper;
import eu.ttbox.geoping.service.encoder.GeoPingMessage;
import eu.ttbox.geoping.service.encoder.helper.SmsMessageIntentEncoderHelper;
import eu.ttbox.geoping.service.receiver.MsgReceiverIntentService;

/**
 *<a href="http://www.tutos-android.com/broadcast-receiver-android">broadcast-receiver-android</a>
 *<a href="http://mobiforge.com/developing/story/sms-messaging-android">sms-messaging-android</a>
  *
 */
public class SMSReceiver extends BroadcastReceiver {

	private static final String TAG = "SMSReceiver";

	public static final String ACTION_RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";
	public static final String EXTRA_PDUS = "pdus";

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
							Log.d(TAG, "Cancel wanting abortBroadcast for unexpected Sms Message " + message.getMessageBody());
						}
					}
					if (deleteSms) {
						abortBroadcast();
					}
				}
			}
		}
	}

	// ===========================================================
	// Consume Sms message
	// ===========================================================

	private boolean consumeMessage(Context context, SmsMessage message) {
		final String messageBody = message.getMessageBody();
		final String phoneNumber = message.getDisplayOriginatingAddress();
		Log.d(TAG, "Consume SMS Geo Action : " + phoneNumber + " / " + messageBody);
		// Decrypt Msg
		TextEncryptor textEncryptor = null;
		if (SmsMessageIntentEncoderHelper.isGeoPingEncodedSmsMessageEncrypted(messageBody)) {
			// TODO Find Encryptor
		}
		GeoPingMessage geoMsg = SmsMessageIntentEncoderHelper.decodeAsGeoPingMessage(context, phoneNumber, messageBody, textEncryptor);
		boolean isConsume = SmsMessageIntentEncoderHelper.startServiceGeoPingMessageAsIntent(context, geoMsg);
		Log.d(TAG, "is Consume SMS (" + isConsume + ") Geo Action : " + phoneNumber + " / " + messageBody);
		if (isConsume) {
			// Log It
			logSmsMessageReceive(context, geoMsg,    messageBody );
		}
		return isConsume;
	}

	private boolean consumeMessageV2(Context context, SmsMessage message) {
		final String messageBody = message.getMessageBody();
		final String phoneNumber = message.getDisplayOriginatingAddress();
		Log.d(TAG, "Consume SMS Geo Action : " + phoneNumber + " / " + messageBody);
		// Is GeoPing Msg
		boolean isGeoPingmsg = false;
		if (SmsMessageIntentEncoderHelper.isGeoPingEncodedSmsMessageEncrypted(messageBody) || SmsMessageIntentEncoderHelper.isGeoPingEncodedSmsMessageEncrypted(messageBody)) {
			isGeoPingmsg = true;
			Intent intent = new Intent(context, MsgReceiverIntentService.class).setAction(Intents.ACTION_SMS_GEOPING_ARRIVED);
			intent.putExtra(Intents.EXTRA_SMS_GEOPING_ARRIVED, messageBody);
			intent.putExtra(Intents.EXTRA_SMS_PHONE, phoneNumber);
			MsgReceiverIntentService.runIntentInService(context, intent);
		}
		return isGeoPingmsg;
	}

	// ===========================================================
	// Log Sms message
	// ===========================================================

	private void logSmsMessageReceive(Context context,     GeoPingMessage geoMsg,  String messageBody ) {
		// Save
		ContentResolver cr = context.getContentResolver(); 
		SmsLogSideEnum side = geoMsg.action.isMasterConsume ? SmsLogSideEnum.MASTER : SmsLogSideEnum.SLAVE;
		Uri insertUri = SmsSenderHelper.logSmsMessage( cr,side,   SmsLogTypeEnum.RECEIVE, geoMsg, 1,    messageBody );
		Log.d(TAG, "Save Log Message : " + insertUri); 
		// Multi Message
		if (geoMsg.isMultiMessages()) {
			String logParentId = null;
			boolean isParent = false;
			if (insertUri != null) {
				logParentId = insertUri.getLastPathSegment();
				isParent = true;
			}
			// Add Count for 0 msg
			for (GeoPingMessage msgOther : geoMsg.multiMessages) {
				ContentValues valuesOther = SmsLogHelper.getContentValues(side,   SmsLogTypeEnum.RECEIVE, msgOther);
				valuesOther.put(SmsLogColumns.COL_MSG_COUNT, 0);
  				if (isParent) {
					valuesOther.put(SmsLogColumns.COL_PARENT_ID, logParentId);
				}
				cr.insert(SmsLogProvider.Constants.CONTENT_URI, valuesOther);
			}
		}
	}

}
