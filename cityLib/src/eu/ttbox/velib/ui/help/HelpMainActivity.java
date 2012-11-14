package eu.ttbox.velib.ui.help;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import eu.ttbox.velib.R;
import eu.ttbox.velib.VelibMapActivity;
import eu.ttbox.velib.ui.preference.VelibPreferenceActivity;

public class HelpMainActivity extends FragmentActivity {

    private static final String TAG = "HelpMainActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
     * will keep every loaded fragment in memory. If this becomes too memory
     * intensive, it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_main_activity);
        // Create the adapter that will return a fragment for each of the three
        // primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    // ===========================================================
    // Menu
    // ===========================================================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menuOptions: {
            Intent intentOption = new Intent(this, VelibPreferenceActivity.class);
            startActivity(intentOption);
            return true;
        }
        case R.id.menu_search: {
            onSearchRequested();
            return true;
        }
        // case R.id.menuSearch: {
        // Intent intentSearch = new Intent(this, SearchableVeloActivity.class);
        // startActivity(intentSearch);
        // return true;
        // }
        case R.id.menuMap: {
            // Go To
            Intent intentMap = new Intent(this, VelibMapActivity.class);
            // intentMap.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intentMap);
            // overridePendingTransition(R.animator.push_right_in,
            // R.animator.push_right_out);
            return true;
        }
        case R.id.menuQuit: {
            finish();
            return true;
        }
        }
        return false;
    }

    // ===========================================================
    // Action Adapter
    // ===========================================================

    public void onStartMapActivityClick(View v) {
        Intent startMap = new Intent(this, VelibMapActivity.class);
        startActivity(startMap);
    }

    // ===========================================================
    // Pages Adapter
    // ===========================================================

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the primary sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        static final int CIRCLE_CODE = 0;
        static final int COLOR_CODE = 1;
        static final int BUBBLE_CODE = 2;
        static final int CONDUIT_CODE = 3;
        static final int MAP_CODE = 4;

//        Fragment circleColorFragment;
//        Fragment colorCodeFragment;
//        Fragment bubbleCodeFragment;
//        Fragment conduitCodeFragment;
//        Fragment mapCodeFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
            case CIRCLE_CODE:
//                if (circleColorFragment == null) {
                fragment = new Fragment() {
                        @Override
                        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                            View v = inflater.inflate(R.layout.help_circle, container, false);
                            return v;
                        }
                    };
//                }
//                fragment = circleColorFragment;
                break;
            case COLOR_CODE:
//                if (colorCodeFragment == null) {
                fragment = new Fragment() {
                        @Override
                        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                            View v = inflater.inflate(R.layout.help_color, container, false);
                            return v;
                        }
                    };
//                }
//                fragment = colorCodeFragment;
                break;
            case BUBBLE_CODE:
//                if (bubbleCodeFragment == null) {
                    fragment = new HelpStationDispoBubbleActivity();
//                }
//                fragment = bubbleCodeFragment;
                break;
            case CONDUIT_CODE:
//                if (conduitCodeFragment == null) {
                fragment = new Fragment() {
                        @Override
                        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                            View v = inflater.inflate(R.layout.help_conduite_code, container, false);
                            return v;
                        }
                    };
//                }
//                fragment = conduitCodeFragment;
                break;
            case MAP_CODE:
//                if (mapCodeFragment == null) {
                fragment = new Fragment() {
                        @Override
                        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
                            View v = inflater.inflate(R.layout.help_map, container, false);
                            return v;
                        }
                    };
//                }
//                fragment = mapCodeFragment;
                break;
            }
            // fragment = new DummySectionFragment();
            // Bundle args = new Bundle();
            // args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position +
            // 1);
            // fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case CIRCLE_CODE:
                return getString(R.string.help_tab_circle_code).toUpperCase();
            case COLOR_CODE:
                return getString(R.string.help_tab_color_code).toUpperCase();
            case BUBBLE_CODE:
                return getString(R.string.help_tab_bubble_dispo).toUpperCase();
            case CONDUIT_CODE:
                return getString(R.string.help_tab_conduit_code).toUpperCase();
            case MAP_CODE:
                return getString(R.string.help_tab_map_code).toUpperCase();
            }
            return null;
        }
    }

}
