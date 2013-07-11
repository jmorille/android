package eu.ttbox.geoping.service.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class GcmMessageReceiver  extends BroadcastReceiver {

    private static final String TAG = "GcmMessageReceiver";
    private static final String  INTENT_EXTRA_GCM_MESSAGE = "eu.ttbox.geoping.EXTRA_GCM_MESSAGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "#################################");
        Log.d(TAG, "### GCM onMessage : " + intent);
        printExtras(intent.getExtras());
        Log.d(TAG, "#################################");

        // Resend As Broadcast
        Intent new_intent = new Intent();
        new_intent.putExtras(intent);
        new_intent.setAction(INTENT_EXTRA_GCM_MESSAGE);
        context.sendBroadcast(new_intent);


    }

    private void printExtras(Bundle extras) {
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d(TAG, "### GCM extras : " + key + " = " + value);
            }
        }
    }


}
