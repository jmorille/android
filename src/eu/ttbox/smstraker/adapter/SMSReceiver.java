package eu.ttbox.smstraker.adapter;

import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;
import eu.ttbox.smstraker.core.AppConstant;
import eu.ttbox.smstraker.domain.TrackPoint;
import eu.ttbox.smstraker.domain.TrackingBDD;

/**
 * @see http://www.tutos-android.com/broadcast-receiver-android
 * @author deostem
 * 
 */
public class SMSReceiver extends BroadcastReceiver {

	private final String ACTION_RECEIVE_SMS = "android.provider.Telephony.SMS_RECEIVED";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION_RECEIVE_SMS)) {
			Bundle bundle = intent.getExtras();
			if (bundle != null) {
				Object[] pdus = (Object[]) bundle.get("pdus");

				final SmsMessage[] messages = new SmsMessage[pdus.length];
				for (int i = 0; i < pdus.length; i++) {
					messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
				}
				if (messages.length > 0) {
					SharedPreferences appPreferences = PreferenceManager.getDefaultSharedPreferences(context);
					boolean deleteSms = appPreferences.getBoolean("smsMonitorDelete", false);
					// context.getSharedPreferences("smsMonitorDelete",
					// Context.MODE_PRIVATE).getBoolean(key, defValue);
					for (SmsMessage message : messages) {
						final String messageBody = message.getMessageBody();
						final String phoneNumber = message.getDisplayOriginatingAddress();
						Location loc = SmsLocationHelper.fromSmsMessage(messageBody);
						if (loc != null) {
							manangeNewLocation(context, phoneNumber, loc);
						} else {
							if (deleteSms) {
								Log.w(getClass().getSimpleName(), "Cancel wanting abortBroadcast for unexpected Sms Message " + message.getMessageBody());
							}
							deleteSms = false;
						}
					}
					if (deleteSms) {
						abortBroadcast();
					}

				}
			}
		}

	}

	private void manangeNewLocation(Context context, String phoneNumber, Location loc) {
		if (loc != null) {
			TrackPoint geoPoint = new TrackPoint(phoneNumber, loc);
			TrackingBDD trackingBDD = new TrackingBDD(context);
			trackingBDD.open();
			trackingBDD.insertTrackPoint(geoPoint);
			trackingBDD.close();
			Toast.makeText(context, "Message : " + new Date(loc.getTime()).toLocaleString() + " (" + loc.getLatitude() + "," + loc.getLongitude() + " ~ " + loc.getAccuracy() + ") from " + phoneNumber, Toast.LENGTH_LONG)
					.show();
		}

	}

}
