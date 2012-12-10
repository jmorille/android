package eu.ttbox.geoping;

import android.annotation.TargetApi;
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

	/* Analytics tracker instance */
	private GoogleAnalyticsTracker tracker;

	private static GeoPingApplication APP_INSTANCE;

	private static final boolean DEVELOPPER_MODE = true;

	@Override
	public void onCreate() {
		// Strict Mode
//		initStrictMode();

		// Create Application
		super.onCreate();
		APP_INSTANCE = this;
 
		// Perform the initialization that doesn't have to finish immediately.
		// We use an async task here just to avoid creating a new thread.
		(new DelayedInitializer()).execute();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB) 
	private void initStrictMode() {
		if (DEVELOPPER_MODE) {
			StrictMode.setThreadPolicy( //
					new StrictMode.ThreadPolicy.Builder()//
							.detectDiskReads()//
							.detectDiskWrites() //
							.detectNetwork() //
							.penaltyFlashScreen() //
							// .penaltyLog()//
							.build());
		}
	}

	public static GeoPingApplication getInstance() {
		return APP_INSTANCE;
	}

	/**
	 * Google Analytics Tracker <br>
	 * {@link http://androidcookbook.com/Recipe.seam?recipeId=1503}
	 */
	private GoogleAnalyticsTracker createGoogleAnalyticsTracker() {
		synchronized (AppConstants.ANALYTICS_KEY) {
			if (tracker == null) {
				tracker = GoogleAnalyticsTracker.getInstance();
				tracker.setCustomVar(2, "Build/Platform", Build.VERSION.RELEASE);
				tracker.setCustomVar(3, "Build/Brand", Build.BRAND);
				tracker.setCustomVar(4, "Build/Device", Build.DEVICE);
				tracker.setCustomVar(1, "AppVersion", versionName());
				tracker.startNewSession(AppConstants.ANALYTICS_KEY, 60, getApplicationContext());
			}
		}
		return tracker;
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
	public GoogleAnalyticsTracker tracker() {
		if (tracker == null) {
			tracker = createGoogleAnalyticsTracker();
		}
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
