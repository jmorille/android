package eu.ttbox.geoping.service.slave;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.core.VersionUtils;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.model.Pairing;
import eu.ttbox.geoping.domain.model.PairingAuthorizeTypeEnum;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.domain.pairing.PairingHelper;
import eu.ttbox.geoping.service.SmsSenderHelper;
import eu.ttbox.geoping.service.core.ContactHelper;
import eu.ttbox.geoping.service.core.ContactVo;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.slave.receiver.AuthorizePhoneTypeEnum;
import eu.ttbox.geoping.ui.person.PhotoThumbmailCache;

// http://dhimitraq.wordpress.com/tag/android-intentservice/
// https://github.com/commonsguy/cwac-wakeful
public class GeoPingSlaveService extends IntentService implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = "GeoPingSlaveService";

	private static final int SHOW_GEOPING_REQUEST_NOTIFICATION_ID = AppConstants.PER_PERSON_ID_MULTIPLICATOR * R.id.show_notification_new_geoping_request_confirm;

	private static final int SHOW_PAIRING_NOTIFICATION_ID = AppConstants.PER_PERSON_ID_MULTIPLICATOR * R.id.show_pairing_request;

	private final IBinder binder = new LocalBinder();
	// Constant

	// Services
	private NotificationManager mNotificationManager;
	private SharedPreferences appPreferences;

	// Config
	boolean displayGeopingRequestNotification = false;
	boolean authorizeNewPairing = true;

	// Set<String> secuAuthorizeNeverPhoneSet;
	// Set<String> secuAuthorizeAlwaysPhoneSet;

	// ===========================================================
	// Constructors
	// ===========================================================

	public GeoPingSlaveService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// service
		this.appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		this.mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		loadPrefConfig();
		// register listener
		appPreferences.registerOnSharedPreferenceChangeListener(this);

		Log.d(TAG, "#################################");
		Log.d(TAG, "### GeoPing Service Started.");
		Log.d(TAG, "#################################");
	}

	private void loadPrefConfig() {
		this.displayGeopingRequestNotification = appPreferences.getBoolean(AppConstants.PREFS_SHOW_GEOPING_NOTIFICATION, false);
		this.authorizeNewPairing = appPreferences.getBoolean(AppConstants.PREFS_AUTHORIZE_GEOPING_PAIRING, true);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals(AppConstants.PREFS_SHOW_GEOPING_NOTIFICATION)) {
			this.displayGeopingRequestNotification = appPreferences.getBoolean(AppConstants.PREFS_SHOW_GEOPING_NOTIFICATION, false);
		}
		if (key.equals(AppConstants.PREFS_AUTHORIZE_GEOPING_PAIRING)) {
			this.authorizeNewPairing = appPreferences.getBoolean(AppConstants.PREFS_AUTHORIZE_GEOPING_PAIRING, true);
		}
	}

	@Override
	public void onDestroy() {
		appPreferences.unregisterOnSharedPreferenceChangeListener(this);
		super.onDestroy();
		Log.d(TAG, "#################################");
		Log.d(TAG, "### GeoPing Service Destroyed.");
		Log.d(TAG, "#################################");
	}

	// ===========================================================
	// Intent Handler
	// ===========================================================

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			String action = intent.getAction();
			Log.d(TAG, "##################################");
			Log.d(TAG, String.format("onHandleIntent for action %s : %s", action, intent));
			Log.d(TAG, "##################################");
			if (Intents.ACTION_SMS_GEOPING_REQUEST_HANDLER.equals(action)) {
				// GeoPing Request
				String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
				Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);

				// Request
				// registerGeoPingRequest(phone, params);
				Pairing pairing = getPairingByPhone(phone);
				PairingAuthorizeTypeEnum authorizeType = pairing.authorizeType;
				boolean showNotification = pairing.showNotification;
				if (intent.getBooleanExtra(Intents.EXTRA_INTERNAL_BOOL, false)) {
					// Is Internal Direct Order
					showNotification = false;
					authorizeType = PairingAuthorizeTypeEnum.AUTHORIZE_ALWAYS;
					Log.i(TAG, "Internal Order, bypass user preference and Hide Notif and Authorize anyway");
					// Show Toast

				}
				switch (authorizeType) {
				case AUTHORIZE_NEVER:
					Log.i(TAG, "Ignore Geoping (Never Authorize) request from phone " + phone);
					// Show Blocking Notification
					if (showNotification) {
						showNotificationGeoPing(pairing, params, false);
					}
					break;
				case AUTHORIZE_ALWAYS:
					Log.i(TAG, "Accept Geoping (always Authorize) request from phone " + phone);
					GeoPingSlaveLocationService.runFindLocationAndSendInService(this, phone, params);
					// Display Notification GeoPing
					if (showNotification) {
						showNotificationGeoPing(pairing, params, true);
					}
					break;
				case AUTHORIZE_REQUEST:
					GeopingNotifSlaveTypeEnum type = GeopingNotifSlaveTypeEnum.GEOPING_REQUEST_CONFIRM;
					if (AppConstants.UNSET_ID == pairing.id) {
						type = GeopingNotifSlaveTypeEnum.GEOPING_REQUEST_CONFIRM_FIRST;
					}
					showNotificationNewPingRequestConfirm(pairing, params, type);
					break;
				default:
					break;
				}

			} else if (Intents.ACTION_SLAVE_GEOPING_PHONE_AUTHORIZE.equals(action)) {
				// GeoPing Pairing User ressponse
				manageNotificationAuthorizeIntent(intent.getExtras());
			} else if (Intents.ACTION_SMS_PAIRING_RESQUEST.equals(action)) {
				// GeoPing Pairing
				String phone = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
				Bundle params = intent.getBundleExtra(Intents.EXTRA_SMS_PARAMS);
				managePairingRequest(phone, params);

			}
		} finally {
			// synchronized (LOCK) {
			// sWakeLock.release();
			// }
		}
	}

	// ===========================================================
	// Pairing
	// ===========================================================

	private void managePairingRequest(String phone, Bundle params) {
		PairingAuthorizeTypeEnum authorizeType = PairingAuthorizeTypeEnum.AUTHORIZE_REQUEST;
		Log.i(TAG, "########## pairing request : " + authorizeType);
		Log.i(TAG, "########## pairing request : " + authorizeType);
		Log.i(TAG, "########## pairing request : " + authorizeType);
		Pairing pairing = getPairingByPhone(phone);
		if (pairing != null && pairing.authorizeType != null) {
			authorizeType = pairing.authorizeType;
		}
		long personId = SmsMessageLocEnum.PERSON_ID.readLong(params, -1l);
		switch (authorizeType) {
		case AUTHORIZE_ALWAYS:// Already pairing, resent the response
			doPairingPhone(pairing, PairingAuthorizeTypeEnum.AUTHORIZE_ALWAYS, personId);
			break;
		case AUTHORIZE_NEVER: // No Auhtorize it !!
			doPairingPhone(pairing, PairingAuthorizeTypeEnum.AUTHORIZE_NEVER, personId);
			break;
		case AUTHORIZE_REQUEST:
			// Open the Notification For asking Yes or fuck
			showNotificationNewPingRequestConfirm(pairing, params, GeopingNotifSlaveTypeEnum.PAIRING);
			break;
		default:
			break;
		}
	}

	private void manageNotificationAuthorizeIntent(Bundle extras) {
		// Read Intent
		String phone = extras.getString(Intents.EXTRA_SMS_PHONE);
		Bundle params = extras.getBundle(Intents.EXTRA_SMS_PARAMS);
		long personId = SmsMessageLocEnum.PERSON_ID.readLong(params, -1l);
		GeopingNotifSlaveTypeEnum notifType = GeopingNotifSlaveTypeEnum.getByOrdinal(extras.getInt(Intents.EXTRA_NOTIFICATION_TYPE_ENUM_ORDINAL, -1));
		AuthorizePhoneTypeEnum type = AuthorizePhoneTypeEnum.getByOrdinal(extras.getInt(Intents.EXTRA_AUTHORIZE_PHONE_TYPE_ENUM_ORDINAL));
		Log.d(TAG, "******* AuthorizePhoneTypeEnum : " + type);
		String personNewName = extras.getString(Intents.EXTRA_PERSON_NAME);
		// Cancel Notification
		int notifId = extras.getInt(Intents.EXTRA_NOTIF_ID, -1);
		if (notifId != -1) {
			mNotificationManager.cancel(notifId);
		}
		// Read Pairing
		Pairing pairing = getPairingByPhone(phone);
		if (TextUtils.isEmpty(pairing.name) && personNewName != null) {
			pairing.name = personNewName;
		}
		// ### Manage Pairing Type
		// #############################
		Log.d(TAG, String.format("manageAuthorizeIntent for phone %s with User security choice %s", phone, type));
		boolean positifResponse = false;
		switch (type) {
		case NEVER:
			doPairingPhone(pairing, PairingAuthorizeTypeEnum.AUTHORIZE_NEVER, personId);
			break;
		case ALWAYS:
			doPairingPhone(pairing, PairingAuthorizeTypeEnum.AUTHORIZE_ALWAYS, personId);
			positifResponse = true;
			break;
		case YES:
			positifResponse = true;
			if (AppConstants.UNSET_ID == pairing.id) {
				doPairingPhone(pairing, PairingAuthorizeTypeEnum.AUTHORIZE_REQUEST, personId);
			}
			break;
		default:
			Log.w(TAG, "Not manage PhoneAuthorizeTypeEnum for " + type);
			positifResponse = false;
			break;
		}

		// ### Manage Notification Type
		// #############################
		switch (notifType) {
		case GEOPING_REQUEST_CONFIRM:
			if (positifResponse) {
				GeoPingSlaveLocationService.runFindLocationAndSendInService(this, phone, params);
			}
			break;
		default:
			break;
		}
	}

	private void doPairingPhone(Pairing pairing, PairingAuthorizeTypeEnum authorizeType, long personId) {
		// ### Persist Pairing
		// #############################
		if (pairing.id > -1l) {
			if (pairing.authorizeType == null || !pairing.authorizeType.equals(authorizeType)) {
				// update
				ContentValues values = authorizeType.writeTo(null);
				values.put(PairingColumns.COL_PAIRING_TIME, System.currentTimeMillis());
				Uri uri = Uri.withAppendedPath(PairingProvider.Constants.CONTENT_URI, String.valueOf(pairing.id));
				int affectedRow = getContentResolver().update(uri, values, null, null);
				Log.w(TAG, String.format("Change %s Pairing %s : to new  %s", affectedRow, uri, authorizeType));
			} else {
				Log.d(TAG, String.format("Ignore Change Pairing type %s to %s", pairing.authorizeType, authorizeType));
			}
		} else {
			// Create
			pairing.setPairingTime(System.currentTimeMillis());
			ContentValues values = PairingHelper.getContentValues(pairing);
			authorizeType.writeTo(values);
			Uri pairingUri = getContentResolver().insert(PairingProvider.Constants.CONTENT_URI, values);
			if (pairingUri != null) {
				String entityId = pairingUri.getLastPathSegment();
				pairing.setId(Long.valueOf(entityId));
			}
			Log.w(TAG, String.format("Insert new Pairing %s : to new  %s", pairingUri, pairing));
		}
		// ### Send Pairing response
		// #############################
		switch (authorizeType) {
		case AUTHORIZE_NEVER:
			// TODO Check Last Send
			break;
		case AUTHORIZE_ALWAYS:
			sendPairingResponse(pairing.phone, personId, authorizeType);
			break;
		default:
			break;
		}

	}

	private void sendPairingResponse(String phone, long personId, PairingAuthorizeTypeEnum authorizeType) {
		Bundle params = null;
		if (personId != -1l) {
			params = SmsMessageLocEnum.PERSON_ID.writeToBundle(null, personId);
		}
		SmsSenderHelper.sendSms(this, phone, SmsMessageActionEnum.ACTION_GEO_PAIRING_RESPONSE, params);
	}

	// ===========================================================
	// GeoPing Security
	// ===========================================================

	private Pairing getPairingByPhone(String phoneNumber) {
		Pairing result = null;
		// Search
		// Log.d(TAG, String.format("Search Painring for Phone [%s]",
		// phoneNumber));
		Uri uri = Uri.withAppendedPath(PairingProvider.Constants.CONTENT_URI_PHONE_FILTER, Uri.encode(phoneNumber));
		Cursor cur = getContentResolver().query(uri, null, null, null, null);
		try {
			if (cur != null && cur.moveToFirst()) {
				PairingHelper helper = new PairingHelper().initWrapper(cur);
				result = helper.getEntity(cur);
			}
		} finally {
			cur.close();
		}
		Log.d(TAG, String.format("Search Painring for Phone [%s] : Found %s", phoneNumber, result));
		// Create It
		if (result == null) {
			// TODO Read Prefs values
			result = new Pairing();
			result.setPhone(phoneNumber);
			result.setShowNotification(displayGeopingRequestNotification);
			if (authorizeNewPairing) {
				result.setAuthorizeType(PairingAuthorizeTypeEnum.AUTHORIZE_REQUEST);
			} else {
				result.setAuthorizeType(PairingAuthorizeTypeEnum.AUTHORIZE_NEVER);
			}
		}
		return result;
	}

	// ===========================================================
	// Notification
	// ===========================================================

	private void showNotificationGeoPing(Pairing pairing, Bundle params, boolean authorizeIt) {
		String phone = pairing.phone;
		// Contact Name
		ContactVo contact = ContactHelper.searchContactForPhone(this, phone);
		String contactDisplayName = phone;
		Bitmap photo = null;
		if (contact != null) {
			if (contact.displayName != null && contact.displayName.length() > 0) {
				contactDisplayName = contact.displayName;
				if (TextUtils.isEmpty(pairing.name)) {
					pairing.name = contactDisplayName;
				}
			}
			PhotoThumbmailCache photoCache = ((GeoPingApplication) getApplication()).getPhotoThumbmailCache();
			photo = ContactHelper.openPhotoBitmap(this, photoCache, String.valueOf(contact.id), phone);
		}
		// Create Notifiation
		Builder notificationBuilder = new NotificationCompat.Builder(this) //
				.setDefaults(Notification.DEFAULT_ALL) //
				.setSmallIcon(R.drawable.ic_stat_notif_icon) //
				.setWhen(System.currentTimeMillis()) //
				.setAutoCancel(true) //

				.setContentText(contactDisplayName); //
		if (authorizeIt) {
			notificationBuilder.setContentTitle(getString(R.string.notif_geoping_request)); //
		} else {
			notificationBuilder.setContentTitle(getString(R.string.notif_geoping_request_blocked)); //
		}
		if (photo != null) {
			notificationBuilder.setLargeIcon(photo);
		} else {
			Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_notif_icon);
			notificationBuilder.setLargeIcon(icon);
		}
		Notification notification = notificationBuilder.build();
		notification.contentIntent = PendingIntent.getActivity(getApplicationContext(), 0, new Intent(), 0);
		// notification.number += 1;
		// Show
		// int notifId = SHOW_GEOPING_REQUEST_NOTIFICATION_ID +
		// phone.hashCode();
		// Log.d(TAG, String.format("GeoPing Notification Id : %s for phone %s",
		// notifId, phone));
		mNotificationManager.notify(SHOW_GEOPING_REQUEST_NOTIFICATION_ID, notification);
	}

	private void showNotificationNewPingRequestConfirm(Pairing pairing, Bundle params, GeopingNotifSlaveTypeEnum onlyPairing) {
		// Log.d(TAG,"******************************************************");
		// Log.d(TAG,"*****  showNotificationNewPingRequestConfirm  ****");
		// Log.d(TAG,"******************************************************");
		String phone = pairing.phone;
		String contactDisplayName = phone;
		String contactNewName = null;
		RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notif_geoping_request_register);
		// Contact Name
		ContactVo contact = ContactHelper.searchContactForPhone(this, phone);
		Bitmap photo = null;
		if (contact != null) {
			if (contact.displayName != null && contact.displayName.length() > 0) {

				contactDisplayName = contact.displayName;
				if (TextUtils.isEmpty(pairing.name)) {
					contactNewName = contact.displayName;
					pairing.name = contact.displayName;
				}
			}
			PhotoThumbmailCache photoCache = ((GeoPingApplication) getApplication()).getPhotoThumbmailCache();
			photo = ContactHelper.openPhotoBitmap(this, photoCache, String.valueOf(contact.id), phone);
		}

		// Generate Notification ID per Person
		int notifId = SHOW_GEOPING_REQUEST_NOTIFICATION_ID + phone.hashCode();
		Log.d(TAG, String.format("GeoPing Notification Id : %s for phone %s", notifId, phone));

		// Content Intent In android 2.3 no Custun View displayble
		// TODO Propose a choice
		PendingIntent contentIntent = null;

		// Service
		Resources r = getResources();
		// Title
		String title;
		String contentText = contactDisplayName + r.getString(R.string.notif_click_to_accept);
		switch (onlyPairing) {
		case PAIRING:
			notifId = SHOW_PAIRING_NOTIFICATION_ID + phone.hashCode();
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_yes, View.VISIBLE);
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_no, View.GONE);
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_never, View.VISIBLE);
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_always, View.VISIBLE);
			contentView.setTextViewText(R.id.notif_geoping_confirm_button_yes, getText(R.string.notif_confirm_request_eachtime));
			title = getString(R.string.notif_pairing);
			contentIntent = PendingIntent.getService(this, 0, //
					Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.ALWAYS, notifId, onlyPairing),//
					PendingIntent.FLAG_UPDATE_CURRENT);
			break;
		case GEOPING_REQUEST_CONFIRM:
			title = getString(R.string.notif_geoping_request);
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_yes, View.VISIBLE);
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_no, View.VISIBLE);
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_never, View.GONE);
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_always, View.GONE);
			break;
		case GEOPING_REQUEST_CONFIRM_FIRST:
			title = getString(R.string.notif_geoping_request);
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_yes, View.VISIBLE);
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_no, View.GONE);
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_always, View.VISIBLE);
			contentView.setViewVisibility(R.id.notif_geoping_confirm_button_never, View.VISIBLE);
			contentView.setTextViewText(R.id.notif_geoping_confirm_button_yes, getText(R.string.notif_confirm_request_eachtime));
			break;
		default:
			title = getString(R.string.app_name);
			break;
		}

		// View
		contentView.setTextViewText(R.id.notif_geoping_title, title);
		contentView.setTextViewText(R.id.notif_geoping_phone, contactDisplayName);
		// Pending Intent
		PendingIntent secuNo = PendingIntent.getService(this, 0, //
				Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.NO, notifId, onlyPairing),//
				PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent secuNever = PendingIntent.getService(this, 1, //
				Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.NEVER, notifId, onlyPairing),//
				PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent secuYes = PendingIntent.getService(this, 2, //
				Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.YES, notifId, onlyPairing),//
				PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent secuAlways = PendingIntent.getService(this, 3, //
				Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.ALWAYS, notifId, onlyPairing),//
				PendingIntent.FLAG_UPDATE_CURRENT);
		// Manage Button Confirmation
		contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_no, secuNo);
		contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_never, secuNever);
		contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_yes, secuYes);
		contentView.setOnClickPendingIntent(R.id.notif_geoping_confirm_button_always, secuAlways);

		// Content Intent
		if (contentIntent == null) {
			contentIntent = PendingIntent.getService(this, 0, //
					Intents.authorizePhone(this, phone, contactNewName, params, AuthorizePhoneTypeEnum.YES, notifId, onlyPairing),//
					PendingIntent.FLAG_UPDATE_CURRENT);
		}

		// Create Notifiation
		Builder notificationBuilder = new NotificationCompat.Builder(this) //
				.setDefaults(Notification.DEFAULT_ALL) //
				.setSmallIcon(R.drawable.ic_stat_notif_icon) //
				.setWhen(System.currentTimeMillis()) //
				.setAutoCancel(true) //
				.setOngoing(true) //
				.setContentTitle(title) //
				.setContentText(contentText) //
				.setTicker(title) //
				.setContentIntent(contentIntent); //

		// .setNumber(5) //
		// Content Value
		if (VersionUtils.isJb16) {
			// Jb
			NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(notificationBuilder);
			style.addLine(contactDisplayName) //
			// .setSummaryText("this is the summary")//
			;
			notificationBuilder.setStyle(style);
			// Add Action
			notificationBuilder.setDeleteIntent(secuNo);

			switch (onlyPairing) {
			case PAIRING:
				notificationBuilder.addAction(R.drawable.ic_cadenas_ferme_rouge, r.getString(R.string.notif_pairing_never), secuNever);
				notificationBuilder.addAction(R.drawable.ic_cadenas_entrouvert_jaune, r.getString(android.R.string.yes), secuYes);
				notificationBuilder.addAction(R.drawable.ic_cadenas_ouvert_vert, r.getString(R.string.notif_pairing_always), secuAlways);
				break;
			case GEOPING_REQUEST_CONFIRM:
				notificationBuilder.addAction(R.drawable.ic_menu_nav_accept, r.getString(android.R.string.yes), secuYes);
				notificationBuilder.addAction(R.drawable.ic_menu_nav_cancel, r.getString(android.R.string.no), secuNo);
				break;
			case GEOPING_REQUEST_CONFIRM_FIRST:
				notificationBuilder.addAction(R.drawable.ic_cadenas_ferme_rouge, r.getString(R.string.notif_pairing_never), secuNever);
				notificationBuilder.addAction(R.drawable.ic_menu_nav_accept, r.getString(android.R.string.yes), secuYes);
				notificationBuilder.addAction(R.drawable.ic_cadenas_ouvert_vert, r.getString(R.string.notif_pairing_always), secuAlways);
				break;
			}
			// notificationBuilder.addAction(R.drawable.ic_cadenas_ferme_rouge,
			// r.getString(R.string.notif_pairing_never), secuNever);
			// notificationBuilder.addAction(R.drawable.ic_menu_nav_cancel,
			// r.getString(android.R.string.no), secuNo);
			// notificationBuilder.addAction(R.drawable.ic_menu_nav_accept,
			// r.getString(android.R.string.yes), secuYes);
			// notificationBuilder.addAction(R.drawable.ic_cadenas_ouvert_vert,
			// r.getString(R.string.notif_pairing_always), secuAlways);
			// Tocker

		} else {
			// Ics, Hb, ginger
			notificationBuilder.setContent(contentView);
		}

		if (photo != null) {
			notificationBuilder.setLargeIcon(photo);
		} else {
			Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_notif_icon);
			notificationBuilder.setLargeIcon(icon);
		}
		Notification notification = notificationBuilder.build();
		// notification.contentIntent = contentIntent;
		// notification.contentView = contentView;
		// notification.flags = Notification.FLAG_ONGOING_EVENT |
		// Notification.FLAG_ONLY_ALERT_ONCE;
		// notification.flags = Notification.FLAG_SHOW_LIGHTS;
		// Show
		mNotificationManager.notify(notifId, notification);
	}

	// ===========================================================
	// Binder
	// ===========================================================

	public class LocalBinder extends Binder {
		public GeoPingSlaveService getService() {
			return GeoPingSlaveService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

}
