package eu.ttbox.geoping.ui.smslog;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.TextView;

import eu.ttbox.geoping.core.PhoneNumberUtils;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.pairing.PairingDatabase;
import eu.ttbox.geoping.domain.person.PersonDatabase;


public class SmsLogViewHelper {


    private static final String TAG = "SmsLogViewHelper";
    private Context mContext;
    private LruCache<String, String> cache = new LruCache<String, String>(1024);

    public SmsLogViewHelper(Context context) {
        mContext = context;
    }
    public void setTextViewPersonNameByPhone(TextView textView,String phone, SmsLogSideEnum smsLogSide) {
        // TODO Better
        String personName = getPersonNameByPhone(phone, smsLogSide);
        personName = personName ==null ? phone : personName;
        textView.setText(personName);
    }

    public String getPersonNameByPhone(String phone, SmsLogSideEnum smsLogSide) {
        String result = null;
        switch (smsLogSide) {
            case MASTER: {
                String cacheKey = "MASTER:" + PhoneNumberUtils.getStrippedReversed(phone);
                result = cache.get(cacheKey);
                if (result == null) {
                    result = queryPersonName(PersonProvider.Constants.getUriPhoneFilter(phone), PersonDatabase.PersonColumns.COL_NAME);
                    cache.put(cacheKey, result);
                }
            }
            break;
            case SLAVE: {
                String cacheKey = "SLAVE:" + PhoneNumberUtils.getStrippedReversed(phone);
                result = cache.get(cacheKey);
                if (result == null) {
                    result = queryPersonName(PairingProvider.Constants.getUriPhoneFilter(phone), PairingDatabase.PairingColumns.COL_NAME);
                    cache.put(cacheKey, result);
                }
            }
            break;
            default:
                Log.w(TAG, "Not manage Side : " + smsLogSide);
        }
        return result;
    }

    public String queryPersonName(Uri uri, String colName) {
        String result = null;
        ContentResolver cr = mContext.getContentResolver();
        Cursor cursor = cr.query(uri, new String[]{colName}, null, null, null);
        try {
            if (cursor.moveToNext()) {
                result = cursor.getString(0);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

}

