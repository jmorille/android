package eu.ttbox.geoping.ui.geofence;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.ui.geofence.GeofenceEditFragment;
import eu.ttbox.geoping.ui.smslog.SmsLogListFragment;

public class GeofenceEditActivity extends SherlockFragmentActivity {

    private static final String TAG = "GeofenceEditActivity";

    // Binding
    private GeofenceEditFragment editFragment;
    private SmsLogListFragment smsLogFragment;


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;
    // Instance
    private static final int VIEW_PAGER_LOADPERS_PAGE_COUNT = 1;
    private int viewPagerPageCount = 1;

    private Uri geofenceUri;
    private String geofencePhone;

    // ===========================================================
    // Listener
    // ===========================================================

    private GeofenceEditFragment.OnGeofenceSelectListener onGeofenceSelectListener = new GeofenceEditFragment.OnGeofenceSelectListener() {

        @Override
        public void onGeofenceSelect(Uri id, CircleGeofence fence) {
            // Check Update Phone
//            if (!TextUtils.isEmpty(geofencePhone) && !TextUtils.isEmpty(phone)) {
//                if (smsLogFragment != null && !geofencePhone.equals(phone)) {
//                    Bundle args = new Bundle();
//                    args.putString(SmsLogListFragment.Intents.EXTRA_SMS_PHONE, geofencePhone);
//                    args.putInt(SmsLogListFragment.Intents.EXTRA_SIDE_DBCODE, SmsLogSideEnum.SLAVE.getDbCode());
//                    smsLogFragment.refreshLoader(args);
//                }
//            }
            geofenceUri = id;
//            geofencePhone = phone;
            // Update Ui Tabs
            if (viewPagerPageCount != VIEW_PAGER_LOADPERS_PAGE_COUNT) {
                viewPagerPageCount = VIEW_PAGER_LOADPERS_PAGE_COUNT;
                mSectionsPagerAdapter.notifyDataSetChanged();
            }
        }

    };

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.geofence_edit_activity);
        // Pagers
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        // Fragment
        editFragment = new GeofenceEditFragment();
        editFragment.setOnGeofenceSelectListener( onGeofenceSelectListener);

        // Analytic
        mViewPager.setAdapter(mSectionsPagerAdapter);
        // Intents
        handleIntent(getIntent());
        // Tracker
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Tracker
        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (geofenceUri != null) {
            outState.putString(PersonColumns.COL_ID, geofenceUri.toString());
            outState.putString(PersonColumns.COL_PHONE, geofencePhone);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String pariringUriString = savedInstanceState.getString(PersonColumns.COL_ID);
        if (pariringUriString != null) {
            geofenceUri = Uri.parse(pariringUriString);
            geofencePhone = savedInstanceState.getString(PersonColumns.COL_PHONE);
        }
    }

    // ===========================================================
    // Life Cycle
    // ===========================================================

    // ===========================================================
    // Menu
    // ===========================================================



    // ===========================================================
    // Intent Handler
    // ===========================================================

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        Log.d(TAG, "handleIntent for action : " + action);

        if (Intent.ACTION_EDIT.equals(action) || Intent.ACTION_DELETE.equals(action)) {
            mViewPager.setCurrentItem(SectionsPagerAdapter.GEOFENCE);
            // Prepare Edit
            Uri entityUri = intent.getData();
            // Set Fragment
            Bundle fragArgs = new Bundle();
            fragArgs.putString(Intents.EXTRA_PERSON_ID, entityUri.toString());
            editFragment.setArguments(fragArgs);

        } else if (Intent.ACTION_INSERT.equals(action)) {
            mViewPager.setCurrentItem(SectionsPagerAdapter.GEOFENCE);
        }

    }

    // ===========================================================
    // Pages Adapter
    // ===========================================================

    /**
     * A {@link android.support.v4.app.FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the primary sections of the app.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        static final int GEOFENCE = 0;
        // static final int GEOFENCE = 1;
        static final int LOG = 2;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
            case GEOFENCE:
                fragment = editFragment;
                break;

            case LOG:
                if (smsLogFragment == null) {
                    Bundle args = new Bundle();
                    args.putString(SmsLogListFragment.Intents.EXTRA_SMS_PHONE, geofencePhone);
                    args.putInt(SmsLogListFragment.Intents.EXTRA_SIDE_DBCODE, SmsLogSideEnum.SLAVE.getDbCode());
                    smsLogFragment = new SmsLogListFragment();
                    smsLogFragment.setArguments(args);
                }
                fragment = smsLogFragment;
                break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return viewPagerPageCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
            case GEOFENCE:
                return getString(R.string.menu_geofence).toUpperCase();
            case LOG:
                return getString(R.string.menu_smslog).toUpperCase();
            }
            return null;
        }
    }
    // ===========================================================
    // Listener
    // ===========================================================

}
