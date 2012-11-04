package eu.ttbox.geoping;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;

public class GeoPingApplication extends Application {

    private String TAG = "AndroGisterApp";

    @Override
    public void onCreate() {
        // Create Application
        super.onCreate();
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
        }
        // Perform the initialization that doesn't have to finish immediately.
        // We use an async task here just to avoid creating a new thread.
        (new DelayedInitializer()).execute();

    }

    /**
     * Get Application Version
     * 
     * @return
     */
    public String version() {
        return String.format("Version : %s/%s", getPackageName(), versionName());
    }

    public String versionPackageName() {
        return String.format("%s/%s", getPackageName(), versionName());
    }

    public String versionName() {
        try {
            final PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
            return info.versionName;
        } // try
        catch (PackageManager.NameNotFoundException nnfe) {
            return "Unknown";
        }
    }

    private class DelayedInitializer extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            final Context context = GeoPingApplication.this;
            // Increment Counter Laught
            int laugthCount = incrementApplicationLaunchCounter(context);
            Log.i(TAG, "Laugth count " + laugthCount);
            return null;
        }
    }

    private int incrementApplicationLaunchCounter(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        // Read previous values
        int counter = settings.getInt(AppConstants.PREFS_APP_COUNT_LAUGHT, 0);
        long firstDateLaugth = settings.getLong(AppConstants.PREFS_APP_FIRSTDATE_LAUGHT, Long.MIN_VALUE);
        counter++;
        // Edit
        SharedPreferences.Editor prefEditor = settings.edit();
        prefEditor.putInt(AppConstants.PREFS_APP_COUNT_LAUGHT, counter);
        if (Long.MIN_VALUE == firstDateLaugth) {
            long now = System.currentTimeMillis();
            prefEditor.putLong(AppConstants.PREFS_APP_FIRSTDATE_LAUGHT, now);
        }
        prefEditor.commit();
        return counter;
    }

}
