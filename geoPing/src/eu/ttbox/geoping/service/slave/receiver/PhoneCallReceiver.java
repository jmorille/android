package eu.ttbox.geoping.service.slave.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneCallReceiver extends BroadcastReceiver {

	private static final String TAG = "PhoneCallReceiver";

	 
	private static final String ACTION_PHONE_STATE_CHANGED = TelephonyManager.ACTION_PHONE_STATE_CHANGED;
	private static final String ACTION_NEW_OUTGOING_CALL ="android.intent.action.NEW_OUTGOING_CALL";
	
	// Extras Incoming
	private static final String  EXTRA_STATE = "state";
	private static final String  EXTRA_INCOMING_NUMBER  ="incoming_number";

	// Extras Outgoing
	private static final String  EXTRA_OUTGOING_NUMBER  ="android.intent.extra.PHONE_NUMBER";
	
	// Incomming State
	private static final String  STATE_RINGING = "RINGING"; // Sonne
 	private static final String  STATE_OFFHOOK = "OFFHOOK"; // Decrocher
 	private static final String  STATE_IDLE	 = "IDLE"; // Racroche
 	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ACTION_PHONE_STATE_CHANGED.equals(action)) {
			Log.d(TAG,  "PhoneState action : " + action);
			Bundle extras = intent.getExtras();
			printExtras(extras);
			String state = extras.getString(EXTRA_STATE);
			if (STATE_RINGING.equals(state)) {
				String phoneNumber = extras.getString(EXTRA_INCOMING_NUMBER);
				Log.d(TAG, "PhoneState incomming call : " + phoneNumber);
			} else if (STATE_OFFHOOK.equals(state)) {
				// Decrocher
			} else if (STATE_IDLE.equals(state)) {
				// Racroche ou ignore 
			}
		}  else if (ACTION_NEW_OUTGOING_CALL.equals(action)) {
			Log.d(TAG,  "PhoneState action : " + action);
			Bundle extras = intent.getExtras();
			printExtras(extras);
			// String 
			String composePhoneNumber = extras.getString(EXTRA_OUTGOING_NUMBER);
			Log.d(TAG, "PhoneState compose PhoneNumber : " + composePhoneNumber);
		}  

	}

	
	private void printExtras(Bundle extras) {
		for (String key : extras.keySet()) {
			String value = extras.getString(key);
			Log.d(TAG, "PhoneState extras : " + key + " = " + value);
		}
	}

}
