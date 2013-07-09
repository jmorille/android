package eu.ttbox.geoping.service.geofence;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import eu.ttbox.geoping.MainActivity;
import eu.ttbox.geoping.R;

/**
 * Service that receives ActivityRecognition updates. It receives
 * updates in the background, even if the main Activity is not visible.
 */
public class ActivityRecognitionIntentService extends IntentService {

    private static String TAG = "ActivityRecognitionIntentService";

    public ActivityRecognitionIntentService() {
        super(TAG);
    }

    /**
     * Called when a new activity detection update is available.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        // If the incoming intent contains an update
        if (ActivityRecognitionResult.hasResult(intent)) {
            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            // Get the most probable activity
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();
            // Get the probability that this activity is the  the user's actual activity
            int confidence = mostProbableActivity.getConfidence();
            //Get an integer describing the type of activity
            int activityType = mostProbableActivity.getType();
            String activityName = getNameFromType(activityType);

            /*
             * At this point, you have retrieved all the information
             * for the current update. You can display this
             * information to the user in a notification, or
             * send it to an Activity or Service in a broadcast
             * Intent.
             */
            showNotification(confidence, activityName);
        } else {
            /*
             * This implementation ignores intents that don't contain
             * an activity update. If you wish, you can report them as
             * errors.
             */
        }
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";
        }
        return "unknown";
    }

    private void showNotification(int confidence, String activityName) {

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
                .setContentTitle("Activity Recognition") // Transition Type
                .setContentText("" + confidence + "% of " + activityName) // Zone Name
                .setContentIntent(notificationPendingIntent);

        // BigView
        NotificationCompat.InboxStyle inBoxStyle = new NotificationCompat.InboxStyle();
        inBoxStyle.setBigContentTitle("Activity Recognition");
        inBoxStyle.addLine("" + confidence + "% of " + activityName);
        builder.setStyle(inBoxStyle);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }
}
