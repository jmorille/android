package eu.ttbox.geoping.ui.billing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.service.billing.util.SkuDetails;


public class SkuDetailsListAdapter extends ArrayAdapter<SkuDetails> {

        public SkuDetailsListAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.sku_details_list_item, null);
                // Then populate the ViewHolder
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.skuDetails_title_textView);
                holder.description = (TextView) convertView.findViewById(R.id.skuDetails_description_textView);
                holder.price = (TextView) convertView.findViewById(R.id.skuDetails_price_textView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            SkuDetails lineItem = getItem(position);
            holder.title.setText(lineItem.getTitle());
            holder.description.setText(lineItem.getDescription());
            holder.price.setText(lineItem.getPrice());

            return convertView;
        }

    static class ViewHolder {
        TextView title;
        TextView description;
        TextView price;
    }
}


