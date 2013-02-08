package eu.ttbox.geoping.service.receiver;

import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

public class MessageAcknowledgeReceiver extends BroadcastReceiver {

	private static final String TAG = "MsgQuittingAcknowledge";

	public static final String ACTION_SEND_ACK = "eu.ttbox.geoping.ACTION_SEND_ACK";
	public static final String ACTION_DELIVERY_ACK = "eu.ttbox.geoping.ACTION_DELIVERY_ACK";

	public static final String EXTRA_PDU = "pdu";
	public static final String EXTRA_FORMAT = "format";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, "Message Acknowledge action : " + action );
		printExtras(intent.getExtras());
		
		if (ACTION_SEND_ACK.equals(action)) {
			Uri logUri = intent.getData();
			Log.d(TAG, "GeoPing message Acknowledge Send : " + logUri);
			saveAcknowledge(context, intent, logUri, true);
		} else if (ACTION_DELIVERY_ACK.equals(action)) {
			Uri logUri = intent.getData();
			Log.d(TAG, "GeoPing message Acknowledge Delivery : " + logUri);
			saveAcknowledge(context, intent, logUri, false);
		}
	}

	private void printExtras(Bundle extras) {
		if (extras != null) {
			for (String key : extras.keySet()) {
				Object value = extras.get(key);
				Log.d(TAG, "Message Acknowledge extras : " + key + " = " + value);
				if (EXTRA_PDU.equals(key)) {
					SmsMessage message =SmsMessage.createFromPdu((byte[]) value);
					Log.d(TAG, "Message Acknowledge extras : " + key + " = " + message);
					Log.d(TAG, "Message Acknowledge extras : " + key + " = MessageBody : " + message.getMessageBody());
					Log.d(TAG, "Message Acknowledge extras : " + key + " = Status      : " + message.getStatus());
					Log.d(TAG, "Message Acknowledge extras : " + key + " = Ori Address : " + message.getDisplayOriginatingAddress());
					
				}
			}
		}
	}

	
	/**
	 * Do the update of the Message Acknowledge.
	 * 
	 * @param context
	 *            The context
	 * @param logUri
	 *            The Log Uri
	 * @param isSendOrDelivery
	 *            Send=true and delivery = false
	 */
	private void saveAcknowledge(Context context, Intent intent, Uri logUri, boolean isSendOrDelivery) {
		String acknowledgeType;
		if (isSendOrDelivery) {
			acknowledgeType = SmsLogColumns.COL_IS_SEND_TIME;
		} else {
			acknowledgeType = SmsLogColumns.COL_IS_DELIVERY_TIME;
		}
		// Manage Code
		String message = null;
		boolean error = true;
		int resultCode = getResultCode(); // TODO Really not in the intent ?
		switch (resultCode) {
		case Activity.RESULT_OK:
			if (isSendOrDelivery) {
				message = "Message sent!";
			} else {
				message = "Message Delivery!";
			}
			error = false;
			break;
		case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			message = "Error.";
			break;
		case SmsManager.RESULT_ERROR_NO_SERVICE:
			message = "Error: No service.";
			break;
		case SmsManager.RESULT_ERROR_NULL_PDU:
			message = "Error: Null PDU.";
			break;
		case SmsManager.RESULT_ERROR_RADIO_OFF:
			message = "Error: Radio off.";
			break;
		}
		Log.d(TAG, "### ############################### ### ");
		Log.d(TAG, "### ###  ACTION_SMS_SENT : " + message);
		Log.d(TAG, "### ############################### ### ");
		// Save Acknowledge
		ContentValues values = new ContentValues(1);
		values.put(acknowledgeType, System.currentTimeMillis());
		ContentResolver cr = context.getContentResolver();
		cr.update(logUri, values, null, null);
	}

}
