package eu.ttbox.geoping;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;

import eu.ttbox.geoping.ui.MenuOptionsItemSelectionHelper;
import eu.ttbox.geoping.ui.pairing.PairingListFragment;
import eu.ttbox.geoping.ui.person.PersonListFragment;
import eu.ttbox.geoping.ui.person.PhotoThumbmailCache;
import eu.ttbox.geoping.ui.smslog.SmsLogListFragment;

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

	private PhotoThumbmailCache photoCache;

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Photo Cache
		initPhotoThumbmailCache();
		personListFragment = new PersonListFragment(photoCache);
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

	private void initPhotoThumbmailCache() {
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		int memoryClassBytes = am.getMemoryClass() * 1024 * 1024;
		int cacheSize = memoryClassBytes / 8; // 307000 * 10
		Log.i(TAG, "Create Cache of PhotoThumbmailCache wih size " + cacheSize);
		photoCache = new PhotoThumbmailCache(cacheSize); 
	}


	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (photoCache != null) {
			photoCache.onLowMemory();
		}
	}

	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		if (photoCache != null) {
			photoCache.onTrimMemory(level);
		}
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
					personListFragment = new PersonListFragment(photoCache);
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
