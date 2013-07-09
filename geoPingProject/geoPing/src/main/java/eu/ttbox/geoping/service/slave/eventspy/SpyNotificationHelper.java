package eu.ttbox.geoping.service.slave.eventspy;

import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.domain.model.PairingAuthorizeTypeEnum;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.pairing.PairingDatabase;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.service.SmsSenderHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.slave.GeoPingSlaveLocationService;

public class SpyNotificationHelper {

    private static final String TAG = "SpyNotificationHelper";


    public static String[] searchListPhonesForGeofenceViolation(Context context,  List<CircleGeofence> geofences, SmsMessageActionEnum transitionType) {
        // TODO Use geofences and transitionType to Calculate
        // Query
        String[] projection = new String[]{PairingDatabase.PairingColumns.COL_PHONE};
        String selection = String.format("%s = 1", PairingDatabase.PairingColumns.COL_AUTHORIZE_TYPE);
        String[] selectionArgs = new String[]{String.valueOf(PairingAuthorizeTypeEnum.AUTHORIZE_ALWAYS.getCode())};
        Cursor cursor = context.getContentResolver().query(PairingProvider.Constants.CONTENT_URI, projection, selection, null, null);
        // Convert As String
        String[] phones = convertCursorAsStringArrayWithCloseCursor(cursor, 0);
        return phones;
    }


    public static  String[] searchListPhonesForNotif(Context context, String notifCol) {
        Cursor cursor = getCursorForSearchPhoneForNotif(context, notifCol);
        Log.d(TAG, "Search EventSpy " + notifCol + " : found " + cursor.getCount() + " phones to notify");
        String[] result = convertCursorAsStringArrayWithCloseCursor(cursor, 0);
        return result;
    }


    public static String searchContactPhonesForNotif(Context context, String notifCol) {
        StringBuffer sb = new StringBuffer();
        boolean isNotFirst = false;
        Cursor cursor = getCursorForSearchPhoneForNotif(context, notifCol);
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


    private static  String[] convertCursorAsStringArrayWithCloseCursor(Cursor cursor, int colIdx) {
        String[] result = null;
        try {
            int resultCount = cursor.getCount();
            if (resultCount > 0) {
                HashSet<String>  phones = new HashSet<String>(resultCount);
                while (cursor.moveToNext()) {
                    String phone = cursor.getString(0);
                    phones.add(phone);
                }
                result  = phones.toArray(new String[phones.size()]);
            }
            Log.d(TAG, "ConvertCursor As StringArray : found " + resultCount + " String converted from idx " + colIdx);
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

    public static void sendEventSpySmsMessage(Context context,String[] phones, SmsMessageActionEnum eventType, Bundle eventParams) {
        if (phones != null) {
            Log.d(TAG, "EventSpy Notification  : " + eventType + " for " + phones.length + " phones destinations");
            // Send SMS
            Bundle params = eventParams == null ? new Bundle() : eventParams;
            if (!SmsMessageLocEnum.EVT_DATE.isToBundle(params)) {
                SmsMessageLocEnum.EVT_DATE.writeToBundle(params, System.currentTimeMillis());
            }
            if (SmsMessageActionEnum.SPY_SHUTDOWN.equals(eventType)) {
                // Not time to get GeoLoc, send it direct
                for (String phone : phones) {
                    SmsSenderHelper.sendSmsAndLogIt(context, SmsLogSideEnum.SLAVE, phone, eventType, params);
                }
                // TODO saveInLocalDb

            } else { 
                GeoPingSlaveLocationService.runFindLocationAndSendInService(context, eventType, phones, params); 
            }
        }
    }

}
