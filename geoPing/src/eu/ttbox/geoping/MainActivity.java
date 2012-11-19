package eu.ttbox.geoping;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import eu.ttbox.geoping.ui.MenuOptionsItemSelectionHelper;
import eu.ttbox.geoping.ui.pairing.PairingListFragment;
import eu.ttbox.geoping.ui.person.PersonListFragment;
import eu.ttbox.geoping.ui.smslog.SmsLogListFragment;

public class MainActivity extends FragmentActivity {

    private static final String TAG = "MainActivity";

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
     * sections. We use a {@link android.support.v4.app.FragmentPagerAdapter} derivative, which will
     * keep every loaded fragment in memory. If this becomes too memory intensive, it may be best
     * to switch to a {@link android.support.v4.app.FragmentStatePagerAdapter}.
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
        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three primary sections
        // of the app.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());


        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // Tracker
        GoogleAnalyticsTracker tracker = ((GeoPingApplication)getApplication()).getTracker();
        tracker.trackPageView("/"+TAG);

    }
    
    // ===========================================================
    // Menu
    // ===========================================================


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

  
     
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean isConsume = MenuOptionsItemSelectionHelper.onOptionsItemSelected(this, item);
        if (isConsume) {
            return isConsume;
        } else {
            switch (item.getItemId()) {
            case R.id.menuQuitter:
                // Pour fermer l'application il suffit de faire finish()
                finish();
                return true;
            }
        }
        return false;
    }

    // ===========================================================
    // Pages Adapter
    // ===========================================================

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
     * sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

       static final int PERSON = 0;
       static final int PAIRING = 1;
       static final int LOG = 2;
       
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
            case PERSON: 
                fragment=new PersonListFragment();
                break;
            case PAIRING: 
                fragment=new PairingListFragment();
                break;
            case LOG: 
                fragment=new SmsLogListFragment();
                break;
        }
//            fragment = new DummySectionFragment();
//            Bundle args = new Bundle();
//            args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, position + 1);
//            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case PERSON: return getString(R.string.menu_person).toUpperCase();
                case PAIRING: return getString(R.string.menu_pairing).toUpperCase();
                case LOG: return getString(R.string.menu_smslog).toUpperCase();
            }
            return null;
        }
    }

    /**
     * A dummy fragment representing a section of the app, but that simply displays dummy text.
     */
    public static class DummySectionFragment extends Fragment {
        public DummySectionFragment() {
        }

        public static final String ARG_SECTION_NUMBER = "section_number";

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            TextView textView = new TextView(getActivity());
            textView.setGravity(Gravity.CENTER);
            Bundle args = getArguments();
            textView.setText(Integer.toString(args.getInt(ARG_SECTION_NUMBER)));
            return textView;
        }
    }
}
