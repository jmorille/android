package eu.ttbox.geoping.ui.smslog;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;

public class SmsLogListAdapter extends android.support.v4.widget.ResourceCursorAdapter {

    private SmsLogHelper helper;

    private boolean isNotBinding = true;

    public SmsLogListAdapter(Context context, Cursor c, int flags) {
        super(context, R.layout.smslog_list_item, c, flags); 
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
        helper.setTextSmsLogAction(holder.actionText, cursor)//
                .setTextSmsLogTime(holder.timeText, cursor) //
                .setTextSmsLogPhone(holder.phoneText, cursor);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);
        // Then populate the ViewHolder
        ViewHolder holder = new ViewHolder();
        holder.timeText = (TextView) view.findViewById(R.id.smslog_list_item_time);
        holder.phoneText = (TextView) view.findViewById(R.id.smslog_list_item_phone);
        holder.actionText= (TextView) view.findViewById(R.id.smslog_list_item_action);
        // and store it inside the layout.
        view.setTag(holder);
        return view;
    }

    static class ViewHolder {
    	TextView actionText;
        TextView timeText;
        TextView phoneText;
    }
}
