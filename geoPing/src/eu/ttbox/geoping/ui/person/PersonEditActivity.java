package eu.ttbox.geoping.ui.person;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.ui.smslog.SmsLogListFragment;

public class PersonEditActivity extends SherlockFragmentActivity {

	private static final String TAG = "PersonEditActivity";

	// Binding
	private PersonEditFragment editFragment;
	private SmsLogListFragment smsLogFragment;

	private SectionsPagerAdapter mSectionsPagerAdapter;
	private ViewPager mViewPager;
	// Instance
	private static final int VIEW_PAGER_LOADPERS_PAGE_COUNT = 2;
	private int viewPagerPageCount = 1;

	private String personId;
	private String personPhone;

	// ===========================================================
	// Listener
	// ===========================================================

	private PersonEditFragment.OnPersonSelectListener onPersonSelectListener = new PersonEditFragment.OnPersonSelectListener() {

		@Override
		public void onPersonSelect(String id, String phone) {
			// Check Update Phone
			if (!TextUtils.isEmpty(personPhone) && !TextUtils.isEmpty(phone)) {
				if (smsLogFragment!=null && !personPhone.equals(phone)) {
					Bundle args = new Bundle();
					args.putString(eu.ttbox.geoping.ui.smslog.SmsLogListFragment.Intents.EXTRA_SMS_PHONE, personPhone);
					args.putInt(eu.ttbox.geoping.ui.smslog.SmsLogListFragment.Intents.EXTRA_SIDE_DBCODE, SmsLogSideEnum.MASTER.getDbCode());
  					smsLogFragment.refreshLoader(args); 
				}
			}
			personId = id;
			personPhone = phone;
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
		setContentView(R.layout.track_person_edit_activity);
        // Add selector
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Pagers
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		// Fragment
		editFragment = new PersonEditFragment();
		editFragment.setOnPersonSelectListener(onPersonSelectListener);

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
		outState.putString(PersonColumns.COL_ID, personId);
		outState.putString(PersonColumns.COL_PHONE, personPhone);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		personId = savedInstanceState.getString(PersonColumns.COL_ID);
		personPhone = savedInstanceState.getString(PersonColumns.COL_PHONE);
		super.onRestoreInstanceState(savedInstanceState);
	}

	// ===========================================================
	// Menu
	// ===========================================================



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


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
			mViewPager.setCurrentItem(SectionsPagerAdapter.PERSON);
			// Prepare Edit
			String entityId = intent.getData().getLastPathSegment();
			// Set Fragment
			Bundle fragArgs = new Bundle();
			fragArgs.putString(Intents.EXTRA_PERSON_ID, entityId);
			editFragment.setArguments(fragArgs);
			// Tracker
			if (Intent.ACTION_DELETE.equals(action)) {
				// Delete
			} else {
				// Edit
			}
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mViewPager.setCurrentItem(SectionsPagerAdapter.PERSON); 
		}

	}

	// ===========================================================
	// Pages Adapter
	// ===========================================================

 

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		static final int PERSON = 0;
		// static final int PAIRING = 1;
		static final int LOG = 1;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			Fragment fragment = null;
			switch (position) {
			case PERSON:
				fragment = editFragment;
				break;
			case LOG:
				if (smsLogFragment == null) {
					Bundle args = new Bundle();
					args.putString(eu.ttbox.geoping.ui.smslog.SmsLogListFragment.Intents.EXTRA_SMS_PHONE, personPhone);
					args.putInt(eu.ttbox.geoping.ui.smslog.SmsLogListFragment.Intents.EXTRA_SIDE_DBCODE, SmsLogSideEnum.MASTER.getDbCode());
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
			case PERSON:
				return getString(R.string.menu_person).toUpperCase();
				// case PAIRING:
				// return getString(R.string.menu_pairing).toUpperCase();
			case LOG:
				return getString(R.string.menu_smslog).toUpperCase();
			}
			return null;
		}
	}
	// ===========================================================
	// Listener
	// ===========================================================

	// ===========================================================
	// Activity Result handler
	// ===========================================================

}
