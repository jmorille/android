package eu.ttbox.geoping.service.backup;

import android.app.backup.FileBackupHelper;
import android.content.Context;
import android.util.Log;

public class DbBackupHelper  extends FileBackupHelper {

    private static final String TAG = "DbBackupHelper";

    public DbBackupHelper(Context ctx, String dbName) {
        // TODO A Better Way
        super(ctx, "../databases/"+dbName );
        Log.i(TAG, String.format(   "### DbBackupHelper data %s : %s" , ctx.getDatabasePath(dbName).exists(), ctx.getDatabasePath(dbName) ));
    }
    
     

}
