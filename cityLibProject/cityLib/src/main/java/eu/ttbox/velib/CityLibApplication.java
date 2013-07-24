package eu.ttbox.velib;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.ExceptionReporter;
import com.google.analytics.tracking.android.GAServiceManager;
import com.google.analytics.tracking.android.Tracker;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.ui.help.HelpMainActivity;

public class CityLibApplication extends Application {

	private String TAG = "CityLibApplication";
 

	public void onCreate() {
		// Stric Mode
		// enableStricMode()

		// Create Application
		super.onCreate();

		// Init Tacker
        // Get the tracker object.



        // Increment Counter Lauch
        // Perform the initialization that doesn't have to finish immediately.
        // We use an async task here just to avoid creating a new thread.
        (new DelayedInitializer(1000)).execute();

	}


    private class DelayedInitializer extends AsyncTask<Void, Void, Integer> {

        long delayInMs;

        public  DelayedInitializer(long delayInMs) {
            super();
            this.delayInMs = delayInMs;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            final Context context = CityLibApplication.this;
            try {
                Thread.sleep(delayInMs);
            } catch (InterruptedException e) {
                Log.d(TAG, "InterruptedException " + e.getMessage());
            }
            // Increment Counter Laught
            int launchCount = incrementApplicationLaunchCounter(context);
            Log.d(TAG, "================ CityLib Launch count = " + launchCount + "  ======================================");
            EasyTracker.getInstance().setContext(context);
            Tracker tracker = EasyTracker.getTracker();
            tracker.set("APP_COUNT_LAUGHT", String.valueOf( launchCount));
            return launchCount;
        }

        @Override
        protected void onPostExecute(Integer launchCount) {
            if (launchCount < 3) {
                startHelpActivity();
            }
        }
    }



	private void startHelpActivity() {
		Intent intent = new Intent(getBaseContext(), HelpMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getBaseContext().startActivity(intent);
	}

	private int incrementApplicationLaunchCounter(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		// Read previous values
		int counter = settings.getInt(AppConstants.PREFS_KEY_APP_COUNT_LAUGHT, 0);
		counter++;
		// Edit
		SharedPreferences.Editor prefEditor = settings.edit();
		prefEditor.putInt(AppConstants.PREFS_KEY_APP_COUNT_LAUGHT, counter);
		prefEditor.commit();
		return counter;
	}

 

	@Override
	public void onTerminate() { 
	    GAServiceManager.getInstance().dispatch(); 
		super.onTerminate();
	}

	 
	
	/**
	 * Get Application Version
	 * @return
	 */
	public String version() {
		return String.format("Version : %s/%s", getPackageName(), versionName());
	}  

	private String versionName() {
		try {
			final PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), 0);
			return info.versionName;
		} // try
		catch (PackageManager.NameNotFoundException nnfe) { 
			return "Unknown";
		} 
	}

	// public void enableStricMode() {
	// if (BuildConfig.DEBUG) {
	// Log.d(TAG, "----------------------------------------------");
	// Log.d(TAG, "-----------      STRIC MODE  -----------------");
	// Log.d(TAG, "----------------------------------------------");
	// Log.d(TAG, "----------------------------------------------");
	// StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectDiskReads()
	// // .detectDiskWrites()
	// .permitDiskWrites().detectNetwork() // or .detectAll() for
	// // all detectable
	// // problems
	// // .penaltyLog()
	// .build());
	// StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectLeakedSqlLiteObjects().detectLeakedClosableObjects().penaltyLog()
	// // .penaltyDeath()
	// .build());
	// }
	// }
}
