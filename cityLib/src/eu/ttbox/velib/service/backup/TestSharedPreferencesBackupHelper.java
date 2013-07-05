package eu.ttbox.velib.service.backup;

import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.content.Context;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class TestSharedPreferencesBackupHelper extends SharedPreferencesBackupHelper {
    private static final String TAG = "TestSharedPreferencesBackupHelper";

    public TestSharedPreferencesBackupHelper(Context context, String... prefGroups) {
        super(context, prefGroups);

    }

    /**
     * Backs up the configured {@link android.content.SharedPreferences} groups.
     */
    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
        String backupKey = CityLibBackupAgent.BACKUP_KEY_PREFS;
        Log.i(TAG, "-------------------------------------------------");
        Log.i(TAG, "--- performBackup : key =  " + backupKey);

        super.performBackup(oldState, data, newState);

        Log.i(TAG, "----- performBackup End : key =  " + backupKey);
        Log.i(TAG, "-------------------------------------------------");

    }

    /**
     * Restores one entity from the restore data stream to its proper shared
     * preferences file store.
     */
    public void restoreEntity(BackupDataInputStream data) {
        String key = data.getKey();
        Log.i(TAG, "-------------------------------------------------");
        Log.i(TAG, "--- restore Entity '" + key + "' size=" + data.size());
        super.restoreEntity(data);
        Log.i(TAG, "----- restoreEntity End");
        Log.i(TAG, "-------------------------------------------------");
    }

}
