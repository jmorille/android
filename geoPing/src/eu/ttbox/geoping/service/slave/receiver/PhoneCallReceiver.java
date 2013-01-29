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

	private static final String  EXTRA_STATE = "state";
	private static final String  EXTRA_INCOMING_NUMBER  ="incoming_number";

	
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
		}  

	}

	
	private void printExtras(Bundle extras) {
		for (String key : extras.keySet()) {
			String value = extras.getString(key);
			Log.d(TAG, "PhoneState extras : " + key + " = " + value);
		}
	}

}
