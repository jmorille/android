package eu.ttbox.geoping.ui.billing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.GeoPingSlidingMenuFragmentActivity;
import eu.ttbox.geoping.ui.gcm.RegisterActivity;

public class ExtraFeaturesActivity extends GeoPingSlidingMenuFragmentActivity   {
    
    // Debug tag, for logging
    private static final String TAG = "ExtraFeaturesActivity";


    public static final int REGISTER_ACTIVITY_REQ_CODE = 1;

    // binding
    private ExtraFeaturesFragment listFragment;

// ===========================================================
    // Constructor
    // ===========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.extra_features_activity);
        // SlidingMenu
//        final SlidingMenu slidingMenu = SlidingMenuHelper.newInstance(this);
//        slidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Intents
        handleIntent(getIntent());
        // Tracker
        EasyTracker.getInstance().activityStart(this);

        // temp test


        Intent registerActivity = new Intent(this, RegisterActivity.class);
        startActivityForResult(registerActivity, REGISTER_ACTIVITY_REQ_CODE);

    }

    @Override
    public void onStop() {
        super.onStop();
        // Tracker
        EasyTracker.getInstance().activityStop(this);

        // Unregister
// TODO         GCMIntentService.unregister(this);
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof ExtraFeaturesFragment) {
            listFragment = (ExtraFeaturesFragment) fragment;
        }
    }


    // ===========================================================
    // Result Code
    // ===========================================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, requestCode + " " + resultCode + ";", Toast.LENGTH_SHORT).show();
        if(requestCode == REGISTER_ACTIVITY_REQ_CODE) {
            if(resultCode != RESULT_OK) {
                // just end the activity if register fails
             //   finish();
            }
        }
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
        Log.d(TAG, "handleIntent for action : " + intent.getAction());
    }

    // ===========================================================
    // Other
    // ===========================================================

}
