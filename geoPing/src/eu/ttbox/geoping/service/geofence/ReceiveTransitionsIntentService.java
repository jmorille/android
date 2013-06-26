package eu.ttbox.geoping.service.geofence;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import eu.ttbox.geoping.MainActivity;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.GeoFenceProvider;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.domain.model.PairingAuthorizeTypeEnum;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.pairing.GeoFenceHelper;
import eu.ttbox.geoping.domain.pairing.PairingDatabase;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase;
import eu.ttbox.geoping.service.SmsSenderHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.slave.GeoPingSlaveLocationService;
import eu.ttbox.geoping.service.slave.eventspy.SpyNotificationHelper;
import eu.ttbox.osm.ui.map.mylocation.sensor.LocationUtils;

/**
 * This class receives geofence transition events from Location Services, in the
 * form of an Intent containing the transition type and geofence id(s) that triggered
 * the event.
 */
public class ReceiveTransitionsIntentService extends IntentService {

    private static final String TAG = "ReceiveTransitionsIntentService";

    // Service
    private LocationManager locationManager;

    /**
     * Sets an identifier for this class' background thread
     */
    public ReceiveTransitionsIntentService() {
        super("ReceiveTransitionsIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Service
        this.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    private void printExtras(Bundle extras) {
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d(TAG, "--- Geofence extras : " + key + " = " + value);
            }
        }
    }

    /**
     * Handles incoming intents
     *
     * @param intent The Intent sent by Location Services. This Intent is provided
     *               to Location Services (inside a PendingIntent) when you call addGeofences()
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        // Create a local broadcast Intent
        Intent broadcastIntent = new Intent();
        // Give it the category for all intents sent by the Intent Service
        broadcastIntent.addCategory(GeofenceUtils.CATEGORY_LOCATION_SERVICES);

        Log.d(TAG, "--- ------------------------------------------------------- ---");
        Log.d(TAG, "--- Geofence onHandleIntent : " + intent);
      //  printExtras(intent.getExtras());
        Log.d(TAG, "--- ------------------------------------------------------- ---");

        // First check for errors
        if (LocationClient.hasError(intent)) {
            int errorCode = LocationClient.getErrorCode(intent);
            String errorMessage = LocationServiceErrorMessages.getErrorString(this, errorCode);
            // Log the error
            Log.e(TAG, getString(R.string.geofence_transition_error_detail, errorMessage));
            // Set the action and error message for the broadcast intent
            broadcastIntent.setAction(GeofenceUtils.ACTION_GEOFENCE_ERROR)
                    .putExtra(GeofenceUtils.EXTRA_GEOFENCE_STATUS, errorMessage);

            // Broadcast the error *locally* to other components in this app
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
        } else {
            // If there's no error, get the transition type and create a notification
            // Get the type of transition (entry or exit)
            int transition = LocationClient.getGeofenceTransition(intent);

            // Test that a valid transition was reported
            if ((transition == Geofence.GEOFENCE_TRANSITION_ENTER) || (transition == Geofence.GEOFENCE_TRANSITION_EXIT)) {
                // Post a notification
                List<Geofence> geofences = LocationClient.getTriggeringGeofences(intent);
                SmsMessageActionEnum transitionType = getTransitionString(transition);
                // Send Notification
                String[] geofenceIds = extractGeofenceRequestsId(geofences);
                sendNotification(transitionType, geofenceIds);
                // Log the transition type and a message
                Log.d(TAG, "GeoFence Violation : " + transitionType + " for " + geofences.size() + " geofences (like" + geofences.get(0));
            } else {
                // An invalid transition was reported
                // Always log as an error
                Log.e(TAG, "Geofence transition error. Invalid type " + transition +
                        " in geofences %2$s");
            }
        }
    }

    private String[] extractGeofenceRequestsId(List<Geofence> geofences) {
        String[] geofenceIds = new String[geofences.size()];
        for (int index = 0; index < geofences.size(); index++) {
            Geofence geofence = geofences.get(index);
            geofenceIds[index] = geofence.getRequestId();
            Log.d(TAG, "--- Geofence offence : " + geofence);
        }
        return geofenceIds;
    }

    private List<CircleGeofence> getCircleGeofenceFromRequestIds(String[] requestIds) {
        List<CircleGeofence> result = null;
        ContentResolver cr = getContentResolver();
        Cursor cursor = cr.query(GeoFenceProvider.Constants.CONTENT_URI_REQUEST_IDS, null, null, requestIds, null);
        try {
            int cursorSize = cursor.getCount();
            if (cursorSize > 0) {
                result = new ArrayList<CircleGeofence>(cursorSize);
                GeoFenceHelper helper = new GeoFenceHelper().initWrapper(cursor);
                while (cursor.moveToNext()) {
                    CircleGeofence fence = helper.getEntity(cursor);
                    result.add(fence);
                }
            }
        } finally {
            cursor.close();
        }

        return result;
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the main Activity.
     *
     * @param transitionType The type of transition that occurred.
     */
    private void sendNotification(SmsMessageActionEnum transitionType, String[] geofenceRequestIds) {
         // Compute afected geoFence
        List<CircleGeofence> geofences = getCircleGeofenceFromRequestIds(geofenceRequestIds);
        if (geofences==null || geofences.size()<1) {
            Log.w(TAG, "No CircleGeofence in Db for request Ids : " +  Arrays.toString(geofenceRequestIds));
            return;
        }
        // Geofence Manage
        String[] phones = SpyNotificationHelper.searchListPhonesForGeofenceViolation(this, geofences, transitionType);
        if (phones!=null && phones.length>0) {
            Location lastLocation =   LocationUtils.getLastKnownLocation(locationManager);
            // TODO Compute Geofence Requests Id per User
            CircleGeofence geofenceRequest = geofences.get(0);
            sendEventSpySmsMessage(geofenceRequest,   transitionType, phones,   null,   lastLocation);
        } else {
            Log.w(TAG, "No Person assoiated to Geofences : " + geofenceRequestIds);
        }

        // Display Local Notification
        showNotification(geofences, transitionType);

    }

