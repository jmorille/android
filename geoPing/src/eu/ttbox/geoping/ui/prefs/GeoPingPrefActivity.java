package eu.ttbox.geoping.ui.prefs;

import android.app.backup.BackupManager;
import android.app.backup.RestoreObserver;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceGroup;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.NotifToasts;

public class GeoPingPrefActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {

    private static final String TAG = "GeoPingPrefActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
        // Init Summary
        initSummaries(this.getPreferenceScreen());
        // Register change listener
        this.getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Set the summaries of all preferences
     */
    private void initSummaries(PreferenceGroup pg) {
        for (int i = 0; i < pg.getPreferenceCount(); ++i) {
            Preference p = pg.getPreference(i);
            if (p instanceof PreferenceGroup)
                this.initSummaries((PreferenceGroup) p); // recursion
            else
                this.setSummary(p);
        }
    }

    /**
     * Set the summaries of the given preference
     */
    private void setSummary(Preference pref) {
        // react on type or key
        if (pref instanceof EditTextPreference) {
            EditTextPreference editPref = (EditTextPreference) pref;
            String prefText = editPref.getText();
            if (prefText != null && prefText.length() > 0)
                pref.setSummary(prefText);
        } else if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference pref = findPreference(key);
        setSummary(pref);
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
