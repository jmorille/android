package eu.ttbox.geoping.service.backup;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.util.Log;

import java.util.Arrays;
import java.util.List;

import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase;
import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase.GeoFenceColumns;
import eu.ttbox.geoping.domain.pairing.GeoFenceHelper;

public class GeoFenceDbBackupHelper  extends AbstractDbBackupHelper {

    @SuppressWarnings("unused")
    private static final String TAG = "GeoFenceDbBackupHelper";

    public static final String BACKUP_KEY_GEOFENCE_DB = "GEOPING_03_GEOFENCE_DB";

    private GeoFenceDatabase pairingDatabase;
    private GeoFenceHelper helper;
    
    // ===========================================================
    // Constructor
    // ===========================================================

    
    public GeoFenceDbBackupHelper(Context ctx) {
        super(ctx, BACKUP_KEY_GEOFENCE_DB ); 
        pairingDatabase = new GeoFenceDatabase(ctx);
    }
  
    
    // ===========================================================
    // Backup
    // ===========================================================
 
    public   Cursor getBackupCursor() {
        String[] columns = GeoFenceColumns.ALL_COLS;
        Cursor cursor = pairingDatabase.queryEntities(columns, null, null, null);
        if (helper==null) {
            helper = new GeoFenceHelper();
        }
        helper.initWrapper(cursor);
        return cursor;
    }
    
    public  ContentValues getCursorAsContentValues(Cursor cursor ) { 
        CircleGeofence entity = helper.getEntity(cursor);
        ContentValues values =   GeoFenceHelper.getContentValues(entity);
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
            Log.e(TAG, "Ignore insert Constraint Exception : " + ce.getMessage() + " for Geofence = "+  values, ce);
        }
        return count;
    }


    @Override
    public List<String> getValidColumns() {
        final List<String> validColumns = Arrays.asList(GeoFenceColumns.ALL_COLS); 
        return validColumns;
    }
   

}
