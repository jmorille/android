package eu.ttbox.geoping.ui.smslog;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.google.analytics.tracking.android.EasyTracker;
import android.support.v4.app.Fragment;

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
