package eu.ttbox.geoping.service.slave.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

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
        String phone = null;//"0777048649";
        if (intent.getAction().equals(ACTION_BOOT_COMPLETED)) {
            String encrypedMsg = "Mon tel ACTION_BOOT_COMPLETED";
            Log.d(TAG, "### ############################### ### ");
            Log.d(TAG, "### ### " + encrypedMsg+  " ### ### ");
            Log.d(TAG, "### ############################### ### ");
             SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, null, null);
        } else if (intent.getAction().equals(ACTION_SHUTDOWN)) {
            String encrypedMsg = "Mon tel ACTION_SHUTDOWN";
            Log.d(TAG, "### ############################### ### ");
            Log.d(TAG, "### ### " + encrypedMsg+  " ### ### ");
            Log.d(TAG, "### ############################### ### ");
            SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, null, null);
        }
    }
}
