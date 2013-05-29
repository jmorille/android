package eu.ttbox.geoping.service.backup;

import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase;
import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase.GeoFenceColumns;
import eu.ttbox.geoping.domain.pairing.GeoFenceHelper;

public class GeoFenceDbBackupHelper  extends AbstractDbBackupHelper {

    @SuppressWarnings("unused")
    private static final String TAG = "GeoFenceDbBackupHelper";

    public static final String BACKUP_KEY_GEOFENCE_DB = "GEOPING_02_GEOFENCE_DB";

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
        return   pairingDatabase.insertEntity(values);
    }


    @Override
    public List<String> getValidColumns() {
        final List<String> validColumns = Arrays.asList(GeoFenceColumns.ALL_COLS); 
        return validColumns;
    }
   

}
