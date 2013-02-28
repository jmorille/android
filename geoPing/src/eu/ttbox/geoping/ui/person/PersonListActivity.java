package eu.ttbox.geoping.ui.person;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.slidingmenu.lib.SlidingMenu;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.slidingmenu.SlidingMenuHelper;

public class PersonListActivity extends SherlockFragmentActivity {

    private static final String TAG = "PersonListActivity";

    private PersonListFragment listFragment;

    // ===========================================================
    // Constructor
    // ===========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_person_list_activity);
        // SlidingMenu
        final SlidingMenu slidingMenu = SlidingMenuHelper.newInstance(this);
        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Intent
        handleIntent(getIntent());
        // Tracker
        GoogleAnalyticsTracker tracker = ((GeoPingApplication) getApplication()).tracker();
        tracker.trackPageView("/" + TAG);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof PersonListFragment) {
            listFragment = (PersonListFragment) fragment;
        }
    }

    // ===========================================================
    // Menu
    // ===========================================================

    public void onAddEntityClick(View v) {
        listFragment.onAddEntityClick(v);
    }

    private void onCancelClick() {
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.menu_person_list, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_add:
            onAddEntityClick(null);
            return true;
        case R.id.menu_cancel:
            onCancelClick();
            return true;
        }
        return false;
    }

    // ===========================================================
    // Intent
    // ===========================================================

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "handleIntent for action : " + intent.getAction());
        }
    }

    // ===========================================================
    // Other
    // ===========================================================

}
