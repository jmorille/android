package eu.ttbox.geoping.ui.smslog;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import eu.ttbox.geoping.core.PhoneNumberUtils;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.pairing.PairingDatabase;
import eu.ttbox.geoping.domain.person.PersonDatabase;
import eu.ttbox.geoping.service.core.ContactHelper;
import eu.ttbox.geoping.service.core.ContactVo;


public class PersonNameFinderHelper {


    private static final String TAG = "PersonNameFinderHelper";
    private static final String CACHE_PREFIX_MASTER = "M:";
    private static final String CACHE_PREFIX_SLAVE = "S:";

    private static final String VALUE_FOR_NOT_FOUND = "";
    // Instance
    private Context mContext;
    private LruCache<String, String> cache;

    public PersonNameFinderHelper(Context context) {
      this(context, 1024*1024);
    }

    public PersonNameFinderHelper(Context context, int cacheSize) {
        this.mContext = context;
        this.cache = new LruCache<String, String>(cacheSize);
    }

    public void setTextViewPersonNameByPhone(TextView textView, String phone, SmsLogSideEnum smsLogSide) {
        // Base Uri
        String  cacheKey = null;
        String nameResult = null;
        switch (smsLogSide) {
            case MASTER: {
                cacheKey = getCacheKey(phone, CACHE_PREFIX_MASTER);
                nameResult = cache.get(cacheKey);
            }
            break;
            case SLAVE: {
                cacheKey = getCacheKey(phone, CACHE_PREFIX_SLAVE);
                nameResult = cache.get(cacheKey);
            }
            default:
                throw new IllegalArgumentException("No getCacheKey Implementation for SmsLogSideEnum : " + smsLogSide);
        }
        // Bind name
        if (nameResult!=null) {
            textView.setText(nameResult);
        } else {
            // Set Temporary Phone as Name
            textView.setText(phone);
            // Cancel previous Async
            final PersonNameFinderAsyncTask oldTask = (PersonNameFinderAsyncTask) textView.getTag();
            if (oldTask != null) {
                oldTask.cancel(false);
            }
            PersonNameFinderAsyncTask newTask = new PersonNameFinderAsyncTask(textView, smsLogSide);
            textView.setTag(newTask);
            newTask.execute(phone, cacheKey);
        }
    }

    private String getCacheKey(String phone, String cachePrefix) {
        String cacheKey = new StringBuilder()
                .append(cachePrefix) //
                .append(PhoneNumberUtils.getStrippedReversed(phone))//
                .toString();
        return cacheKey;
    }



    private String queryPersonName(Uri uri, String colName) {
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



    private String getPersonNameByPhone(String phone, SmsLogSideEnum smsLogSide) {
        String result = null;
        switch (smsLogSide) {
            case MASTER: {
                String cacheKey = getCacheKey(CACHE_PREFIX_MASTER, phone);
                result = cache.get(cacheKey);
                if (result == null) {
                    result = queryPersonName(PersonProvider.Constants.getUriPhoneFilter(phone), PersonDatabase.PersonColumns.COL_NAME);
                    cache.put(cacheKey, result);
                }
            }
            break;
            case SLAVE: {
                String cacheKey = getCacheKey(CACHE_PREFIX_SLAVE, phone);
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

    private class PersonNameFinderAsyncTask extends AsyncTask<String, Void, String> {


        final TextView holder;
        final SmsLogSideEnum smsLogSide;

        public PersonNameFinderAsyncTask(TextView holder, SmsLogSideEnum smsLogSid) {
            super();
            this.holder = holder;
            this.smsLogSide = smsLogSid;
        }

        @Override
        protected void onPreExecute() {
            holder.setTag(this);
        }

        @Override
        protected String doInBackground(String... params) {
            String phone = params[0];
            String cacheKey = params[1];
            // Search In Cache
            String result = cache.get(cacheKey);
            // Search In Db
            if (TextUtils.isEmpty(result)) {
                result = getPersonNameByPhone(phone, smsLogSide);
            }
            // Search In Contact Directory
            if (TextUtils.isEmpty(result)) {
                ContactVo contact = ContactHelper.searchContactForPhone(mContext,  phone);
                if (contact!=null) {
                    result = contact.displayName;
                }
            }
            // Put In Cache
            if (!TextUtils.isEmpty(result)) {
                cache.put(cacheKey, result);
            } else {
                // Nothing found, so register the Search Criteria
                cache.put(cacheKey, phone);
//                cache.put(cacheKey, VALUE_FOR_NOT_FOUND);
            }
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            // Define Result
            if (TextUtils.isEmpty(result)) {
                holder.setText(result);
            }
            // Clear Ref
            holder.setTag(null);
        }

    }


}

