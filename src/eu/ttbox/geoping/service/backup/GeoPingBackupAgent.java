package eu.ttbox.geoping.service.backup;

import java.io.IOException;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import eu.ttbox.geoping.domain.pairing.PairingOpenHelper;

public class GeoPingBackupAgent extends BackupAgentHelper {

	private static final String TAG = "GeoPingBackupAgent";

	
	// The name of the SharedPreferences file
    static final String FILENAME_PAIRING_DB = PairingOpenHelper.DATABASE_NAME;
    static final String PREFS = "user_preferences";
    
    // A key to uniquely identify the set of backup data
    static final String BACKUP_KEY_PREFS = "geoPingPrefs";
    static final String BACKUP_KEY_PAIRING_DB = "geoPingDbPairing";

    // Allocate a helper and add it to the backup agent
    @Override
   public void onCreate() {
    	// Prefs
    	  SharedPreferencesBackupHelper helperPrefs = new SharedPreferencesBackupHelper(this, PREFS);
    	  addHelper(BACKUP_KEY_PREFS, helperPrefs);
    	  
    	// Database
        FileBackupHelper helperDbPairing = new FileBackupHelper(this, FILENAME_PAIRING_DB);
        addHelper(BACKUP_KEY_PAIRING_DB, helperDbPairing);
    }
 
	@Override
	public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
		Log.i(TAG, "----- onBackup Begin Backup data station");
		super.onBackup(oldState, data, newState);
		Log.i(TAG, "----- onBackup End   Backup data station");
	}
	
	@Override
	public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
		Log.i(TAG, "----- onRestore Begin Backup data station"); 
		super.onRestore(data, appVersionCode, newState);
		Log.i(TAG, "----- onRestore Begin End   data station"); 
	}
	
	
}
