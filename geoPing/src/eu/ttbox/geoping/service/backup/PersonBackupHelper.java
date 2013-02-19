package eu.ttbox.geoping.service.backup;

import android.app.backup.BackupDataInputStream;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;
import android.os.ParcelFileDescriptor;

public class PersonBackupHelper  implements BackupHelper {

    private static final String TAG = "PersonBackupHelper";

    public static final String BACKUP_KEY_PAIRING_DB = "BACKUP_KEY_PERSON_DB";

    @Override
    public void performBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void restoreEntity(BackupDataInputStream data) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void writeNewStateDescription(ParcelFileDescriptor newState) { 
    }


}
