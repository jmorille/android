package eu.ttbox.geoping.ui.smslog;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.core.ContactHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.ui.person.PhotoEditorView;
import eu.ttbox.geoping.ui.person.PhotoThumbmailCache;
import eu.ttbox.geoping.ui.person.PhotoThumbmailCache.PhotoLoaderAsyncTask;

/**
 * <ul>
 * <li>platform_packages_apps_contacts/res/layout/call_log_list_item.xml</li>
 * <li> platform_packages_apps_contacts/res/layout/call_detail_history_item.xml</li>
 * <li>com.android.contacts.PhoneCallDetailsHelper#callTypeAndDate</li>
 * <li> android.text.format.DateUtils#getRelativeTimeSpanString(long time, long now, long minResolution,  int flags)</li>
 * </ul>
 */
public class SmsLogListAdapter extends android.support.v4.widget.ResourceCursorAdapter {

    private static final String TAG = "SmsLogListAdapter";
    private SmsLogHelper helper;
    private boolean isNotBinding = true;
    private SmsLogResources mResources;
    private android.content.res.Resources resources;
    private boolean isDisplayContactDetail = true;
    private boolean isDisplayPhoneAndName = true;
    // Cache
    private PhotoThumbmailCache photoCache;
    private PersonNameFinderHelper cacheNameFinder;

    // ===========================================================
    // Constructor
    // ===========================================================

    public SmsLogListAdapter(Context context, Cursor c, int flags, boolean isDisplayContactDetail) {
        super(context, R.layout.smslog_list_item, c, flags);
        this.resources = context.getResources();
        this.mResources = new SmsLogResources(context);
        this.isDisplayContactDetail = isDisplayContactDetail;
        // Cache
        this.photoCache = ((GeoPingApplication) context.getApplicationContext()).getPhotoThumbmailCache();
        this.cacheNameFinder = new PersonNameFinderHelper(mContext, isDisplayPhoneAndName);
    }

    private void intViewBinding(View view, Context context, Cursor cursor) {
        // Init Cursor
        helper = new SmsLogHelper().initWrapper(cursor);
        isNotBinding = false;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        if (isNotBinding) {
            intViewBinding(view, context, cursor);
        }
        ViewHolder holder = (ViewHolder) view.getTag();
        // Bind Value
        SmsLogTypeEnum smLogType = helper.getSmsLogType(cursor);
        Drawable iconType = mResources.getCallTypeDrawable(smLogType);
        holder.smsType.setImageDrawable(iconType);
        // Text
        SmsMessageActionEnum action = helper.getSmsMessageActionEnum(cursor);
        String actionLabel;
        if (action != null) {
            actionLabel = getSmsActionLabel(action);
            /*
            int ressourceTypeId = PhotoEditorView.GEOPING_TYPE_TRIANGLE;
            switch (action) {
                case GEOFENCE_EXIT:
                case GEOFENCE_ENTER:
                    ressourceTypeId = PhotoEditorView.GEOPING_TYPE_GEOFENCE;
                    break;
                default:
                    ressourceTypeId = PhotoEditorView.GEOPING_TYPE_TRIANGLE;
                    break;
            }
            holder.photoImageView.setGeopingType(ressourceTypeId);
            holder.photoImageViewSend.setGeopingType(PhotoEditorView.GEOPING_TYPE_TRIANGLE);
            */
        } else {
            actionLabel = helper.getSmsMessageActionString(cursor);
        }
        holder.actionText.setText(actionLabel);

        // Phone
        String phone = helper.getSmsLogPhone(cursor);
        if (isDisplayContactDetail) {
            SmsLogSideEnum side = helper.getSmsLogSideEnum(cursor);
            cacheNameFinder.setTextViewPersonNameByPhone(holder.phoneText, phone, side);
            holder.phoneText.setVisibility(View.VISIBLE);
        } else {
            holder.phoneText.setText(null);
            holder.phoneText.setVisibility(View.GONE);
        }
        // Time
        long time = helper.getSmsLogTime(cursor);
        String timeFormat = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", time);
        holder.timeText.setText(timeFormat);

        // Time relative
        long currentTimeMillis = System.currentTimeMillis();
        CharSequence dateText =
                DateUtils.getRelativeTimeSpanString(time,
                        currentTimeMillis,
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE);
        holder.timeAgoText.setText(dateText);

        // Backgroud send
        if (SmsLogTypeEnum.RECEIVE.equals(smLogType)) {
            // view.setBackgroundResource(R.drawable.fleche_verte_g);
            view.setBackground(null);
        } else {
            // view.setBackgroundResource(R.drawable.fleche_bleue_d);
            view.setBackgroundResource(R.drawable.dialpad_background);
        }

        // Load Photos
        if (isDisplayContactDetail) {
            // Compute The Correct ImageView
            PhotoEditorView imageViewToLoad = holder.photoImageView;
//            holder.photoImageView.setVisibility(View.VISIBLE);
//            holder.photoImageViewSend.setVisibility(View.GONE);

//            // V Side
            if (SmsLogTypeEnum.RECEIVE.equals(smLogType)) {
                imageViewToLoad = holder.photoImageViewSend;
                holder.photoImageViewSend.setVisibility(View.VISIBLE);
                holder.photoImageView.setVisibility(View.GONE);
            } else {
                imageViewToLoad = holder.photoImageView;
                holder.photoImageViewSend.setVisibility(View.GONE);
                holder.photoImageView.setVisibility(View.VISIBLE);
            }

            // Load The Phone
            photoCache.loadPhoto(mContext ,imageViewToLoad, null, phone);
        } else {
            holder.photoImageView.setVisibility(View.GONE);
            holder.photoImageViewSend.setVisibility(View.GONE);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        // Then populate the ViewHolder
        ViewHolder holder = new ViewHolder();
        holder.timeAgoText = (TextView) view.findViewById(R.id.smslog_list_item_time_ago);
        holder.timeText = (TextView) view.findViewById(R.id.smslog_list_item_time);
        holder.phoneText = (TextView) view.findViewById(R.id.smslog_list_item_phone);
        holder.actionText = (TextView) view.findViewById(R.id.smslog_list_item_action);
        holder.smsType = (ImageView) view.findViewById(R.id.smslog_list_item_smsType_imgs);
        holder.photoImageView = (PhotoEditorView) view.findViewById(R.id.smslog_photo_imageView);
        holder.photoImageViewSend = (PhotoEditorView) view.findViewById(R.id.smslog_photo_imageView_send);
        // and store it inside the layout.
        view.setTag(holder);
        return view;
    }

    private String getSmsActionLabel(SmsMessageActionEnum action) {
        return action.getLabel(resources);
    }




    // ===========================================================
    // Photo Loader
    // ===========================================================

    static class ViewHolder {
        TextView actionText;
        TextView timeText;
        TextView timeAgoText;
        TextView phoneText;
        ImageView smsType;
        PhotoEditorView photoImageView;
        PhotoEditorView photoImageViewSend;
    }



    // ===========================================================
    // Others
    // ===========================================================

}
