package eu.ttbox.geoping;

import android.annotation.TargetApi;
import android.app.ActivityManager;
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
import eu.ttbox.geoping.ui.person.PhotoThumbmailCache;

public class GeoPingApplication extends Application {

	private String TAG = "GeoPingApplication";

	/* define your web property ID obtained after profile creation for the app */

	/* Analytics tracker instance */
	private GoogleAnalyticsTracker tracker;

	private static GeoPingApplication APP_INSTANCE;

	private static final boolean DEVELOPPER_MODE = true;

	// Cache

	private PhotoThumbmailCache photoCache;

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public void onCreate() {
		// Strict Mode
		 initStrictMode();
		// Create Application
		super.onCreate();
		APP_INSTANCE = this;

		// Perform the initialization that doesn't have to finish immediately.
		// We use an async task here just to avoid creating a new thread.
		(new DelayedInitializer()).execute();
	}

	public static GeoPingApplication getInstance() {
		return APP_INSTANCE;
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

	// ===========================================================
	// Statistic
	// ===========================================================

	private int incrementApplicationLaunchCounter(Context context) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
		// Read previous values
		int counter = settings.getInt(AppConstants.PREFS_APP_LAUGHT_COUNT, 0);
// TODO		long lastVersionLaugth = settings.getInt(AppConstants.PREFS_APP_LAUGHT_LASTVERSION, Integer.MIN_VALUE);
		long firstDateLaugth = settings.getLong(AppConstants.PREFS_APP_LAUGHT_FIRSTDATE, Long.MIN_VALUE);
		counter++;
		// Edit
		SharedPreferences.Editor prefEditor = settings.edit();
		prefEditor.putInt(AppConstants.PREFS_APP_LAUGHT_COUNT, counter);
		long now = System.currentTimeMillis();
		prefEditor.putLong(AppConstants.PREFS_APP_LAUGHT_LASTDATE, now);
		if (Long.MIN_VALUE == firstDateLaugth) {
			prefEditor.putLong(AppConstants.PREFS_APP_LAUGHT_FIRSTDATE, now);
		}
		prefEditor.commit();
		return counter;
	}

	// ===========================================================
	// Life Cycle
	// ===========================================================

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		if (photoCache != null) {
			photoCache.onLowMemory();
		}
	}

	@Override
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		if (photoCache != null) {
			photoCache.onTrimMemory(level);
		}
	}

	@Override
	public void onTerminate() {
		if (tracker != null) {
			tracker.dispatch();
			tracker.stopSession();
		}
		super.onTerminate();
	}

	// ===========================================================
	// Accessors
	// ===========================================================

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

	// ===========================================================
	// Google Analytics
	// ===========================================================

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

	// ===========================================================
	// Photo Cache
	// ===========================================================


	public PhotoThumbmailCache getPhotoThumbmailCache() {
		if (photoCache == null) {
			photoCache = initPhotoThumbmailCache();
		}
		return photoCache;
	}
	private synchronized PhotoThumbmailCache initPhotoThumbmailCache() {
		PhotoThumbmailCache photoCacheLocal = photoCache;
		if (photoCache == null) {
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			int memoryClassBytes = am.getMemoryClass() * 1024 * 1024;
			int maxSizeBytes = memoryClassBytes / 8; // 307000 * 10
			photoCacheLocal = new PhotoThumbmailCache(maxSizeBytes);
			Log.d(TAG, "Create Cache of PhotoThumbmailCache wih size " + maxSizeBytes); 
		}
		return photoCacheLocal;
	}


	// ===========================================================
	// Dev
	// ===========================================================

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void initStrictMode() {
		if (DEVELOPPER_MODE) {
			StrictMode.setThreadPolicy( //
					new StrictMode.ThreadPolicy.Builder()//
							.detectDiskReads()//
							.detectDiskWrites() //
							.detectNetwork() //
							.penaltyFlashScreen() //
							 .penaltyLog()//
							.build());
		}
	}

	// ===========================================================
	// Other
	// ===========================================================

}
