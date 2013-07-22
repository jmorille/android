package eu.ttbox.geoping.ui.smslog;


import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
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
import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.service.encoder.MessageActionEnumLabelHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.ui.person.PhotoHeaderBinderHelper;
import eu.ttbox.geoping.ui.person.PhotoThumbmailCache;


public class SmsLogViewFragment extends SherlockFragment {

    private static final String TAG = "SmsLogViewFragment";

    // Binding
    private PhotoHeaderBinderHelper photoHeader;


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
        photoHeader = new PhotoHeaderBinderHelper(v);

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
        photoHeader.subEltPhoneTextView.setText(phone);

        // Action
        MessageActionEnum action =  helper.getSmsMessageActionEnum(cursor);
        String actionLabel = MessageActionEnumLabelHelper.getString(mContext, action );
        photoHeader.subEltNameTextView.setText(actionLabel);

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
        cacheNameFinder.setTextViewPersonNameByPhone(photoHeader.mainActionNameTextView, phone, smsLogSide);

        // Photo
         photoCache.loadPhoto(getActivity(), photoHeader.photoImageView, null, phone);
    }

    private void bindMessageParams(String msgParams) {
        Log.d(TAG, "Read Json Params : " + msgParams);
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
                     defineParamTextLabel(keyTextView, valueTextView, key, val);
                    paramListView.addView(convertView);
                }
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing JSON " + msgParams + " : " + e.getMessage(), e);
            }
        }
    }

    private void defineParamTextLabel( TextView keyTextView, TextView valueTextView ,  String key,  String val ) {
        SmsMessageLocEnum param = SmsMessageLocEnum.getByEnumName(key);
        if (param ==null) {
            keyTextView.setText(key);
            valueTextView.setText(val);
        } else   if (param.equals(SmsMessageLocEnum.EVT_DATE) || param.equals(SmsMessageLocEnum.DATE)) {
            keyTextView.setText(key);
            valueTextView.setText(val);
        } else if ( param.hasLabelValueResourceId() ) {
            keyTextView.setText(param.getLabelValueResourceId(getActivity(), val));
            valueTextView.setText(null);
        } else {
            keyTextView.setText(key);
            valueTextView.setText(val);
        }

    }



    // ===========================================================
    // Others
    // ===========================================================

}
