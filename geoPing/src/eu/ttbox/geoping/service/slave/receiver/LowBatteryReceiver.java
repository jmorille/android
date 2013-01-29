package eu.ttbox.geoping.service.slave.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;

public class LowBatteryReceiver extends BroadcastReceiver {

	private static final String TAG = "LowBatteryReceiver";

	private static final String ACTION_BATTERY_LOW = "android.intent.action.ACTION_BATTERY_LOW";
	private static final String ACTION_BATTERY_OKAY = "android.intent.action.ACTION_BATTERY_OKAY";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ACTION_BATTERY_LOW.equals(action)) {
			String encrypedMsg = "Mon tel  a peu de Batteyr: " + action;
			String phone = SpyNotificationHelper.searchPhoneForNotif(context, PairingColumns.COL_NOTIF_BATTERY_LOW);
			Log.d(TAG, "### ### Destination : " + phone + " ### ### ");
			if (phone != null) { 
				SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, null, null);
			}
		}  

	}

}
