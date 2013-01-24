package eu.ttbox.geoping.service.slave.receiver;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;

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
		if (ACTION_BOOT_COMPLETED.equals(action)) {
			String encrypedMsg = "Mon tel Viens de démarrer : " + ACTION_BOOT_COMPLETED;
			Log.d(TAG, "### ############################### ### ");
			Log.d(TAG, "### ### " + encrypedMsg + " ### ### ");
			Log.d(TAG, "### ############################### ### ");
			// Search Phones
			String phone = searchPhoneForNotif(context, PairingColumns.COL_NOTIF_SHUTDOWN);
			if (phone != null) {
				SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, null, null);
			}
		} else if (ACTION_SHUTDOWN.equals(action) || QUICKBOOT_POWEROFF.equals(action)) {
			String encrypedMsg = "Mon tel viens de s'éteindre : " + action;
			Log.d(TAG, "### ############################### ### ");
			Log.d(TAG, "### ### " + encrypedMsg + " ### ### ");
			Log.d(TAG, "### ############################### ### ");
			// Search Phones
			String phone = searchPhoneForNotif(context, PairingColumns.COL_NOTIF_SHUTDOWN);
			Log.d(TAG, "### ### Destination : " + phone + " ### ### ");
			if (phone != null) {
				// Send SMS
				SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, null, null);
				// Sleep for Send the Sms
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
				int sleepWantedInMs =   prefs.getInt(AppConstants.PREFS_SPY_NOTIFICATION_SHUTDOWN_SLEEP_IN_MS, 5000);
				try {
					Log.d(TAG, "### ### Begin Thread Sleep " + sleepWantedInMs + " ms ### ### ");
					Thread.sleep(sleepWantedInMs);
					Log.d(TAG, "### ### End Thread Sleep " + sleepWantedInMs + " ms ### ### ");
				} catch (InterruptedException e) {
					Log.e(TAG, "### ### Error Thread Sleep " + sleepWantedInMs + " ms ### ### " + e.getMessage());
				}
			}
		}
	}

	private String searchPhoneForNotif(Context context, String notifCol) {
		String[] projection = new String[] { PairingColumns.COL_PHONE };
		String selection = String.format("%s = 1", notifCol);
		Cursor cursor = context.getContentResolver().query(PairingProvider.Constants.CONTENT_URI, projection, selection, null, null);
		Log.d(TAG, "Search Pairing for criteria : " + selection + " ==> " + cursor.getCount() + " result");
		StringBuffer sb = new StringBuffer();
		boolean isNotFirst = false;
		try {
			while (cursor.moveToNext()) {
				if (isNotFirst) {
					sb.append(';');
				}
				String phone = cursor.getString(0);
				sb.append(phone);
				isNotFirst = true;
			}
		} finally {
			cursor.close();
		}
		// Result
		if (isNotFirst) {
			return sb.toString();
		}
		return null;

	}

}
