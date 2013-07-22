package eu.ttbox.geoping.service.slave.eventspy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;

public class PhoneCallReceiver extends BroadcastReceiver {

    private static final String TAG = "PhoneCallReceiver";

    // Prefs
    public static final String PHONE_RECEIVER_PREFS_NAME = "eu.ttbox.geoping.prefs.PhoneCallReceiver";

    // Action
    private static final String ACTION_PHONE_STATE_CHANGED = TelephonyManager.ACTION_PHONE_STATE_CHANGED;
    private static final String ACTION_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";

    // Extras Incoming
    private static final String EXTRA_STATE = "state";
    private static final String EXTRA_INCOMING_NUMBER = "incoming_number";

    // Extras Outgoing
    private static final String EXTRA_OUTGOING_NUMBER = "android.intent.extra.PHONE_NUMBER";

    // Incomming State
    private static final String STATE_RINGING = "RINGING"; // Sonne
    private static final String STATE_OFFHOOK = "OFFHOOK"; // Decrocher
    private static final String STATE_IDLE = "IDLE"; // Racroche

    // Prefs
    private static final String PREFS_KEY_PHONE_NUMBER = "PREFS_KEY_PHONE_NUMBER";
    private static final String PREFS_KEY_ACTION = "PREFS_KEY_ACTION";
    private static final String PREFS_KEY_INLINE_TIME_IN_MS = "PREFS_KEY_INLINE_TIME_IN_MS";

    private static final int PREFS_ACTION_INCOMING = 1;
    private static final int PREFS_ACTION_OUTGOING = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_PHONE_STATE_CHANGED.equals(action)) {
            Log.d(TAG, "EventSpy PhoneState action : " + action);
            Bundle extras = intent.getExtras();
            printExtras(extras);
            String state = extras.getString(EXTRA_STATE);
            SharedPreferences prefs = context.getSharedPreferences(PHONE_RECEIVER_PREFS_NAME, Context.MODE_PRIVATE);
            if (STATE_RINGING.equals(state)) {
                String phoneNumber = extras.getString(EXTRA_INCOMING_NUMBER);
                Log.d(TAG, "EventSpy PhoneState STATE_RINGING incomming call : " + phoneNumber);
                // Prefs
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.putString(PREFS_KEY_PHONE_NUMBER, phoneNumber);
                prefEditor.putInt(PREFS_KEY_ACTION, PREFS_ACTION_INCOMING);
                prefEditor.commit();
                
                Log.d(TAG, "EventSpy PhoneState Write State " + PREFS_KEY_PHONE_NUMBER + " : " +  phoneNumber );
                Log.d(TAG, "EventSpy PhoneState Write State " + PREFS_KEY_ACTION + " : " +  PREFS_ACTION_INCOMING );

            } else if (STATE_OFFHOOK.equals(state)) {
            	Log.d(TAG, "EventSpy PhoneState STATE_OFFHOOK"  );
                // Decrocher
                SharedPreferences.Editor prefEditor = prefs.edit();
                long now = System.currentTimeMillis();
                prefEditor.putLong(PREFS_KEY_INLINE_TIME_IN_MS, now);
                prefEditor.commit();
                
                Log.d(TAG, "EventSpy PhoneState Write State " + PREFS_KEY_INLINE_TIME_IN_MS + " : " +  now );
                
            } else if (STATE_IDLE.equals(state)) {
            	Log.d(TAG, "EventSpy PhoneState STATE_IDLE"  );
                // Racroche ou ignore
                long endCall = System.currentTimeMillis();
                long beginCall = prefs.getLong(PREFS_KEY_INLINE_TIME_IN_MS, -1);
                int callAction = prefs.getInt(PREFS_KEY_ACTION, -1);
                String phoneNumber = prefs.getString(PREFS_KEY_PHONE_NUMBER, null);
                // Clear Values
                SharedPreferences.Editor prefEditor = prefs.edit();
                prefEditor.remove(PREFS_KEY_PHONE_NUMBER);
                prefEditor.remove(PREFS_KEY_ACTION);
                prefEditor.remove(PREFS_KEY_INLINE_TIME_IN_MS);
                prefEditor.commit();
                // Manage Datas
                String message =  manageCallDatas(context, phoneNumber, callAction, beginCall, endCall);
            }
        } else if (ACTION_NEW_OUTGOING_CALL.equals(action)) {
            Log.d(TAG, "EventSpy PhoneState action : " + action);
            Bundle extras = intent.getExtras();
            printExtras(extras);
            // String
            String composePhoneNumber = extras.getString(EXTRA_OUTGOING_NUMBER);
            Log.d(TAG, "EventSpy PhoneState compose PhoneNumber : " + composePhoneNumber);
            // Service
            SharedPreferences prefs = context.getSharedPreferences(PHONE_RECEIVER_PREFS_NAME, Context.MODE_PRIVATE);
            // Prefs
            SharedPreferences.Editor prefEditor = prefs.edit();
            prefEditor.putString(PREFS_KEY_PHONE_NUMBER, composePhoneNumber);
            prefEditor.putInt(PREFS_KEY_ACTION, PREFS_ACTION_OUTGOING);
            prefEditor.commit();
        }
    }

    private String manageCallDatas(Context context, String callPhoneNumber, int callAction, long beginCall, long endCall) {
        String message = null;
        Log.d(TAG, "EventSpy PhoneState Compute Final State phoneNumber : " + callPhoneNumber );
        Log.d(TAG, "EventSpy PhoneState Compute Final State callAction : " + callAction );
        Log.d(TAG, "EventSpy PhoneState Compute Final State beginCall : " + beginCall );
        Log.d(TAG, "EventSpy PhoneState Compute Final State endCall : " + endCall );
        if (beginCall < 0) {
            // Pas de communication
            switch (callAction) {
            case PREFS_ACTION_OUTGOING:
                message = "A essayer d'appeler le numéro " + callPhoneNumber;
                break;
            case PREFS_ACTION_INCOMING:
                message = "A recu un appel non repondu du numéro " + callPhoneNumber;
                break;
            default:
                break;
            }
        } else {
            int callDurationInS = (int) ((endCall - beginCall) / 1000);
            switch (callAction) {
            case PREFS_ACTION_OUTGOING:
                message = "A appeler " + callDurationInS + " s le numéro " + callPhoneNumber;
                break;
            case PREFS_ACTION_INCOMING:
                message = "A Recu un appel de " + callDurationInS + " s du numéro " + callPhoneNumber;
                break;
            default:
                break;
            }
        }
        Log.d(TAG, "EventSpy PhoneState === > : " + message);
        // TODO
        String[] phones= SpyNotificationHelper.searchListPhonesForNotif(context, PairingColumns.COL_NOTIF_PHONE_CALL);
        if (phones != null) {
            Bundle params = new Bundle();
            SmsMessageLocEnum.PHONE_NUMBER.writeToBundle(params, callPhoneNumber);
            // Send Sms
            SpyNotificationHelper.sendEventSpySmsMessage(context,phones,  MessageActionEnum.SPY_PHONE_CALL, params);
        }
        return message;
    }

    private void printExtras(Bundle extras) {
        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            Log.d(TAG, "EventSpy PhoneState extras : " + key + " = " + value);
        }
    }

}
