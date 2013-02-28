package eu.ttbox.geoping.ui.slidingmenu;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import eu.ttbox.geoping.R;
/**
 * @see SampleListFragment 
 *
 */
public class GeopingSlidingMenuFragment extends ListFragment {
 
    
    public GeopingSlidingMenuFragment() {
        super(); 
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.slidingmenu_list, null);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SampleAdapter adapter = new SampleAdapter(getActivity());
        adapter.add(new SampleItem(R.string.menu_map, R.drawable.ic_location_web_site));

        adapter.add(new SampleItem(R.string.menu_person, R.drawable.ic_action_user));
        adapter.add(new SampleItem(R.string.menu_pairing, R.drawable.ic_device_access_secure));
        adapter.add(new SampleItem(R.string.menu_smslog, R.drawable.ic_collections_go_to_today));
        adapter.add(new SampleItem(R.string.menu_settings, android.R.drawable.ic_menu_preferences)); 
        //
        setListAdapter(adapter);
    
    }

    private class SampleItem {
        public String tag;
        public int iconRes;
        public SampleItem(int tagId, int iconRes) {
            this.tag =   getResources().getString(tagId);
            this.iconRes = iconRes;
        }
    }

    public class SampleAdapter extends ArrayAdapter<SampleItem> {

        public SampleAdapter(Context context) {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.slidingmenu_row, null);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.row_icon);
            icon.setImageResource(getItem(position).iconRes);
            TextView title = (TextView) convertView.findViewById(R.id.row_title);
            title.setText(getItem(position).tag);
 
            return convertView;
        }
        
        

    }
}
