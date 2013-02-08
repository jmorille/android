package eu.ttbox.geoping.service.slave.receiver;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.service.SmsSenderHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.encoder.params.SmsValueEventTypeEnum;

public class SpyNotificationHelper {

    private static final String TAG = "SpyNotificationHelper";

    public static String searchContactPhonesForNotif(Context context, String notifCol) {
        Cursor cursor = getCursorForSearchPhoneForNotif(context, notifCol);
        StringBuffer sb = new StringBuffer();
        boolean isNotFirst = false;
        try {
            while (cursor.moveToNext()) {
                if (isNotFirst) {
                    sb.append(';');
                }
                String phone = cursor.getString(0);
                sb.append(phone);
                isNotFirst = true;
            }
        } finally {
            cursor.close();
        }
        // Result
        if (isNotFirst) {
            return sb.toString();
        }
        return null;

    }

    public static ArrayList<String> searchListPhonesForNotif(Context context, String notifCol) {
        ArrayList<String> result = null;
        Cursor cursor = getCursorForSearchPhoneForNotif(context, notifCol);
        try {
            int resultCount = cursor.getCount();
            if (resultCount > 0) {
                result = new  ArrayList<String> (resultCount);
                while (cursor.moveToNext()) {
                    String phone = cursor.getString(0);
                    result.add(phone);
                }
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    private static Cursor getCursorForSearchPhoneForNotif(Context context, String notifCol) {
        String[] projection = new String[] { PairingColumns.COL_PHONE };
        String selection = String.format("%s = 1", notifCol);
        Cursor cursor = context.getContentResolver().query(PairingProvider.Constants.CONTENT_URI, projection, selection, null, null);
        Log.d(TAG, "Search Pairing for criteria : " + selection + " ==> " + cursor.getCount() + " result");
        return cursor;
    }
    
    
    public static void sendEventSpySmsMessage(Context context, ArrayList<String> phones, SmsValueEventTypeEnum eventType) {
        if (phones != null) {
            Log.d(TAG, "Event Spy Notification  : " +eventType+ " for "+ phones.size() + " phones destinations");
            // Send SMS
            Bundle params = new Bundle();
            SmsMessageLocEnum.EVT_DATE.writeToBundle(params, System.currentTimeMillis());
            SmsMessageLocEnum.EVT_TYPE.writeToBundle(params, eventType);
            for (String phone : phones) {
                SmsSenderHelper.sendSms(context, phone, SmsMessageActionEnum.ACTION_SPY_EVENT, params);
            }   
        }
    }
    
}
