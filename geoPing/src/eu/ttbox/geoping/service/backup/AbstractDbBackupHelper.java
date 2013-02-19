package eu.ttbox.geoping.service.backup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import eu.ttbox.geoping.domain.pairing.PairingDatabase;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.app.backup.BackupDataOutput;
import android.app.backup.BackupHelper;


public abstract class AbstractDbBackupHelper  implements BackupHelper {
    
    private static final String TAG = "AbstractDbBackupHelper";
    
    
    private final Context context;
    
    private final String backupKey;
    
    // ===========================================================
    // Interface
    // ===========================================================

    
    public interface BackupInsertor {

        long insertEntity(ContentValues values);
    }
    
    // ===========================================================
    // Constructor
    // ===========================================================

    
    public AbstractDbBackupHelper(Context ctx, String backupKey) {
        super();
        this.context = ctx;
        this.backupKey =backupKey;
    }
    

    // ===========================================================
    // States
    // ===========================================================

    
    @Override
    public void writeNewStateDescription(ParcelFileDescriptor newState) {
     Log.d(TAG, "--- --------------------------------- ---");
     Log.d(TAG, "--- writeNewStateDescription          ---");
     Log.d(TAG, "--- --------------------------------- ---");
    }
    

    // ===========================================================
    // Backup
    // ===========================================================

    public void performBackup(String backupKey, ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) {
        Log.i(TAG, "-------------------------------------------------");
        Log.i(TAG, "--- performBackup : key =  " + backupKey);
        // Doing copy
        ByteArrayOutputStream dataCopy = copyTable(stringColums, intColums, longColums);
        if (dataCopy != null) {
            try {
                byte[] dataBytes = dataCopy.toByteArray();
                data.writeEntityHeader(backupKey, dataBytes.length);
                data.writeEntityData(dataBytes, dataBytes.length);
                Log.i(TAG, "performBackup Entity '" + backupKey + "' size=" + dataBytes.length);
            } catch (IOException e) {
                Log.e(TAG, "Error during Backup Data : " + e.getMessage(), e);
            }
        }
        Log.i(TAG, "----- performBackup End : key =  " + backupKey);
        Log.i(TAG, "-------------------------------------------------");
    }
    
    
    
    
    public ByteArrayOutputStream copyTable(String[] stringColums, String[] intColums, String[] longColums) {
        ByteArrayOutputStream bufStream = null;
        Cursor cursor = null;
        try {
//          int columnSize = stringColums.length + intColums.length + longColums.length;
//          String[] columns = UpgradeDbHelper.concatAllCols(columnSize, stringColums, intColums, longColums);
             String[] columns = PairingColumns.ALL_COLS;
            cursor = pairingDatabase.queryEntities(columns, null, null, null);
//          bufStream = copyTable(cursor, stringColums, intColums, longColums);
             bufStream = copyTable(cursor, columns, null, null);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bufStream;
    }
    

    // ===========================================================
    // Restore
    // ===========================================================

    
}
