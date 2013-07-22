package eu.ttbox.geoping.ui.prefs.comp.version;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;

public class AppVersionPreference extends Preference {

    public static final String PREFS_DEV_MODE = "prefDevMode";
    public static final String PREF_SHOW_DEVMODE = "devModeShow";

    private static final String TAG = "AppVersionPreference";
    private static final int TAPS_TO_BE_A_DEVELOPER = 7;

    private OnClickListener devModeOnClickListener = new OnClickListener() {

        int devHitCountDown = TAPS_TO_BE_A_DEVELOPER;

        Toast devHitToast = null;

        private SharedPreferences getDevModePrefs() {
            SharedPreferences prefs = getContext().getSharedPreferences(PREFS_DEV_MODE, Context.MODE_PRIVATE);
            return prefs;
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "DevMode Click count : " + devHitCountDown);
            if (devHitCountDown > 0) {
                devHitCountDown--;

                if (devHitCountDown == 0) {
                    Log.d(TAG, "DevMode Need to activated");

                    // Set Dev Mode
                    SharedPreferences prefs = getDevModePrefs();
                    prefs.edit().putBoolean(PREF_SHOW_DEVMODE, true).apply(); 
                    // Notify
                    if (devHitToast != null) {
                        devHitToast.cancel();
                    }
                    devHitToast = Toast.makeText(getContext(), R.string.show_dev_on, Toast.LENGTH_LONG);
                    devHitToast.show();
                } else if (devHitCountDown > 0 && devHitCountDown == (TAPS_TO_BE_A_DEVELOPER - 2)) {
                    Log.d(TAG, "DevMode Load current status.");

                    SharedPreferences prefs = getDevModePrefs();
                    if (prefs.getBoolean(PREF_SHOW_DEVMODE, false)) {
                        devHitCountDown = -1;
                        Log.d(TAG, "DevMode Load current status : Already Activated => put devHitCountDown =" + devHitCountDown);
                    }
                } else if (devHitCountDown > 0 && devHitCountDown < (TAPS_TO_BE_A_DEVELOPER - 2)) {
                    Log.d(TAG, "DevMode Normal Tap =" + devHitCountDown);

                    if (devHitToast != null) {
                        devHitToast.cancel();
                    }
                    Resources r = getContext().getResources();
                    devHitToast = Toast.makeText(getContext(), r.getString(R.string.show_dev_countdown, devHitCountDown), Toast.LENGTH_SHORT);
                    devHitToast.show();
                }
            } else if (devHitCountDown < 0) {
                Log.d(TAG, "DevMode Already Activated Tap =" + devHitCountDown);
                if (devHitToast != null) {
                    devHitToast.cancel();
                }
                devHitToast = Toast.makeText(getContext(), R.string.show_dev_already, Toast.LENGTH_LONG);
                devHitToast.show();
            }
        }
    };

    // This is the constructor called by the inflater
    public AppVersionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AppVersionPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onBindView(View view) {
        GeoPingApplication app = (GeoPingApplication) getContext().getApplicationContext();
        String verion = app.versionPackageName();
        setSummary(verion);
        view.setOnClickListener(devModeOnClickListener);
        // Super need to be last
        super.onBindView(view);
    }

}
