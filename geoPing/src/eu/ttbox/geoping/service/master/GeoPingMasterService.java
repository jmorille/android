package eu.ttbox.geoping.service.master;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.MainActivity;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.SmsLogProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.core.ContactHelper;
import eu.ttbox.geoping.service.core.ContactVo;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageIntentEncoderHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;

public class GeoPingMasterService extends IntentService {

	private static final String TAG = "GeoPingMasterService";

	private static final int SHOW_ON_NOTIFICATION_ID = AppConstants.PER_PERSON_ID_MULTIPLICATOR * R.id.show_notification_new_geoping_response;

	private final IBinder binder = new LocalBinder();

	// Service
	private SharedPreferences appPreferences;
	private GoogleAnalyticsTracker tracker;

	// config
	boolean notifyGeoPingResponse = false;

	// ===========================================================
	// UI Handler
	// ===========================================================

	private static final int UI_MSG_TOAST = 0;

	private Handler uiHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UI_MSG_TOAST:
				String msgText = (String) msg.obj;
				Toast.makeText(getApplicationContext(), msgText, Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			} 
		}
	};

	// ===========================================================
	// Constructors
	// ===========================================================

	public GeoPingMasterService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// service
		this.appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		this.notifyGeoPingResponse = appPreferences.getBoolean(AppConstants.PREFS_SHOW_GEOPING_NOTIFICATION, false);
		// Google Analytics
		tracker = ((GeoPingApplication) getApplication()).getTracker();

		Log.d(TAG, "#################################");
		Log.d(TAG, "### GeoPingMasterService Service Started.");
		Log.d(TAG, "#################################");
	}

	// ===========================================================
	// Binder
	// ===========================================================

	public class LocalBinder extends Binder {
		public GeoPingMasterService getService() {
			return GeoPingMasterService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	// ===========================================================
	// Intent Handler
	// ===========================================================

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		Log.d(TAG, String.format("onHandleIntent for action %s : %s", action, intent));
		if (Intents.ACTION_SMS_GEOPING_REQUEST_SENDER.equals(action)) {
			String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
			Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
			sendSmsGeoPingRequest(phone, params);
			// Tracker
			tracker.trackPageView("/action/SMS_GEOPING_REQUEST");
		} else if (Intents.ACTION_SMS_PAIRING_RESQUEST.equals(action)) {
			String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
			long userId = intent.getLongExtra(Intents.EXTRA_SMS_USER_ID, -1);
			sendSmsPairingRequest(phone, userId);
			// Tracker
			tracker.trackPageView("/action/SMS_PAIRING_RESQUEST");
		} else if (Intents.ACTION_SMS_GEOPING_RESPONSE_HANDLER.equals(action)) {
			consumeGeoPingResponse(intent.getExtras());
			// Tracker
			tracker.trackPageView("/action/SMS_GEOPING_RESPONSE");
		} else if (Intents.ACTION_SMS_PAIRING_RESPONSE.equals(action)) {
			String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
			Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
			long userId = SmsMessageLocEnum.PARAM_PERSON_ID.readLong(params, -1);
			consumeSmsPairingResponse(phone, userId);
			// Tracker
			tracker.trackPageView("/action/SMS_PAIRING_RESPONSE");
		}

	}

	// ===========================================================
	// Send GeoPing Request
	// ===========================================================

	private void consumeSmsPairingResponse(String phone, long userId) {
		if (userId != -1l) {
			// Search Phone
			String personPhone = null;
			Uri uri = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI, String.valueOf(userId));
			String[] cols = new String[] { PersonColumns.COL_PHONE };
			Cursor cur = getContentResolver().query(uri, cols, null, null, null);
			try {
				if (cur != null && cur.moveToFirst()) {
					personPhone = cur.getString(1);
				}
			} finally {
				cur.close();
			}
			Log.d(TAG, String.format("Paring response for person Id : %s with phone [%s] =?= Sms [%s]", userId, personPhone, phone));
			// Update
			if (personPhone == null || !personPhone.equals(phone)) {
				ContentValues values = new ContentValues(1);
				values.put(PersonColumns.COL_PHONE, phone);
				values.put(PersonColumns.COL_PAIRING_TIME, System.currentTimeMillis());
				getContentResolver().update(uri, values, null, null);
			}
		} else {
			Log.w(TAG, "Paring response Canceled for no userId");
		}

	}

	// ===========================================================
	// Sender Sms message
	// ===========================================================

	private void sendSmsPairingRequest(String phone, long userId) {
		Bundle params = SmsMessageLocEnum.PARAM_PERSON_ID.writeToBundle(null, userId);
		sendSms(phone, SmsMessageActionEnum.ACTION_GEO_PAIRING, params);

	}

	private void sendSmsGeoPingRequest(String phone, Bundle params) {
		sendSms(phone, SmsMessageActionEnum.GEOPING_REQUEST, params);
		Log.d(TAG, String.format("Send SMS GeoPing %s : %s", phone, params));
		// Display Notif
		// final String formatStr =
		// getResources().getString(R.string.toast_notif_sended_geoping_request,
		// phone);
		// Toast.makeText(getApplicationContext(),formatStr,
		// Toast.LENGTH_SHORT).show();
	}

	private void sendSms(String phone, SmsMessageActionEnum action, Bundle params) {
		String encodeddMsg = SmsMessageIntentEncoderHelper.encodeSmsMessage(action, params);
		if (encodeddMsg != null && encodeddMsg.length() > 0 && encodeddMsg.length() <= AppConstants.SMS_MAX_SIZE) {
			boolean isSend = false;
			try {
				SmsManager.getDefault().sendTextMessage(phone, null, encodeddMsg, null, null);
				isSend = true;
			
			} catch (NullPointerException e) {
				Message msg = new Message();
				msg.what = UI_MSG_TOAST;
				msg.obj = "Error in sending message to " + phone;
//				Message msg = uiHandler.obtainMessage(UI_MSG_TOAST, "Error in sending message to " + phone);
				uiHandler.sendMessage(msg);
			}
			if (isSend) {
				// Log It
				logSmsMessage(SmsLogTypeEnum.SEND, phone, action, params);
			}
		} else if (encodeddMsg != null && encodeddMsg.length() <= AppConstants.SMS_MAX_SIZE) {
			// TODO display Too long messsage
		}
	}

	// ===========================================================
	// Log Sms message
	// ===========================================================

	private void logSmsMessage(SmsLogTypeEnum type, String phone, SmsMessageActionEnum action, Bundle params) {
		ContentValues values = SmsLogHelper.getContentValues(type, phone, action, params);
		getContentResolver().insert(SmsLogProvider.Constants.CONTENT_URI, values);
	}

	// ===========================================================
	// Consume Localisation
	// ===========================================================

	private boolean consumeGeoPingResponse(Bundle bundle) {
		boolean isConsume = false;
		String phone = bundle.getString(Intents.EXTRA_SMS_PHONE);
		Bundle params = bundle.getBundle(Intents.EXTRA_SMS_PARAMS);
		GeoTrack geoTrack = GeoTrackHelper.getEntityFromBundle(params);
		geoTrack.setPhone(phone);
		if (geoTrack != null) {
			if (!geoTrack.hasPhone()) {

			}
			ContentValues values = GeoTrackHelper.getContentValues(geoTrack);
			Uri uri = getContentResolver().insert(GeoTrackerProvider.Constants.CONTENT_URI, values);
			if (uri != null) {
				Log.d(TAG, String.format("Send Broadcast Notification for New GeoTrack %s ", uri));
				// BroadCast Response
				Intent intent = Intents.newGeoTrackInserted(uri, values);
				sendBroadcast(intent);
				// Display Notification
				showNotificationGeoPing(uri, values);
			}
			isConsume = true;
		}
		return isConsume;
	}

	private Person searchPersonForPhone(String phoneNumber) {
		Person person = null;
		Log.d(TAG, String.format("Search Contact Name for Phone [%s]", phoneNumber));
		Uri uri = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI_PHONE_FILTER, Uri.encode(phoneNumber));
		Cursor cur = getContentResolver().query(uri, null, null, null, null);
		try {
			if (cur != null && cur.moveToFirst()) {
				PersonHelper helper = new PersonHelper().initWrapper(cur);
				person = helper.getEntity(cur);
			}
		} finally {
			cur.close();
		}
		return person;
	}

	// ===========================================================
	// Notification
	// ===========================================================

	@SuppressLint("NewApi")
	private void showNotificationGeoPing(Uri geoTrackData, ContentValues values) {
		String phone = values.getAsString(GeoTrackColumns.COL_PHONE);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Contact Name
		Person contact = searchPersonForPhone(phone);

		if (true) {
			// TODO Dont Search twice
			ContactVo contactSearch = ContactHelper.searchContactForPhone(this, phone);
			if (contactSearch != null) {
				Log.w(TAG, String.format("Compare Person Contact Id %s with Search Phone Contact Id %s", contact.contactId, contactSearch.id));
				contact.contactId = String.valueOf(contactSearch.id);
			}
		}
		String contactDisplayName = phone;
		Bitmap photo = null;
		if (contact != null) {
			if (contact.displayName != null && contact.displayName.length() > 0) {
				contactDisplayName = contact.displayName;
			}
			photo = ContactHelper.openPhotoBitmap(this, contact.contactId);
		}
		// Create Notif Intent response
		PendingIntent pendingIntent = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			pendingIntent = PendingIntent.getActivities(this, 0, //
					new Intent[] { new Intent(this, MainActivity.class), Intents.showOnMap(this, geoTrackData, values) }, //
					PendingIntent.FLAG_CANCEL_CURRENT);
		} else {
			pendingIntent = PendingIntent.getActivity(this, 0, //
					Intents.showOnMap(this, geoTrackData, values), //
					PendingIntent.FLAG_CANCEL_CURRENT);
		}
		// Create Notifiation
		Builder notificationBuilder = new NotificationCompat.Builder(this) //
				.setDefaults(Notification.DEFAULT_ALL) //
				.setSmallIcon(R.drawable.ic_stat_notif_icon) //
				.setWhen(System.currentTimeMillis()) //
				.setAutoCancel(true) //
				.setContentIntent(pendingIntent)//
				.setContentTitle(getString(R.string.notif_geoping)) //
				.setContentText(contactDisplayName); //
		if (photo != null) {
			notificationBuilder.setLargeIcon(photo);
		} else {
			Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_notif_icon);
			notificationBuilder.setLargeIcon(icon);
		}
		Notification notification = notificationBuilder.build();
		// Show
		int notifId = SHOW_ON_NOTIFICATION_ID + phone.hashCode();
		Log.d(TAG, String.format("GeoPing Notification Id : %s for phone %s", notifId, phone));

		mNotificationManager.notify(notifId, notification);
	}

	// ===========================================================
	// Other
	// ===========================================================

	/**
	 * {link http://www.devx.com/wireless/Article/40524/0/page/2}
	 * 
	 * @param cellID
	 * @param lac
	 * @return
	 * @throws Exception
	 */

}
