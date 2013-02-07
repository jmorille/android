package eu.ttbox.geoping.service.backup;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import eu.ttbox.geoping.domain.pairing.PairingOpenHelper;
import eu.ttbox.geoping.domain.person.PersonOpenHelper;

public class GeoPingBackupAgent extends BackupAgentHelper {

	private static final String TAG = "GeoPingBackupAgent";

	// Object for intrinsic lock
	public static final Object[] sDataLock = new Object[0];

	// The name of the SharedPreferences file
	static final String FILENAME_PAIRING_DB = PairingOpenHelper.DATABASE_NAME;
	static final String FILENAME_PERSON_DB = PersonOpenHelper.DATABASE_NAME;
	static final String PREFS = "eu.ttbox.geoping_preferences";

	// A key to uniquely identify the set of backup data
	static final String BACKUP_KEY_PREFS = "GEOPING_PREFS"; 
	static final String BACKUP_KEY_PERSON_DB = "GEOPING_DB_PERSON";

	// Allocate a helper and add it to the backup agent
	@Override
	public void onCreate() {

		// Prefs
		SharedPreferencesBackupHelper helperPrefs = new SharedPreferencesBackupHelper(this, PREFS);
		addHelper(BACKUP_KEY_PREFS, helperPrefs);
 		// Database Pairing
		PairingBackupHelper helperDbPairing = new PairingBackupHelper(this);
		addHelper(PairingBackupHelper.BACKUP_KEY_PAIRING_DB, helperDbPairing); 
		// Database Person
		// DbBackupHelper helperDbPerson = new DbBackupHelper(this,  FILENAME_PERSON_DB);
		// addHelper(BACKUP_KEY_PERSON_DB, helperDbPerson);
	}

	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
		synchronized (GeoPingBackupAgent.sDataLock) {
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
			Log.i(TAG, "----- onBackup Begin Backup --- GeoPing"); 
			super.onBackup(oldState, data, newState);
			Log.i(TAG, "----- onBackup End   Backup --- GeoPing");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
		}
	}

	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		synchronized (GeoPingBackupAgent.sDataLock) {
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
		    Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
			Log.i(TAG, "----- onRestore Begin Backup --- GeoPing"); 
			super.onRestore(data, appVersionCode, newState);
			Log.i(TAG, "----- onRestore Begin End --- GeoPing");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
            Log.i(TAG, "----- ----- ----- ----- ----- ----- ----- ----- ----- ");
		}
	}

}
