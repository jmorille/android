package eu.ttbox.geoping.service.slave.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneCallReceiver extends BroadcastReceiver {

	private static final String TAG = "LowBatteryReceiver";

	private static final String PHONE_ANSWER = "android.intent.action.ANSWER";

	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (PHONE_ANSWER.equals(action)) {

		}

	}
	
}
