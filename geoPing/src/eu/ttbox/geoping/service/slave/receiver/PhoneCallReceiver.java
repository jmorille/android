package eu.ttbox.geoping.service.slave.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneCallReceiver extends BroadcastReceiver {

	private static final String TAG = "LowBatteryReceiver";

	private static final String PHONE_ANSWER = "android.intent.action.ANSWER";

	private static final String ACTION_PHONE_STATE_CHANGED = TelephonyManager.ACTION_PHONE_STATE_CHANGED;

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ACTION_PHONE_STATE_CHANGED.equals(action)) {
			Log.d(TAG,  "PhoneState action : " + action);
			Bundle extras = intent.getExtras();
			printExtras(extras);
		} else if (PHONE_ANSWER.equals(action)) {

		}

	}

	
	private void printExtras(Bundle extras) {
		for (String key : extras.keySet()) {
			String value = extras.getString(key);
			Log.d(TAG, "PhoneState extras : " + key + " = " + value);
		}
	}

}
