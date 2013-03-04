package eu.ttbox.geoping.ui.prefs;

import java.util.List;

import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.NotifToasts;
import eu.ttbox.geoping.core.VersionUtils;
import eu.ttbox.geoping.ui.prefs.comp.version.AppVersionPreference;

/**
 * http://www.blackmoonit.com/2012/07/all_api_prefsactivity/
 */
public class GeoPingPrefActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private static final String TAG = "GeoPingPrefActivity";

    private SharedPreferences sharedPreferences;

    // Dev Listener
    private SharedPreferences developmentPreferences;
    SharedPreferences.OnSharedPreferenceChangeListener mDevelopmentPreferencesListener;

    // ===========================================================
    // Constructor
    // ===========================================================

    @Override
    public void onCreate(Bundle aSavedState) {
        // onBuildHeaders() will be called during super.onCreate()
        developmentPreferences = getSharedPreferences(AppVersionPreference.PREFS_DEV_MODE, Context.MODE_PRIVATE);
        super.onCreate(aSavedState);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if (!VersionUtils.isHc11) {
            final boolean showDev = developmentPreferences.getBoolean(AppVersionPreference.PREF_SHOW_DEVMODE, false);
            // addPreferencesFromResource(R.xml.prefs);
            addPreferencesFromResource(R.xml.geoping_prefs);
            addPreferencesFromResource(R.xml.map_prefs);
            if (showDev) {
                addPreferencesFromResource(R.xml.development_prefs);
            }
            addPreferencesFromResource(R.xml.info_prefs);
        }
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
    public void onResume() {
        super.onResume();
        // Register change listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        // Resume for Headeers
        if (VersionUtils.isHc11) {
            onResumeHc11();
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onResumeHc11() {
        mDevelopmentPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            @TargetApi(Build.VERSION_CODES.HONEYCOMB)
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                invalidateHeaders();
            }
        };
        developmentPreferences.registerOnSharedPreferenceChangeListener(mDevelopmentPreferencesListener);
        invalidateHeaders();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Register change listener
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        // Resume for Headeers
        if (developmentPreferences != null && mDevelopmentPreferencesListener != null) {
            developmentPreferences.unregisterOnSharedPreferenceChangeListener(mDevelopmentPreferencesListener);
            mDevelopmentPreferencesListener = null;
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        if (VersionUtils.isHc11) {
            loadHeadersFromResource(R.xml.preference_headers, target);
            updateHeaderList(target);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void updateHeaderList(List<Header> target) {
        final boolean showDev = developmentPreferences.getBoolean(AppVersionPreference.PREF_SHOW_DEVMODE, false);
        int i = 0;
        while (i < target.size()) {
            Header header = target.get(i);
            // Ids are integers, so downcasting
            int id = (int) header.id;
            if (id == R.id.development_settings) {
                if (!showDev) {
                    target.remove(i);
                }
            }
            // Increment if the current one wasn't removed by the Utils code.
            // if (target.get(i) == header) {
            i++;
            // }
        }
    }

    // ===========================================================
    // Listener
    // ===========================================================

    // ===========================================================
    // Generic Fragment
    // ===========================================================

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle aSavedState) {
            super.onCreate(aSavedState);
            Context anAct = getActivity().getApplicationContext();
            String fragFileIdentifer = getArguments().getString("pref-resource");
            int thePrefRes = anAct.getResources().getIdentifier(fragFileIdentifer, "xml", anAct.getPackageName());
            Log.i(TAG, "Create PrefsFragment for file : " + fragFileIdentifer);
            addPreferencesFromResource(thePrefRes);
        }
    }

    // ===========================================================
    // Change Pref Listener
    // ===========================================================

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Ask Backup
        BackupManager.dataChanged(getPackageName());
        // Update Display
        // Preference pref = findPreference(key);
        // setSummary(pref);
        // Tracker
        // GeoPingApplication.getInstance().tracker().trackPageView("/Pref/" +
        // key);
        Tracker tracker = EasyTracker.getTracker();
        tracker.sendEvent("ui_pref", "changed", key, null);
    }

    // ===========================================================
    // Backup Restore
    // ===========================================================

    public void onBackupButtonClick(View v) {
        BackupManager.dataChanged(getPackageName());
    }

    /**
     * Click handler, designated in the layout, that runs a restore of the app's
     * most recent data when the button is pressed.
     */
    public void onRestoreButtonClick(View v) {
        Log.v(TAG, "Requesting restore of our most recent data");
        BackupManager mBackupManager = new BackupManager(this);
        mBackupManager.requestRestore(new RestoreObserver() {
            public void restoreFinished(int error) {
                /** Done with the restore! Now draw the new state of our data */
                if (error == 0) {
                    Log.v(TAG, "Restore finished, error = " + error);
                    NotifToasts.showBackupRestored(GeoPingPrefActivity.this);
                } else {
                    Log.e(TAG, "Restore finished with error = " + error);
                }
            }
        });
    }

    // ===========================================================
    // Other
    // ===========================================================

}
