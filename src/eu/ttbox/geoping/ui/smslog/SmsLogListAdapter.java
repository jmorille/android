package eu.ttbox.geoping.ui.smslog;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;

public class SmsLogListAdapter extends android.support.v4.widget.ResourceCursorAdapter {

    private SmsLogHelper helper;

    private boolean isNotBinding = true;

    private Resources mResources;

    // ===========================================================
    // Constructor
    // ===========================================================

    public SmsLogListAdapter(Context context, Cursor c, int flags) {
        super(context, R.layout.smslog_list_item, c, flags);
        mResources = new Resources(context);
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
        Drawable iconType = getCallTypeDrawable(smLogType);
        holder.smsType.setImageDrawable(iconType);
        // Text
        SmsMessageActionEnum action = helper.getSmsMessageActionEnum(cursor);
        String actionLabel = getSmsActionLabel(action);
        holder.actionText.setText(actionLabel);
        // Phone
        helper.setTextSmsLogPhone(holder.phoneText, cursor);
        // Time
        long time = helper.getSmsLogTime(cursor);
        String timeFormat = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS", time);
        holder.timeText.setText(timeFormat);

    }

  


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        // Then populate the ViewHolder
        ViewHolder holder = new ViewHolder();
        holder.timeText = (TextView) view.findViewById(R.id.smslog_list_item_time);
        holder.phoneText = (TextView) view.findViewById(R.id.smslog_list_item_phone);
        holder.actionText = (TextView) view.findViewById(R.id.smslog_list_item_action);
        holder.smsType = (ImageView) view.findViewById(R.id.smslog_list_item_smsType_imgs);
        // and store it inside the layout.
        view.setTag(holder);
        return view;
    }

    static class ViewHolder {
        TextView actionText;
        TextView timeText;
        TextView phoneText;
        ImageView smsType;
    }


    private Drawable getCallTypeDrawable(SmsLogTypeEnum callType) {
        switch (callType) {
        case RECEIVE:
            return mResources.incoming;
        case SEND:
            return mResources.outgoing;
        default:
            throw new IllegalArgumentException("invalid call type: " + callType);
        }
    }
    
    private String getSmsActionLabel(SmsMessageActionEnum action) {
        switch (action) {
        case GEOPING_REQUEST:
            return mResources.actionGeoPingRequest;
        case ACTION_GEO_LOC:
            return mResources.actionGeoPingResponse;
        case ACTION_GEO_PAIRING:
            return mResources.actionPairingRequest;
        case ACTION_GEO_PAIRING_RESPONSE:
            return mResources.actionPairingResponse;
         default:
            return action.name();
        }
    }
    
    private static class Resources {
        public final Drawable incoming;
        public final Drawable outgoing;
        public final Drawable missed;
        public final Drawable voicemail;
        public final String actionGeoPingRequest;
        public final String actionGeoPingResponse;
        public final String actionPairingRequest;
        public final String actionPairingResponse;
        
        public Resources(Context context) {
            final android.content.res.Resources r = context.getResources();
            incoming = r.getDrawable(R.drawable.ic_call_incoming_holo_dark);
            outgoing = r.getDrawable(R.drawable.ic_call_outgoing_holo_dark);
            missed = r.getDrawable(R.drawable.ic_call_missed_holo_dark);
            voicemail = r.getDrawable(R.drawable.ic_call_voicemail_holo_dark);
            // Text
            actionGeoPingRequest = r.getString(R.string.sms_action_geoping_request);
            actionGeoPingResponse = r.getString(R.string.sms_action_geoping_response);
            actionPairingRequest = r.getString(R.string.sms_action_pairing_request);
            actionPairingResponse = r.getString(R.string.sms_action_pairing_response);
        }
    }
}
