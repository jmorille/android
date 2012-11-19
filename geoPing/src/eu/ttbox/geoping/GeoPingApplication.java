package eu.ttbox.geoping;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import eu.ttbox.geoping.core.AppConstants;

public class GeoPingApplication extends Application {

	private String TAG = "AndroGisterApp";

	/* define your web property ID obtained after profile creation for the app */
	private String analyticsKey = "UA-36410991-1";

	/* Analytics tracker instance */
	GoogleAnalyticsTracker tracker;

	@Override
	public void onCreate() {
		// Create Application
		super.onCreate();
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog().build());
		}
		 createGoogleAnalyticsTracker() ;
		// Perform the initialization that doesn't have to finish immediately.
		// We use an async task here just to avoid creating a new thread.
		(new DelayedInitializer()).execute();

	}

	/**
	 * Google Analytics Tracker <br>
	 * {@link http://androidcookbook.com/Recipe.seam?recipeId=1503}
	 */
	private void createGoogleAnalyticsTracker() {
		tracker = GoogleAnalyticsTracker.getInstance();
		tracker.setCustomVar(2, "Build/Platform", Build.VERSION.RELEASE);
		tracker.setCustomVar(3, "Build/Brand", Build.BRAND);
		tracker.setCustomVar(4, "Build/Device", Build.DEVICE);
		tracker.setCustomVar(1, "AppVersion", versionName());
		tracker.startNewSession(analyticsKey, 60, getApplicationContext());
	}

	@Override
	public void onTerminate() {
		if (tracker != null) {
			tracker.dispatch();
			tracker.stopSession();
		}
		super.onTerminate();
	}

	/*
	 * This is getter for tracker instance. This is called in activity to get
	 * reference to tracker instance.
	 */
	public GoogleAnalyticsTracker getTracker() {
		return tracker;
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
