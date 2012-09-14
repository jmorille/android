package eu.ttbox.geoping.service.request.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ShutdownReceiver extends BroadcastReceiver {

    private static final String TAG = "ShutdownReceiver";

    // http://developer.android.com/reference/android/content/Intent.html#ACTION_BOOT_COMPLETED
    private final String ACTION_BOOT_COMPLETED = "android.intent.action.ACTION_BOOT_COMPLETED";

    /**
     * To be more specific, if you choose Restart, ACTION_SHUTDOWN is broadcast,
     * but if you choose Power Off, QUICKBOOT_POWEROFF is broadcast instead.
     **/
    private final String QUICKBOOT_POWEROFF = "android.intent.action.QUICKBOOT_POWEROFF";
    private final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {

        } else if (intent.getAction().equals(ACTION_SHUTDOWN)) {

        }
    }
}
