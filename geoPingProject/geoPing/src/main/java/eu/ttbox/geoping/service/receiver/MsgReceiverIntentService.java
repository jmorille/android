package eu.ttbox.geoping.service.receiver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import eu.ttbox.geoping.core.Intents;

@Deprecated
public class MsgReceiverIntentService extends IntentService {

	private static final String TAG = "MsgReceiverIntentService";

	public MsgReceiverIntentService() {
		super(TAG);
	}

	private static PowerManager.WakeLock sWakeLock;
	private static final Object[] LOCK = new Object[0];

	public static void runIntentInService(Context context, Intent intent) {
		synchronized (LOCK) {
			if (sWakeLock == null) {
				PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				sWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "geoping_msg_wakelock");
			}
		}
		sWakeLock.acquire();
		intent.setClassName(context, MsgReceiverIntentService.class.getName());
		context.startService(intent);
	}

	@Override
	public final void onHandleIntent(Intent intent) {
		try {
			String action = intent.getAction();
			if (Intents.ACTION_SMS_GEOPING_ARRIVED.equals(action)) {
				handleSms(intent);
			} else
			// GCM
			if (action.equals("com.google.android.c2dm.intent.REGISTRATION")) {
				handleRegistration(intent);
			} else if (action.equals("com.google.android.c2dm.intent.RECEIVE")) {
				// handleMessage(intent);
			}
		} finally {
			synchronized (LOCK) {
				sWakeLock.release();
			}
		}
	}
	
	// ===========================================================
	// Sms Decoder
	// ===========================================================


	private void handleSms(Intent intent) {

		
	}
	
	// ===========================================================
	// GCM
	// ===========================================================


	private void handleRegistration(Intent intent) {
		String registrationId = intent.getStringExtra("registration_id");
		String error = intent.getStringExtra("error");
		String unregistered = intent.getStringExtra("unregistered");
		// registration succeeded
		if (registrationId != null) {
			// store registration ID on shared preferences
			// notify 3rd-party server about the registered ID
		}

		// unregistration succeeded
		if (unregistered != null) {
			// get old registration ID from shared preferences
			// notify 3rd-party server about the unregistered ID
		}

		// last operation (registration or unregistration) returned an error;
		if (error != null) {
			if ("SERVICE_NOT_AVAILABLE".equals(error)) {
				// optionally retry using exponential back-off
				// (see Advanced Topics)
			} else {
				// Unrecoverable error, log it
				Log.i(TAG, "Received error: " + error);
			}
		}
	}
}
