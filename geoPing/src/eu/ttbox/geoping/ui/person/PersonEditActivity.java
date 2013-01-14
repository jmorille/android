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
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
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
					args.putString(Intents.EXTRA_SMS_PHONE, personPhone);
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

	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_person_edit, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save:
			editFragment.onSaveClick();
			finish();
			return true;
		case R.id.menu_delete:
			editFragment.onDeleteClick();
			return true;
		case R.id.menu_select_contact:
			editFragment.onSelectContactClick(null);
			return true;
		case R.id.menu_cancel:
			editFragment.onCancelClick();
			return true;
		case R.id.menuQuitter:
			// Pour fermer l'application il suffit de faire finish()
			finish();
			return true;
		}
		return false;
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
				GeoPingApplication.getInstance().tracker().trackPageView("/Person/delete");
			} else {
				GeoPingApplication.getInstance().tracker().trackPageView("/Person/edit");
			}
		} else if (Intent.ACTION_INSERT.equals(action)) {
			mViewPager.setCurrentItem(SectionsPagerAdapter.PERSON);
			// Tracker
			GeoPingApplication.getInstance().tracker().trackPageView("/Person/insert");
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
					args.putString(Intents.EXTRA_SMS_PHONE, personPhone);
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
