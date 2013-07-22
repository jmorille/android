package eu.ttbox.geoping.service.slave.eventspy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;

public class ShutdownReceiver extends BroadcastReceiver {

	private static final String TAG = "ShutdownReceiver";

	// http://developer.android.com/reference/android/content/Intent.html#ACTION_BOOT_COMPLETED
	private static final String ACTION_BOOT_COMPLETED = "android.intent.action.ACTION_BOOT_COMPLETED";
	public static final String ACTION_SMS_SENT = "eu.ttbox.geoping.ShutdownReceiver.ACTION_SMS_SENT";

	/**
	 * To be more specific, if you choose Restart, ACTION_SHUTDOWN is broadcast,
	 * but if you choose Power Off, QUICKBOOT_POWEROFF is broadcast instead.
	 **/
	private static final String QUICKBOOT_POWEROFF = "android.intent.action.QUICKBOOT_POWEROFF";
	private static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		if (ACTION_SHUTDOWN.equals(action) || QUICKBOOT_POWEROFF.equals(action)) {
			String encrypedMsg = "Mon tel vient de s'éteindre : " + action;
			Log.d(TAG, "### ############################### ### ");
			Log.d(TAG, "### ### EventSpy SHUTDOWN : " + encrypedMsg + " ### ### ");
			Log.d(TAG, "### ############################### ### ");
			// Search Phones 
			String[] phones= SpyNotificationHelper.searchListPhonesForNotif(context, PairingColumns.COL_NOTIF_SHUTDOWN);
			if (phones != null) {
			    Bundle params = new Bundle();
			    // Send Sms
			    SpyNotificationHelper.sendEventSpySmsMessage(context,phones,  MessageActionEnum.SPY_SHUTDOWN, params);
 				// Sleep for Send the Sms
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				int sleepWantedInMs = prefs.getInt(AppConstants.PREFS_EVENT_SPY_SHUTDOWN_SLEEP_IN_MS, 5000);
				try {
					Log.d(TAG, "EventSpy SHUTDOWN : Begin Thread Sleep " + sleepWantedInMs + " ms ### ### ");
					Thread.sleep(sleepWantedInMs);
					Log.d(TAG, "EventSpy SHUTDOWN : End Thread Sleep " + sleepWantedInMs + " ms ### ### ");
				} catch (InterruptedException e) {
					Log.e(TAG, "EventSpy SHUTDOWN :  Error Thread Sleep " + sleepWantedInMs + " ms ### ### " + e.getMessage());
				}
			}
		}
	}

}
