package eu.ttbox.geoping.service.slave.receiver;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.service.encoder.params.SmsValueEventTypeEnum;

public class LowBatteryReceiver extends BroadcastReceiver {

	private static final String TAG = "LowBatteryReceiver";

	private static final String ACTION_BATTERY_LOW = "android.intent.action.ACTION_BATTERY_LOW";
	private static final String ACTION_BATTERY_OKAY = "android.intent.action.ACTION_BATTERY_OKAY";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ACTION_BATTERY_LOW.equals(action)) { 
			ArrayList<String> phones= SpyNotificationHelper.searchListPhonesForNotif(context, PairingColumns.COL_NOTIF_BATTERY_LOW);
			if (phones != null) { 
			    // Send Sms
                SpyNotificationHelper.sendEventSpySmsMessage(context,phones,  SmsValueEventTypeEnum.LOW_BATTERY);
			}
		}  

	}

}
