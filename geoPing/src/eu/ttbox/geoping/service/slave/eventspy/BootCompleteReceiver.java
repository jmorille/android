package eu.ttbox.geoping.service.slave.eventspy;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;

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
            Log.d(TAG, "### ### EventSpy BOOT : " + encrypedMsg + " ### ### ");
            Log.d(TAG, "### ############################### ### ");
            // Search Phones
            ArrayList<String> phones= SpyNotificationHelper.searchListPhonesForNotif(context, PairingColumns.COL_NOTIF_SHUTDOWN);
            if (phones != null) {
                Bundle params = new Bundle();
                // Send Sms
                SpyNotificationHelper.sendEventSpySmsMessage(context,phones,  SmsMessageActionEnum.SPY_BOOT, params);
            } 
        } 
    }

}
