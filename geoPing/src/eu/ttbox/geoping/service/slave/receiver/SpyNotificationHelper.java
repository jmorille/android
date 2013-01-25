package eu.ttbox.geoping.service.slave.receiver;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;

public class SpyNotificationHelper {


    private static final String TAG = "SpyNotificationHelper";

    public static String searchPhoneForNotif(Context context, String notifCol) {
        String[] projection = new String[] { PairingColumns.COL_PHONE };
        String selection = String.format("%s = 1", notifCol);
        Cursor cursor = context.getContentResolver().query(PairingProvider.Constants.CONTENT_URI, projection, selection, null, null);
        Log.d(TAG, "Search Pairing for criteria : " + selection + " ==> " + cursor.getCount() + " result");
        StringBuffer sb = new StringBuffer();
        boolean isNotFirst = false;
        try {
            while (cursor.moveToNext()) {
                if (isNotFirst) {
                    sb.append(' ');
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
    
}
