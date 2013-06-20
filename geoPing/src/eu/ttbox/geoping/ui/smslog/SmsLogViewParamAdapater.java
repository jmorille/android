package eu.ttbox.geoping.ui.smslog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.service.billing.util.SkuDetails;


public class SmsLogViewParamAdapater extends ArrayAdapter<SmsLogParamVO> {


    private static final String TAG = "SmsLogViewParamAdapater";


    public SmsLogViewParamAdapater(Context context) {
        super(context, R.layout.smslog_view_list_param_item, 0);
    }



    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.smslog_view_list_param_item, null);
            // Then populate the ViewHolder
            holder = new ViewHolder();
            holder.keyTextView = (TextView) convertView.findViewById(R.id.smslog_list_item_param_key);
            holder.valueTextView = (TextView) convertView.findViewById(R.id.smslog_list_item_param_value);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        SmsLogParamVO lineItem = getItem(position);
        holder.keyTextView.setText(lineItem.key);
        holder.valueTextView.setText(lineItem.value);

        return convertView;
    }

    static class ViewHolder {
        TextView keyTextView;
        TextView valueTextView;
    }


}
