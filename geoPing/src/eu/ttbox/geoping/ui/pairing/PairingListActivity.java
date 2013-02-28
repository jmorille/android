package eu.ttbox.geoping.ui.pairing;

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

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;

public class PairingListActivity extends SherlockFragmentActivity {

    private static final String TAG = "PairingListActivity";

    // binding
    private PairingListFragment listFragment;

    // ===========================================================
    // Constructor
    // ===========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pairing_list_activity);
        // Intents
        handleIntent(getIntent());
        // Tracker
        GoogleAnalyticsTracker tracker = ((GeoPingApplication)getApplication()).tracker();
        tracker.trackPageView("/pairing/list");
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof PairingListFragment) {
            listFragment = (PairingListFragment) fragment;
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
        inflater.inflate(R.menu.menu_pairing_list, menu);
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
