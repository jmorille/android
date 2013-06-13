package eu.ttbox.geoping.ui.slidingmenu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.slidingmenu.lib.app.SlidingActivityBase;

import eu.ttbox.geoping.GeoTrakerActivity;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.ui.billing.PayFeaturesActivity;
import eu.ttbox.geoping.ui.map.ShowMapActivity;
import eu.ttbox.geoping.ui.pairing.PairingListActivity;
import eu.ttbox.geoping.ui.person.PersonListActivity;
import eu.ttbox.geoping.ui.prefs.GeoPingPrefActivity;
import eu.ttbox.geoping.ui.smslog.SmsLogListActivity;

/**
 * @see SampleListFragment
 * 
 */
@Deprecated
public class GeopingSlidingMenuFragment extends Fragment {

    private static final String TAG = "GeopingSlidingMenuFragment";

    private static final int SLIDINGMENU_PERSON_LIST_LOADER = R.id.config_id_slidingmenu_person_list_loader;
    private static final String PERSON_SORT_DEFAULT = String.format("%s DESC, %s DESC", PersonColumns.COL_NAME, PersonColumns.COL_PHONE);

    private ScrollView slidingmenuContainer;
    private ListView menuListView;
    private ListView personListView;
    private SlidingPersonListAdapter personAdpater;

    // ===========================================================
    // Constructors
    // ===========================================================

    public GeopingSlidingMenuFragment() {
        super();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "--- ---------------------------------- ----");
        Log.d(TAG, "--- SlidingMenuFragment - onCreateView ----");
        Log.d(TAG, "--- ---------------------------------- ----");
        View v = inflater.inflate(R.layout.slidingmenu_list, null);
        menuListView = (ListView) v.findViewById(android.R.id.list);
        personListView = (ListView) v.findViewById(R.id.person_list);
        slidingmenuContainer = (ScrollView)v.findViewById(R.id.slidingmenu_container); 
        // Ugly way to display always the top
        slidingmenuContainer.post(new Runnable() { 
            public void run() { 
                slidingmenuContainer.fullScroll(ScrollView.FOCUS_UP); 
//                slidingmenuContainer.scrollTo(0, 0);
            } 
        }); 
        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "--- --------------------------------------- ----");
        Log.d(TAG, "--- SlidingMenuFragment - onActivityCreated ----");
        Log.d(TAG, "--- --------------------------------------- ----");
        
        SlidingMenuAdapter adapter = new SlidingMenuAdapter(getActivity());
        adapter.add(new SlindingMenuItem(R.id.menuMap, R.string.menu_map, R.drawable.ic_location_web_site));
        adapter.add(new SlindingMenuItem(R.id.menu_track_person, R.string.menu_person, R.drawable.ic_action_user));
        adapter.add(new SlindingMenuItem(R.id.menu_pairing, R.string.menu_pairing, R.drawable.ic_device_access_secure));
        adapter.add(new SlindingMenuItem(R.id.menu_smslog, R.string.menu_smslog, R.drawable.ic_collections_go_to_today));
        adapter.add(new SlindingMenuItem(R.id.menu_settings, R.string.menu_settings, android.R.drawable.ic_menu_preferences));
        adapter.add(new SlindingMenuItem(R.id.menu_extra_feature, R.string.menu_extra_feature, android.R.drawable.ic_menu_more));

        // Binding Menu
        menuListView.setAdapter(adapter);
        menuListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SlindingMenuItem menu = (SlindingMenuItem) parent.getItemAtPosition(position);
                boolean isSlide = onSlidingMenuSelectItem(menu.itemId);
                if (isSlide) {
                    switchFragment();
                }
            }
        });
        // Binding Person
        personAdpater = new SlidingPersonListAdapter(getActivity(), null, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        personListView.setAdapter(personAdpater);
        getActivity().getSupportLoaderManager().initLoader(SLIDINGMENU_PERSON_LIST_LOADER, null, slidingmenuPersonLoaderCallback);
    }

    // ===========================================================
    // Sliding Menu Select Item
    // ===========================================================

    private boolean onSlidingMenuSelectItem(int itemId) {
        Context context = getActivity();
        switch ( itemId) {
        case R.id.menu_settings:
        case R.id.menuGeotracker:
        case R.id.menuMap:
        case R.id.menu_pairing:
        case R.id.menu_track_person:
        case R.id.menu_smslog:
        case R.id.menu_extra_feature:
            Class<? extends Activity> intentClass = getActivityClassByItemId(itemId);
            if (intentClass != null) {
                Intent intentOption = new Intent(context, intentClass);
                context.startActivity(intentOption);
                return true;
            }
            return false;

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

    private Class<? extends Activity> getActivityClassByItemId(int itemId) {
        switch (itemId) {
        case R.id.menu_settings:
            return GeoPingPrefActivity.class;
        case R.id.menuGeotracker:
            return GeoTrakerActivity.class;
        case R.id.menuMap:
            return ShowMapActivity.class;
        case R.id.menu_pairing:
            return PairingListActivity.class;
        case R.id.menu_track_person:
            return PersonListActivity.class;
        case R.id.menu_smslog:
            return SmsLogListActivity.class;
        case R.id.menu_extra_feature:
            return PayFeaturesActivity.class;
        default:
            return null;
        }
    }

    // the meat of switching the above fragment
    private void switchFragment() {
        if (getActivity() == null)
            return;

        if (getActivity() instanceof SlidingActivityBase) {
            SlidingActivityBase fca = (SlidingActivityBase) getActivity();
            fca.showContent();
        }
    }

    // ===========================================================
    // Slinding Menu Item
    // ===========================================================

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

    public class SlidingMenuAdapter extends ArrayAdapter<SlindingMenuItem> {

        public SlidingMenuAdapter(Context context) {
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
            SlindingMenuItem lineItem = getItem(position);
            holder.icon.setImageResource(lineItem.iconRes);
            holder.title.setText(lineItem.tag);
            // TODO Test Selector
            Class<? extends Activity> expectedClass = getActivityClassByItemId(lineItem.itemId);
            if (expectedClass.isAssignableFrom(getActivity().getClass())) {
                holder.selector.setVisibility(View.VISIBLE);
            } else {
                holder.selector.setVisibility(View.GONE);
            }

            return convertView;
        }

    }

    static class ViewHolder {
        ImageView icon;
        TextView title;
        ImageView selector;
    }

    // ===========================================================
    // Slinding Menu Person Item
    // ===========================================================

    // ===========================================================
    // Loader
    // ===========================================================

    private final LoaderManager.LoaderCallbacks<Cursor> slidingmenuPersonLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String sortOrder = PERSON_SORT_DEFAULT;
            String selection = null;
            String[] selectionArgs = null;
            String queryString = null;
            // Loader
            CursorLoader cursorLoader = new CursorLoader(getActivity(), PersonProvider.Constants.CONTENT_URI, null, selection, selectionArgs, sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

            // Display List
            personAdpater.changeCursor(cursor);
            cursor.setNotificationUri(getActivity().getContentResolver(), PersonProvider.Constants.CONTENT_URI);
            // Display Counter
            int count = 0;
            if (cursor != null) {
                count = cursor.getCount();
            }
            Log.d(TAG, "onLoadFinished with result count : " + count);

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            personAdpater.changeCursor(null);
        }

    };

}
