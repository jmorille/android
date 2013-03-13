package eu.ttbox.geoping.ui.billing;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import eu.ttbox.geoping.R;

public class ExtraFeaturesFragment extends Fragment {

    private static final String TAG = "ExtraFeaturesFragment";

    private ListView extraListView;
    
    // ===========================================================
    // Constructor
    // ===========================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.pairing_list, container, false);
        // Bindings
        extraListView = (ListView) v.findViewById(android.R.id.list);
        
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Create Adapter
        ExtraFeatureAdapter adapter = new ExtraFeatureAdapter(getActivity());
        adapter.add(new ExtraFeatureItem(R.id.menuMap, R.string.menu_map, R.drawable.ic_location_web_site));
        adapter.add(new ExtraFeatureItem(R.id.menu_track_person, R.string.menu_person, R.drawable.ic_action_user));
        adapter.add(new ExtraFeatureItem(R.id.menu_pairing, R.string.menu_pairing, R.drawable.ic_device_access_secure));
        adapter.add(new ExtraFeatureItem(R.id.menu_smslog, R.string.menu_smslog, R.drawable.ic_collections_go_to_today));
        adapter.add(new ExtraFeatureItem(R.id.menu_settings, R.string.menu_settings, android.R.drawable.ic_menu_preferences));
        adapter.add(new ExtraFeatureItem(R.id.menu_extra_feature, R.string.menu_extra_feature, android.R.drawable.ic_menu_more));

        // Binding Menu
        extraListView.setAdapter(adapter);
        extraListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ExtraFeatureItem menu = (ExtraFeatureItem) parent.getItemAtPosition(position);
                 
            }
        });
    }


    // ===========================================================
    // Slinding Menu Item
    // ===========================================================

    private class ExtraFeatureItem {
        public int itemId;
        public String tag;
        public int iconRes;

        public ExtraFeatureItem(int menuId, int tagId, int iconRes) {
            this.tag = getResources().getString(tagId);
            this.iconRes = iconRes;
            this.itemId = menuId;
        }
    }

    public class ExtraFeatureAdapter extends ArrayAdapter<ExtraFeatureItem> {

        public ExtraFeatureAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.slidingmenu_row, null);
                // Then populate the ViewHolder
                holder = new ViewHolder();
                holder.icon = (ImageView) convertView.findViewById(R.id.slidingmenu_item_icon);
                holder.title = (TextView) convertView.findViewById(R.id.slidingmenu_item_title);
                holder.selector = (ImageView) convertView.findViewById(R.id.slidingmenu_item_selector_icon);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            ExtraFeatureItem lineItem = getItem(position);
            holder.icon.setImageResource(lineItem.iconRes);
            holder.title.setText(lineItem.tag);
            // TODO Test Selector
//            Class<? extends Activity> expectedClass = getActivityClassByItemId(lineItem.itemId);
//            if (expectedClass.isAssignableFrom(getActivity().getClass())) {
//                holder.selector.setVisibility(View.VISIBLE);
//            } else {
//                holder.selector.setVisibility(View.GONE);
//            }

            return convertView;
        }

    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        ImageView selector;
    }

    // ===========================================================
    // Other
    // ===========================================================

}
