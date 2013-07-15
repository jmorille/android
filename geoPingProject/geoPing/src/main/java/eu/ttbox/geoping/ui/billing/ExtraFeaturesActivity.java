package eu.ttbox.geoping.ui.billing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.service.gcm.GcmUnRegisterAsyncTask;
import eu.ttbox.geoping.ui.GeoPingSlidingMenuFragmentActivity;
import eu.ttbox.geoping.ui.gcm.RegisterActivity;

public class ExtraFeaturesActivity extends GeoPingSlidingMenuFragmentActivity   {
    
    // Debug tag, for logging
    private static final String TAG = "ExtraFeaturesActivity";




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



    }

    @Override
    public void onStop() {
        super.onStop();
        // Tracker
        EasyTracker.getInstance().activityStop(this);


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