    private void showNotification( List<CircleGeofence> geofences, SmsMessageActionEnum transitionType  ) {
        CircleGeofence firstGeofence = geofences.get(0);
        String transitionTypeMsg = getString(transitionType.labelResourceId);

        // Create an explicit content Intent that starts the main Activity
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
        // Construct a task stack
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the main Activity to the task stack as the parent
        stackBuilder.addParentStack(MainActivity.class);
        // Push the content Intent onto the stack
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Set the notification contents
        builder.setSmallIcon(R.drawable.ic_stat_notif_icon) //
                .setWhen(System.currentTimeMillis()) //
                .setContentTitle(transitionTypeMsg) // Transition Type
                .setContentText(firstGeofence.getName()) // Zone Name
                .setContentIntent(notificationPendingIntent);

        // Message count
        if (geofences.size() > 1) {
            builder.setNumber(geofences.size());
            // BigView
            NotificationCompat.InboxStyle inBoxStyle = new NotificationCompat.InboxStyle();
            inBoxStyle.setBigContentTitle(transitionTypeMsg);
            for (CircleGeofence fence : geofences) {
                inBoxStyle.addLine(fence.getName());
            }
            builder.setStyle(inBoxStyle);
        }

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private SmsMessageActionEnum getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return SmsMessageActionEnum.GEOFENCE_ENTER;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return SmsMessageActionEnum.GEOFENCE_EXIT;
            default:
                return SmsMessageActionEnum.GEOFENCE_Unknown_transition;
        }
    }




    public void sendEventSpySmsMessage(   CircleGeofence geofenceRequest ,  SmsMessageActionEnum eventType, String[] phones, Bundle eventParams, Location location) {
        if (phones == null || phones.length < 1) {
            Log.w(TAG, "Geofence violation detected but nobody to warning");
        }
        Log.d(TAG, "EventSpy Notification  : " + eventType + " for " + phones.length + " phones destinations");
        // Send SMS
        Bundle extrasBundles = eventParams == null ? new Bundle() : eventParams;
        if (!SmsMessageLocEnum.EVT_DATE.isToBundle(extrasBundles)) {
            SmsMessageLocEnum.EVT_DATE.writeToBundle(extrasBundles, System.currentTimeMillis());
        }
        if (!extrasBundles.containsKey(SmsLogDatabase.SmsLogColumns.COL_REQUEST_ID)) {
            extrasBundles.putString(SmsLogDatabase.SmsLogColumns.COL_REQUEST_ID, geofenceRequest.requestId);
        }
        SmsMessageLocEnum.GEOFENCE_NAME.writeToBundle(extrasBundles, geofenceRequest.name);
        if (location != null) {
            // Converter Location
            GeoTrack geotrack = new GeoTrack(null, location);
            Bundle params = GeoTrackHelper.getBundleValues(geotrack);
            // Add All Specific extra values
            if (extrasBundles != null && !extrasBundles.isEmpty()) {
                params.putAll(extrasBundles);
            }
            // Not time to get GeoLoc, send it direct
            for (String phone : phones) {
                SmsSenderHelper.sendSmsAndLogIt(this, SmsLogSideEnum.SLAVE, phone, eventType, params);
            }
            // TODO saveInLocalDb
        } else {
            GeoPingSlaveLocationService.runFindLocationAndSendInService(this, eventType, phones, extrasBundles);
        }

    }
}
