package eu.ttbox.geoping.ui.smslog;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.core.ContactHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.ui.person.PhotoThumbmailCache;

public class SmsLogViewFragment extends SherlockFragment {

    private static final String TAG = "SmsLogViewFragment";

    // Binding
    private ImageView photoImageView;
    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView actionTextView;
    private TextView messageTextView;
    private ImageView smsTypeImageView;
    private TextView smsTypeTimeTextView;
    private LinearLayout paramListView;

    // Cache
    private PhotoThumbmailCache photoCache;
    private SmsLogResources mResources;

    // Instance
    private Uri entityUri;

    // Context
    private Context mContext;
    private SmsLogHelper helper = new SmsLogHelper();
    private Handler handler = new Handler();
    private PersonNameFinderHelper cacheNameFinder;

    // ===========================================================
    // Constructors
    // ===========================================================

    private ContentObserver observer = new ContentObserver(handler) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            loadEntity(entityUri);
            super.onChange(selfChange, uri);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.smslog_view, container, false);
        mContext = getActivity();
        // Menu on Fragment
        setHasOptionsMenu(true);
        // Cache
        this.photoCache = ((GeoPingApplication) mContext.getApplicationContext()).getPhotoThumbmailCache();
        this.mResources = new SmsLogResources(getActivity());
        this.cacheNameFinder = new PersonNameFinderHelper(mContext, false);

        // Bindings
        this.photoImageView = (ImageView) v.findViewById(R.id.smslog_photo_imageView);
        this.nameTextView = (TextView) v.findViewById(R.id.smslog_name);
        this.phoneTextView = (TextView) v.findViewById(R.id.smslog_phone);
        this.actionTextView = (TextView) v.findViewById(R.id.smslog_action);
        this.messageTextView = (TextView) v.findViewById(R.id.smslog_message);
        this.paramListView = (LinearLayout) v.findViewById(R.id.smslog_message_param_list);

        this.smsTypeImageView = (ImageView) v.findViewById(R.id.smslog_list_item_smsType_imgs);
        this.smsTypeTimeTextView = (TextView) v.findViewById(R.id.smslog_list_item_time_ago);

        return v;
    }
    // ===========================================================
    // Menu
    // ===========================================================

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Uri loadUri = null;
        if (savedInstanceState != null) {
            Log.d(TAG, "Restore onCreate savedInstanceState: " + savedInstanceState);
            loadUri = savedInstanceState.getParcelable(Intents.EXTRA_DATA_URI);
        }
        Log.d(TAG, "onActivityCreated");
        // Load Data
        if (loadUri != null) {
            loadEntity(loadUri);
        } else {
            loadEntity(getActivity().getIntent());
        }
    }

    // ===========================================================
    // Lyfe Cycle
    // ===========================================================

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(Intents.EXTRA_DATA_URI, entityUri.toString());
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Save onSaveInstanceState : " + outState);
    }

    private void registerContentObserver(Uri entityUri) {
        if (entityUri != null) {
            ContentResolver cr = getActivity().getContentResolver();
            cr.registerContentObserver(entityUri, false, observer);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        registerContentObserver(entityUri);
    }

    @Override
    public void onPause() {
        super.onPause();
        ContentResolver cr = getActivity().getContentResolver();
        cr.unregisterContentObserver(observer);
    }

    // ===========================================================
    // Load Data
    // ===========================================================

    private void loadEntity(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            loadEntity(uri);
        } else {
            Log.w(TAG, "Could not load entity without Uri : " + intent);
        }
    }

    public void loadEntity(Uri entityUri) {
        Log.d(TAG, "Load entity Uri : " + entityUri);
        this.entityUri = entityUri;
        ContentResolver cr = mContext.getContentResolver();
        String selection = null;
        String[] selectionArgs = null;
        Cursor cursor = cr.query(entityUri, SmsLogDatabase.SmsLogColumns.ALL_COLS, selection, selectionArgs, null);
        try {
            if (cursor.moveToFirst()) {
                loadEntity(cursor);
            }
        } finally {
            cursor.close();
        }
        // Register Cursor Observer
        registerContentObserver(entityUri);
    }

    public void loadEntity(Cursor cursor) {
        if (helper.isNotInit ) {
            helper.initWrapper(cursor);
        }
        // Phone
        String phone = helper.getSmsLogPhone(cursor);
        phoneTextView.setText(phone);

        // Action
        SmsMessageActionEnum action =  helper.getSmsMessageActionEnum(cursor);
        String actionLabel =getString(action.labelResourceId);
        actionTextView.setText(actionLabel);

        // Messages Sizes    helper.getM
        String smsMessage = helper.getMessage(cursor);
        int msgSize = smsMessage==null? 0 : smsMessage.length();
        String message = getString(R.string.smslog_message_size,msgSize );
        messageTextView.setText( message);

         // Bind Value
        SmsLogTypeEnum smLogType = helper.getSmsLogType(cursor);
        Drawable iconType = mResources.getCallTypeDrawable(smLogType);
        smsTypeImageView.setImageDrawable(iconType);
        // Time SmsType
        long smsLogTime = helper.getSmsLogTime(cursor);
        switch (smLogType) {
            case SEND_ACK:
                smsLogTime =  helper.getSendAckTimeInMs(cursor);
                break;
            case SEND_DELIVERY_ACK:
                smsLogTime =  helper.getSendDeliveryAckTimeInMs(cursor);
                break;
         }
        // Time
        String smsTypeTime = DateUtils.formatDateRange(mContext, smsLogTime, smsLogTime,
                DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
        smsTypeTimeTextView.setText(smsTypeTime);

        // Params
        String msgParams = helper.getMessageParams(cursor);
        bindMessageParams(msgParams);

        //TODO Name Person
        SmsLogSideEnum smsLogSide = helper.getSmsLogSideEnum(cursor);
        cacheNameFinder.setTextViewPersonNameByPhone(nameTextView, phone, smsLogSide);

        // Photo
        loadPhoto(null, phone);
    }

    private void bindMessageParams(String msgParams) {
        if (msgParams != null) {
            try {
                JSONObject json = new JSONObject(msgParams);
                Iterator<String> it =  json.keys();
                paramListView.removeAllViewsInLayout();
                LayoutInflater layoutInflater = LayoutInflater.from(mContext);
                while (it.hasNext()) {
                    String key = it.next();
                    String val = json.getString(key);
                    Log.d(TAG, "JSONObject key : "  +key + " = " + val);
                    View convertView = layoutInflater.inflate(R.layout.smslog_view_list_param_item, null);
                    TextView keyTextView =  (TextView)convertView.findViewById(R.id.smslog_list_item_param_key);
                    TextView valueTextView =  (TextView)convertView.findViewById(R.id.smslog_list_item_param_value );
                    // Set Values
                    keyTextView.setText(key);
                    valueTextView.setText(val);
                    paramListView.addView(convertView);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON " + msgParams + " : " + e.getMessage(), e);
            }
        }
    }


    // ===========================================================
    // Photo Loader
    // ===========================================================

    /**
     * Pour plus de details sur l'intégration dans les contacts consulter
     * <ul>
     * <li>item_photo_editor.xml</li>
     * <li>com.android.contacts.editor.PhotoEditorView</li>
     * <li>com.android.contacts.detail.PhotoSelectionHandler</li>
     * <li>com.android.contacts.editor.ContactEditorFragment.PhotoHandler</li>
     * </ul>
     *
     * @param contactId
     */
    private void loadPhoto(String contactId, final String phone) {
        Bitmap photo = null;
        boolean isContactId = !TextUtils.isEmpty(contactId);
        boolean isContactPhone = !TextUtils.isEmpty(phone);
        // Search in cache
        if (photo == null && isContactId) {
            photo = photoCache.get(contactId);
        }
        if (photo == null && isContactPhone) {
            photo = photoCache.get(phone);
        }
        // Set Photo
        if (photo != null) {
            photoImageView.setImageBitmap(photo);
        } else if (isContactId || isContactPhone) {
            // Cancel previous Async
            final PhotoLoaderAsyncTask oldTask = (PhotoLoaderAsyncTask) photoImageView.getTag();
            if (oldTask != null) {
                oldTask.cancel(false);
            }
            // Load photos
            PhotoLoaderAsyncTask newTask = new PhotoLoaderAsyncTask(photoImageView);
            photoImageView.setTag(newTask);
            newTask.execute(contactId, phone);
        }
        // photoImageView.setEditorListener(new EditorListener() {
        //
        // @Override
        // public void onRequest(int request) {
        // Toast.makeText(getActivity(), "Click to phone " + phone,
        // Toast.LENGTH_SHORT).show();
        // }
        //
        // });
    }

    public class PhotoLoaderAsyncTask extends AsyncTask<String, Void, Bitmap> {

        final ImageView holder;

        public PhotoLoaderAsyncTask(ImageView holder) {
            super();
            this.holder = holder;
        }

        @Override
        protected void onPreExecute() {
            holder.setTag(this);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String contactIdSearch = params[0];
            String phoneSearch = null;
            if (params.length > 1) {
                phoneSearch = params[1];
            }
            Bitmap result = ContactHelper.openPhotoBitmap(getActivity(), photoCache, contactIdSearch, phoneSearch);
            Log.d(TAG, "PhotoLoaderAsyncTask load photo : " + (result != null));
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (holder.getTag() == this) {
                holder.setImageBitmap(result);
                holder.setTag(null);
                Log.d(TAG, "PhotoLoaderAsyncTask onPostExecute photo : " + (result != null));
            }
        }
    }

    // ===========================================================
    // Others
    // ===========================================================

}
