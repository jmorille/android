package eu.ttbox.geoping;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.CanvasTransformer;

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

	private com.actionbarsherlock.widget.ShareActionProvider mShareActionProvider;

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
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// configure the SlidingMenu
       final SlidingMenu slidingMenu = new SlidingMenu(this);
         slidingMenu.setMode(SlidingMenu.LEFT);
        slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN); 
        slidingMenu.setShadowWidthRes(R.dimen.shadow_width);
        slidingMenu.setShadowDrawable(R.drawable.shadow);
        slidingMenu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
        slidingMenu.setFadeDegree(0.35f);
        slidingMenu.setBehindScrollScale(0.35f); 
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        slidingMenu.setMenu(R.layout.slidingmenu_menu);
        mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) { }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                case 0:
                    slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN); 
                    break;
                default:
                    slidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN); 
                    break;
                }
            }

        });
        CanvasTransformer smTransformer = new CanvasTransformer() {
            @Override
            public void transformCanvas(Canvas canvas, float percentOpen) {
                float scale = (float) (percentOpen*0.25 + 0.75);
                canvas.scale(scale, scale, canvas.getWidth()/2, canvas.getHeight()/2);
            }
        };
        slidingMenu.setBehindCanvasTransformer(smTransformer);
        
        
        // Tracker
        GeoPingApplication.getInstance().tracker().trackPageView("/" + TAG);
 
	}

	// ===========================================================
	// Menu
	// ===========================================================

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu, menu);
		// Share
		MenuItem itemShare = menu.findItem(R.id.menuAppShare);
		mShareActionProvider = (com.actionbarsherlock.widget.ShareActionProvider) itemShare.getActionProvider();
		// Share Inten
 
		mShareActionProvider.setShareIntent(createShareIntent(this));
		return true;
	}

	private Intent createShareIntent(Context context) {
		Intent shareAppIntent = new Intent(Intent.ACTION_SEND);
		shareAppIntent.setType("text/plain"); 
		shareAppIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_app_subject));
//		shareAppIntent.putExtra(Intent.EXTRA_TEXT, "geoPing://pairing?id=eu.ttbox.geoping");
		shareAppIntent.putExtra(Intent.EXTRA_TEXT, "market://details?id=eu.ttbox.geoping");
		return shareAppIntent;
	}

	private void setShareItent(Intent shareIntent) {
		if (mShareActionProvider != null) {
			mShareActionProvider.setShareIntent(shareIntent);
		}
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
			// case R.id.menuAppShare:
			// Intent shareAppIntent = new Intent(Intent.ACTION_SEND);
			// shareAppIntent.putExtra(Intent.EXTRA_TEXT,
			// "market://details?id=eu.ttbox.geoping");
			// setShareItent(shareAppIntent);
			// return true;
		default:
			break;
		}
		boolean isConsume = MenuOptionsItemSelectionHelper.onOptionsItemSelected(this, item);
		if (isConsume) {
			return isConsume;
		} else {
			// switch (item.getItemId()) {
			// case R.id.menuQuitter:
			// // Pour fermer l'application il suffit de faire finish()
			// finish();
			// return true;
			// }
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
