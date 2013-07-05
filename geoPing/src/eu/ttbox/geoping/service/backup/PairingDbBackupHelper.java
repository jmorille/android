package eu.ttbox.geoping.service.backup;

import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import eu.ttbox.geoping.domain.model.Pairing;
import eu.ttbox.geoping.domain.pairing.PairingDatabase;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.domain.pairing.PairingHelper;

public class PairingDbBackupHelper  extends AbstractDbBackupHelper {

    @SuppressWarnings("unused")
    private static final String TAG = "PairingDbBackupHelper";

    public static final String BACKUP_KEY_PAIRING_DB = "GEOPING_02_PAIRING_DB";

    private PairingDatabase pairingDatabase;
    private PairingHelper helper;
    
    // ===========================================================
    // Constructor
    // ===========================================================

    
    public PairingDbBackupHelper(Context ctx) {
        super(ctx, BACKUP_KEY_PAIRING_DB ); 
        pairingDatabase = new PairingDatabase(ctx);
    }
  
    
    // ===========================================================
    // Backup
    // ===========================================================
 
    public   Cursor getBackupCursor() {
        String[] columns = PairingColumns.ALL_COLS;
        Cursor cursor = pairingDatabase.queryEntities(columns, null, null, null);
        if (helper==null) {
            helper = new PairingHelper();
        }
        helper.initWrapper(cursor);
        return cursor;
    }
    
    public  ContentValues getCursorAsContentValues(Cursor cursor ) { 
        Pairing entity = helper.getEntity(cursor);
        ContentValues values =   PairingHelper.getContentValues(entity);
        return values;
    }



    // ===========================================================
    // Restore
    // ===========================================================

    @Override
    public long insertEntity(ContentValues values) {
        long count = 0;
        try {
            count = pairingDatabase.insertEntity(values);
        } catch (   SQLiteConstraintException ce) {
            Log.e(TAG, "Ignore insert Constraint Exception : " + ce.getMessage() + " for Pairing = " + values, ce);
        }
        return count;
    }


    @Override
    public List<String> getValidColumns() {
        final List<String> validColumns = Arrays.asList(PairingColumns.ALL_COLS); 
        return validColumns;
    }
   

}
