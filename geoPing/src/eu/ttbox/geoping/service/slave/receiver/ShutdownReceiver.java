package eu.ttbox.geoping.service.slave.receiver;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.telephony.SmsManager;
import android.util.Log;
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

    boolean isSend = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        String phone = "0777048649";
        String action = intent.getAction();
        if (ACTION_BOOT_COMPLETED.equals(action)) {
            String encrypedMsg = "Mon tel ACTION_BOOT_COMPLETED";
            Log.d(TAG, "### ############################### ### ");
            Log.d(TAG, "### ### " + encrypedMsg + " ### ### ");
            Log.d(TAG, "### ############################### ### ");
            SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, null, null);
        } else if (ACTION_SHUTDOWN.equals(action) || QUICKBOOT_POWEROFF.equals(action)) {
            String encrypedMsg = "Mon tel ACTION_SHUTDOWN : " + action;
            Log.d(TAG, "### ############################### ### ");
            Log.d(TAG, "### ### " + encrypedMsg + " ### ### " + System.currentTimeMillis());
            Log.d(TAG, "### ############################### ### ");
            // Register Receiver
            // Register broadcast receivers for SMS sent and delivered intents

            // Send SMS
            PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_SMS_SENT), 0);
            isSend = true;
            SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, sentIntent, null);
            try {
                Thread.sleep(5000);
                Log.d(TAG, "### ### End Thread Sleep 5s ### ### ");
            } catch (InterruptedException e) {
                Log.d(TAG, "### ### Error Thread Sleep 5s ### ### " + e.getMessage());
                e.printStackTrace();
            }

        } else if (ACTION_SMS_SENT.equals(action)) {
            String message = null;
            boolean error = true;
            switch (getResultCode()) {
            case Activity.RESULT_OK:
                Log.d(TAG, "### ############################### ### " + isSend);
                Log.d(TAG, "### Message sent !!!               # ### " + System.currentTimeMillis());
                Log.d(TAG, "### ############################### ### ");
                message = "Message sent!";
                error = false;
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                message = "Error.";
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                message = "Error: No service.";
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                message = "Error: Null PDU.";
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                message = "Error: Radio off.";
                break;
            }
        }
    }

    private void loadPhoneNotif(Context context, String notifCol) {
        String[] projection = new String[] { PairingColumns.COL_PHONE };
        String selection = String.format("%s = ?", notifCol);
        Cursor cursor = context.getContentResolver().query(PairingProvider.Constants.CONTENT_URI, projection, selection, new String[] { "1" }, null);
        try {
            if (cursor.moveToFirst()) {
                do {
                    
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
