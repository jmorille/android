package eu.ttbox.geoping.ui.slidingmenu;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import eu.ttbox.geoping.GeoTrakerActivity;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.ui.map.ShowMapActivity;
import eu.ttbox.geoping.ui.pairing.PairingListActivity;
import eu.ttbox.geoping.ui.person.PersonListActivity;
import eu.ttbox.geoping.ui.prefs.GeoPingPrefActivity;
import eu.ttbox.geoping.ui.smslog.SmsLogListActivity;

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
        adapter.add(new SlindingMenuItem(R.id.menuMap, R.string.menu_map, R.drawable.ic_location_web_site)); 
        adapter.add(new SlindingMenuItem(R.id.menu_track_person, R.string.menu_person, R.drawable.ic_action_user));
        adapter.add(new SlindingMenuItem(R.id.menu_pairing, R.string.menu_pairing, R.drawable.ic_device_access_secure));
        adapter.add(new SlindingMenuItem(R.id.menu_smslog, R.string.menu_smslog, R.drawable.ic_collections_go_to_today));
        adapter.add(new SlindingMenuItem(R.id.menu_settings, R.string.menu_settings, android.R.drawable.ic_menu_preferences));
        //
        setListAdapter(adapter);
        getListView().setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SlindingMenuItem menu = (SlindingMenuItem) parent.getItemAtPosition(position);
                onSlidingMenuSelectItem(menu);
            }
        });
    }

    private boolean onSlidingMenuSelectItem(SlindingMenuItem cursor) {
        Context  context = getActivity();
        switch (cursor.itemId) {
        case R.id.menu_settings:
            Intent intentOption = new Intent(context, GeoPingPrefActivity.class);
            context.startActivity(intentOption);
            return true;
        case R.id.menuGeotracker:
            Intent intentGeoTraker = new Intent(context, GeoTrakerActivity.class);
            context.startActivity(intentGeoTraker);
            return true;
        case R.id.menuMap:
            Intent intentMap = new Intent(context, ShowMapActivity.class);
            context.startActivity(intentMap);
            return true;
        case R.id.menu_pairing:
            Intent intentPairing = new Intent(context, PairingListActivity.class);
            context.startActivity(intentPairing);
            return true;
        case R.id.menu_track_person:
            Intent intentGeoPing = new Intent(context, PersonListActivity.class);
            context.startActivity(intentGeoPing);
            return true;
        case R.id.menu_smslog:
            Intent intentSmsLog = new Intent(context, SmsLogListActivity.class);
            context.startActivity(intentSmsLog);
            return true;
        case R.id.menuAppComment:
            Intents.startActivityAppMarket(context);
            return true;

            // case R.id.menuAppShare:
            // Intent intentAppShare = new Intent(Intent.ACTION_SEND);
            // intentAppShare.putExtra(Intent.EXTRA_TEXT,
            // "market://details?id=eu.ttbox.geoping");
            // return true;

        }
        return false;
    }
    
    private class SlindingMenuItem {
        public int itemId;
        public String tag;
        public int iconRes;

        public SlindingMenuItem(int menuId, int tagId, int iconRes) {
            this.tag = getResources().getString(tagId);
            this.iconRes = iconRes;
            this.itemId = menuId;
        }
    }

    public class SampleAdapter extends ArrayAdapter<SlindingMenuItem> {

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
