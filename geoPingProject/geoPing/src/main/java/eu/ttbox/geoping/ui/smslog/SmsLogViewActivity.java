package eu.ttbox.geoping.ui.smslog;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;

public class SmsLogViewActivity extends SherlockFragmentActivity {

    private static final String TAG = "SmsLogViewActivity";


    private SmsLogViewFragment editFragment;

    // ===========================================================
    // Constructor
    // ===========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smslog_view_activity);
        // Add selector
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Tracker
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof SmsLogViewFragment) {
            editFragment = (SmsLogViewFragment) fragment;
        }
    }

        @Override
    public void onStop() {
        super.onStop();
        // Tracker
        EasyTracker.getInstance().activityStop(this);

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

        if (Intent.ACTION_VIEW.equals(action) ) {
             // Prepare Edit
            Uri entityUri = intent.getData();
            // Set Fragment
            Bundle fragArgs = new Bundle();
            fragArgs.putString(Intents.EXTRA_PERSON_ID, entityUri.toString());
            editFragment.setArguments(fragArgs);
         }

    }

}
