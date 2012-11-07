package eu.ttbox.velib.ui.help;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.TabHost;
import eu.ttbox.velib.R;
import eu.ttbox.velib.VelibMapActivity;
import eu.ttbox.velib.ui.preference.VelibPreferenceActivity;

/**
 * customize @see http://joshclemm.com/blog/?p=136 to animate
 * 
 * @see http://stackoverflow.com/questions/4952797/how-to-animate-tabactivity-change-in-android
 * @see http://stackoverflow.com/questions/4077440/add-animation-when-changing-tabs
 * 
 * Scrool HorizontalView by code @see http://stackoverflow.com/questions/4799978/how-to-programmatically-scroll-an-horizontalscrollview
 * 
 *      gesture http://mobile.tutsplus.com/tutorials/android/android-gesture/
 *      @see {@link http://android-developers.blogspot.fr/2011/08/horizontal-view-swiping-with-viewpager.html}
 * @author jmorille
 * 
 */
public class HelpTabLayoutActivity extends TabActivity implements OnGestureListener {

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private static final int ANIMATIION_DURATION = 250;

	private GestureDetector gestureDetector;
	private TabHost tabsHost;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);

//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//			SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
//			SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
//			searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
//			searchView.setIconifiedByDefault(false);
//		}
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_main);

		Resources res = getResources(); // Resource object to get Drawables
		tabsHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Register
		// Gesture
		gestureDetector = new GestureDetector(this, this);

		// Table one
		intent = new Intent().setClass(this, HelpCircleActivity.class);
		spec = tabsHost.newTabSpec("help_circle").setIndicator(res.getString(R.string.help_tab_circle_code)).setContent(intent);
		tabsHost.addTab(spec);

		// Table two
		intent = new Intent().setClass(this, HelpColorActivity.class);
		spec = tabsHost.newTabSpec("help_color").setIndicator(res.getString(R.string.help_tab_color_code)).setContent(intent);
		tabsHost.addTab(spec);

		// Table tree
		intent = new Intent().setClass(this, HelpStationDispoBubbleActivity.class);
		spec = tabsHost.newTabSpec("help_buble").setIndicator(res.getString(R.string.help_tab_bubble_dispo)).setContent(intent);
		tabsHost.addTab(spec);

		// Table four
		intent = new Intent().setClass(this, HelpConduiteCodeActivity.class);
		spec = tabsHost.newTabSpec("help_conduit_code").setIndicator(res.getString(R.string.help_tab_conduit_code)).setContent(intent);
		tabsHost.addTab(spec);

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
//			overridePendingTransition(R.animator.push_right_in, R.animator.push_right_out);
			return true;
		}
		case R.id.menuQuit: {
			finish();
			return true;
		}
		}
		return false;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (gestureDetector != null) {
			if (gestureDetector.onTouchEvent(ev))
				return true;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

		// Check movement along the Y-axis. If it exceeds SWIPE_MAX_OFF_PATH, then dismiss the swipe.
		if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
			return false;

		// Swipe from right to left.
		// The swipe needs to exceed a certain distance (SWIPE_MIN_DISTANCE) and a certain velocity (SWIPE_THRESHOLD_VELOCITY).
		if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			// do stuff
			changeToTabIncrement(1);
			return true;
		}

		// Swipe from left to right.
		// The swipe needs to exceed a certain distance (SWIPE_MIN_DISTANCE) and a certain velocity (SWIPE_THRESHOLD_VELOCITY).
		if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
			// do stuff
			changeToTabIncrement(-1);
			return true;
		}

		return false;
	}

	public Animation inFromRightAnimation(float inverse) {
		Animation inFromRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, inverse * +1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		inFromRight.setDuration(ANIMATIION_DURATION);
		inFromRight.setInterpolator(new AccelerateInterpolator());
		return inFromRight;
	}

	public Animation outToLeftAnimation(float inverse) {
		Animation outtoLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, inverse * -1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT, 0.0f);
		outtoLeft.setDuration(ANIMATIION_DURATION);
		outtoLeft.setInterpolator(new AccelerateInterpolator());
		return outtoLeft;
	}

	private void changeToTabIncrement(int incrementTab) {
		int curTab = tabsHost.getCurrentTab();
		int nextTab = ((curTab + incrementTab) % 4);
		Animation currentAnimation;
		Animation nextAnimation;
		if (incrementTab > 0) {
			currentAnimation = outToLeftAnimation(1f);
			nextAnimation = inFromRightAnimation(1f);
		} else {
			currentAnimation =  outToLeftAnimation(-1f);
			nextAnimation =inFromRightAnimation(-1f);
		}
		// Define animation
		View currentView = tabsHost.getCurrentView();
		tabsHost.setCurrentTab(nextTab);
		currentView.setAnimation(currentAnimation);

		// Change Tab
		tabsHost.setCurrentTab(nextTab);
		View nextView = tabsHost.getCurrentView();
		nextView.setAnimation(nextAnimation);
	}

}
