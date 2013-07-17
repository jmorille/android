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

import com.google.analytics.tracking.android.GAServiceManager;

import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.VersionUtils;
import eu.ttbox.geoping.domain.crypto.SecureDatabase;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase;
import eu.ttbox.geoping.ui.person.PhotoThumbmailCache;

public class GeoPingApplication extends Application {

    private String TAG = "GeoPingApplication";

    /* define your web property ID obtained after profile creation for the app */

    /* Analytics tracker instance */
//    private GoogleAnalytics tracker;

    private static GeoPingApplication APP_INSTANCE;

    private static final boolean DEVELOPPER_MODE = BuildConfig.DEBUG;

    // Cache

    private PhotoThumbmailCache photoCache;

    // DataBase
    private SmsLogDatabase smsLogDatabase;
    private GeoTrackDatabase geoTrackDatabase;
    private SecureDatabase secureDatabase;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public void onCreate() {
        // Strict Mode
//        initStrictMode();
        // Create Application
        super.onCreate();
        APP_INSTANCE = this;


        // Perform the initialization that doesn't have to finish immediately.
        // We use an async task here just to avoid creating a new thread.
        (new DelayedInitializer(2000)).execute();
    }

    public static GeoPingApplication getInstance() {
        return APP_INSTANCE;
    }

    private class DelayedInitializer extends AsyncTask<Void, Void, Integer> {

        long delayInMs;

        public  DelayedInitializer(long delayInMs) {
            super();
            this.delayInMs = delayInMs;
        }

        @Override
        protected Integer doInBackground(Void... params) {
            final Context context = GeoPingApplication.this;
            try {
                Thread.sleep(delayInMs);
            } catch (InterruptedException e) {
                Log.d(TAG, "InterruptedException " + e.getMessage());
            }
            // Increment Counter Laught
            int launchCount = incrementApplicationLaunchCounter(context);
            Log.d(TAG, "================ Geoping Launch count = " + launchCount + "  ======================================");
            return launchCount;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);

        }
    }


    // ===========================================================
    // Statistic
    // ===========================================================

    private int incrementApplicationLaunchCounter(Context context) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        // Read previous values
        int counter = settings.getInt(AppConstants.PREFS_APP_LAUGHT_COUNT, 0);
        // TODO long lastVersionLaugth =
        // settings.getInt(AppConstants.PREFS_APP_LAUGHT_LASTVERSION,
        // Integer.MIN_VALUE);
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
        GAServiceManager.getInstance().dispatch(); 
        super.onTerminate();
    }

    // ===========================================================
    // Accessors
    // ===========================================================

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
    // Database instance
    // ===========================================================

    public SmsLogDatabase getSmsLogDatabase() {
        if (smsLogDatabase==null) {
            smsLogDatabase = new SmsLogDatabase(this);
        }
        return smsLogDatabase;
    }

    public GeoTrackDatabase getGeoTrackDatabase() {
        if (geoTrackDatabase==null) {
            geoTrackDatabase = new GeoTrackDatabase(this);
        }
        return geoTrackDatabase;
    }

    public SecureDatabase getSecureDatabase() {
        if (secureDatabase==null) {
            String password = "ddzsmj,rdzm,rmzkrz";
            secureDatabase = new SecureDatabase(this, password);
        }
        return secureDatabase;
    }




    // ===========================================================
    // Dev
    // ===========================================================

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initStrictMode() {
        if (VersionUtils.isHc11 && DEVELOPPER_MODE) {
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

    public static GeoPingApplication getGeoPingApplication(Context context) {
        return (GeoPingApplication)context.getApplicationContext();
    }

}
