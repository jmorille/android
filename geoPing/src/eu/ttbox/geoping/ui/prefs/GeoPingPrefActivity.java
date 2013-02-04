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
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.NotifToasts;
import eu.ttbox.geoping.core.VersionUtils;

/**
 * http://www.blackmoonit.com/2012/07/all_api_prefsactivity/
 */
public class GeoPingPrefActivity extends PreferenceActivity  implements OnSharedPreferenceChangeListener {

    private static final String TAG = "GeoPingPrefActivity";




    // ===========================================================
    // Constructor
    // ===========================================================

    
    @Override
    public void onCreate(Bundle aSavedState) {
        // onBuildHeaders() will be called during super.onCreate()
        super.onCreate(aSavedState);
        if (!VersionUtils.isHc11) {
//        	 addPreferencesFromResource(R.xml.prefs);
            addPreferencesFromResource(R.xml.geoping_prefs);
            addPreferencesFromResource(R.xml.development_prefs);
            addPreferencesFromResource(R.xml.info_prefs);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        super.onBuildHeaders(target);
        if (VersionUtils.isHc11) {
            loadHeadersFromResource(R.xml.preference_headers, target);
        }
    }


    // ===========================================================
    // 
    // ===========================================================


    // ===========================================================
    // Generic Fragment
    // ===========================================================

   

    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static  class PrefsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle aSavedState) {
            super.onCreate(aSavedState);
            Context anAct = getActivity().getApplicationContext();
            String fragFileIdentifer = getArguments().getString("pref-resource");
            int thePrefRes = anAct.getResources().getIdentifier(fragFileIdentifer, "xml", anAct.getPackageName());
            Log.i(TAG, "Create PrefsFragment for file : " + fragFileIdentifer );
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
        Preference pref = findPreference(key);
//        setSummary(pref);
        // Tracker
        GeoPingApplication.getInstance().tracker().trackPageView("/Pref/"+key);

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
