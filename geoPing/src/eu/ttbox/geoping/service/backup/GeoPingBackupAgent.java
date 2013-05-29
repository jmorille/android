package eu.ttbox.geoping.service.backup;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class GeoPingBackupAgent extends BackupAgentHelper {

    private static final String TAG = "GeoPingBackupAgent";

    // Object for intrinsic lock
    public static final Object[] sDataLock = new Object[0];

    // The name of the SharedPreferences file
    static final String PREFS = "eu.ttbox.geoping_preferences";

    // A key to uniquely identify the set of backup data
    public static final String BACKUP_KEY_PREFS = "GEOPING_01_PREFS";

    // Allocate a helper and add it to the backup agent
    @Override
    public void onCreate() {
        // Prefs
        SharedPreferencesBackupHelper helperPrefs = new TestSharedPreferencesBackupHelper(this, PREFS);
        addHelper(BACKUP_KEY_PREFS, helperPrefs);
        // Database Pairing
        PairingDbBackupHelper helperDbPairing = new PairingDbBackupHelper(this);
        addHelper(PairingDbBackupHelper.BACKUP_KEY_PAIRING_DB, helperDbPairing);
        // Database GeoFence
        GeoFenceDbBackupHelper helperDbGeofence = new GeoFenceDbBackupHelper(this);
        addHelper(GeoFenceDbBackupHelper.BACKUP_KEY_GEOFENCE_DB, helperDbGeofence);
        // Database Person
        PersonDbBackupHelper helperDbPerson = new PersonDbBackupHelper(this);
        addHelper(PersonDbBackupHelper.BACKUP_KEY_PERSON_DB, helperDbPerson);
    }

    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
        synchronized (GeoPingBackupAgent.sDataLock) {
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- onBackup GeoPing Backup --- Begin");
            super.onBackup(oldState, data, newState);
            Log.i(TAG, "----- onBackup GeoPing   Backup --- End");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
        }
    }

    @Override
    public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
        synchronized (GeoPingBackupAgent.sDataLock) {
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- onRestore GeoPing Backup : Version = " + appVersionCode);
            Log.i(TAG, "----- onRestore GeoPing Backup --- Begin");
            super.onRestore(data, appVersionCode, newState);
            Log.i(TAG, "----- onRestore GeoPing End --- End");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
        }
    }

}
