package eu.ttbox.geoping.service.accountsync;

import android.content.Intent;
import android.util.Log;

import com.actionbarsherlock.app.SherlockActivity;

public class ProfileActivity   extends SherlockActivity{
    private static final String TAG = "ProfileActivity" ;


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
        Log.d(TAG, "===========================================================");
        Log.d(TAG, "handleIntent for action : " + intent.getAction()  + " / extra " + intent);
        Log.d(TAG, "===========================================================");

    }

}
