package eu.ttbox.geoping.service.slave.receiver;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;

public class BootCompleteReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompleteReceiver";

    public static final String ACTION_SMS_SENT = "eu.ttbox.geoping.BootCompleteReceiver.ACTION_SMS_SENT";

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            // Statrt BG Service
            // Intent i = new Intent(context, BackgroudLocService.class);
            // i.setAction(Intent.ACTION_BOOT_COMPLETED);
            // context.startService(i);

            // NSpy Notif
            String encrypedMsg = "Mon tel Vient de démarrer : " + action;
            Log.d(TAG, "### ############################### ### ");
            Log.d(TAG, "### ### " + encrypedMsg + " ### ### ");
            Log.d(TAG, "### ############################### ### ");
            // Search Phones
            String phone = SpyNotificationHelper.searchPhoneForNotif(context, PairingColumns.COL_NOTIF_SHUTDOWN);
            Log.d(TAG, "### ### Destination : " + phone + " ### ### ");
            if (phone != null) {
                PendingIntent sendIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_SMS_SENT), 0);
                SmsManager.getDefault().sendTextMessage(phone, null, encrypedMsg, sendIntent, null);
            }
        } else if (ACTION_SMS_SENT.equals(action)) {
            String message = null;
            boolean error = true;
            switch (getResultCode()) {
            case Activity.RESULT_OK:
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
            Log.d(TAG, "### ############################### ### ");
            Log.d(TAG, "### ###  ACTION_SMS_SENT : " + message);
            Log.d(TAG, "### ############################### ### ");
        }

        // This is where you start your service
        // startService(new Intent(aContext, MyService.class);
    }

}
