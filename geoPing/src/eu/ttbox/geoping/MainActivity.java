package eu.ttbox.geoping;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import eu.ttbox.geoping.ui.MenuOptionsItemSelectionHelper;
import eu.ttbox.geoping.ui.pairing.PairingListFragment;
import eu.ttbox.geoping.ui.person.PersonListFragment;
import eu.ttbox.geoping.ui.smslog.SmsLogListFragment;

/**
 * TODO <br>
 * edit ./samples/android-17/ActionBarCompat/src/com/example/android/
 * actionbarcompat/ActionBarActivity.java <br>
 * edit ./samples/android-17/ActionBarCompat/src/com/example/android/
 * actionbarcompat/ActionBarHelper.java <br>
 * 
 * @author jmorille
 * 
 */
public class MainActivity extends SherlockFragmentActivity { //

	private static final String TAG = "MainActivity";

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

	// Pages
	private PersonListFragment personListFragment;
	private PairingListFragment pairingListFragment;
	private SmsLogListFragment smsLogListFragment;

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Create the adapter that will return a fragment for each of the three
		// primary sections
		// of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		// Tracker
		GeoPingApplication.getInstance().tracker().trackPageView("/" + TAG);
		// GoogleAnalyticsTracker tracker = ((GeoPingApplication)
		// getApplication()).tracker();
		// if (tracker != null) {
		// tracker.trackPageView("/" + TAG);
		// }
		Log.d(TAG, "--------------- " + getPackageName() + "_preferences");

	}

	// ===========================================================
	// Menu
	// ===========================================================

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_track_person: {
			mViewPager.setCurrentItem(SectionsPagerAdapter.PERSON);
			return true;
		}
		case R.id.menu_pairing: {
			mViewPager.setCurrentItem(SectionsPagerAdapter.PAIRING);
			return true;
		}
		case R.id.menu_smslog:
			mViewPager.setCurrentItem(SectionsPagerAdapter.LOG);
			return true;
		default:
			break;
		}
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
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
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
			Log.d(TAG, "getItem : " + position);
			Fragment fragment = null;
			switch (position) {
			case PERSON:
				if (personListFragment == null) {
					personListFragment = new PersonListFragment();
					Log.d(TAG, "Create Fragment PersonListFragment");
				}
				fragment = personListFragment;
				break;
			case PAIRING:
				if (pairingListFragment == null) {
					pairingListFragment = new PairingListFragment();
					Log.d(TAG, "Create Fragment PairingListFragment");
				}
				fragment = pairingListFragment;
				break;
			case LOG:
				if (smsLogListFragment == null) {
					smsLogListFragment = new SmsLogListFragment();
					Log.d(TAG, "Create Fragment SmsLogListFragment");
				}
				fragment = smsLogListFragment;
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
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			switch (position) {
			case PERSON:
				return getString(R.string.menu_person).toUpperCase();
			case PAIRING:
				return getString(R.string.menu_pairing).toUpperCase();
			case LOG:
				return getString(R.string.menu_smslog).toUpperCase();
			}
			return null;
		}
	}

}
