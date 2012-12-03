package eu.ttbox.geoping.ui.person;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.ui.smslog.SmsLogListFragment;

public class PersonEditActivity extends SherlockFragmentActivity {

	private static final String TAG = "PersonEditActivity";

	private PersonEditFragment editFragment;

	SectionsPagerAdapter mSectionsPagerAdapter;
	ViewPager mViewPager;

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.w(TAG, "----------------------- Activity onCreate Begin ");
 		super.onCreate(savedInstanceState);
		setContentView(R.layout.track_person_edit_activity);
		// Pagers
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		// Fragment
		editFragment = new PersonEditFragment();
		// Analytic
		mViewPager.setAdapter(mSectionsPagerAdapter);
		// Tracker
		
		// Intents
		Log.w(TAG, "----------------------- Activity onCreate ");
		 handleIntent(getIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.w(TAG, "----------------------- Activity onResume ");
//		handleIntent(getIntent());
	}

 

	@Override
	public void onStart() {
		super.onStart();
		Log.w(TAG, "----------------------- Activity onStart ");
//		handleIntent(getIntent());
	}

	// @Override
	// public void onAttachFragment(Fragment fragment) {
	// super.onAttachFragment(fragment);
	// if (fragment instanceof PersonEditFragment) {
	// editFragment = (PersonEditFragment) fragment;
	// }
	// }

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
		if (Intent.ACTION_EDIT.equals(action) || Intent.ACTION_DELETE.equals(action) ) {
			mViewPager.setCurrentItem(SectionsPagerAdapter.PERSON);
			// Prepare Edit
 	   		String entityId = intent.getData().getLastPathSegment();
	   		// Set Fragment
	   		Bundle fragArgs = new Bundle();
			fragArgs.putString(Intents.EXTRA_PERSON_ID,entityId) ;
			editFragment.setArguments(fragArgs);
			// Tracker
			GeoPingApplication.getInstance().tracker().trackPageView("/Person/edit");
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
				fragment = new SmsLogListFragment();
				break;
			}
			return fragment;
		}

		@Override
		public int getCount() {
			return 1;
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
