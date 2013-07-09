package eu.ttbox.geoping.service.accountsync;

import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ContactsSyncAdapterService extends Service
{
    private static final String TAG = "ContactsSyncAdapterService";

    private   AbstractThreadedSyncAdapter geopingSyncAdpater;


    @Override
    public void onCreate() {
        Log.v(TAG, "GeoPing Authentication Service started.");
        geopingSyncAdpater = new GeopingSyncAdpater(this, this, true);
    }

    public IBinder onBind(Intent paramIntent)
    {
        return this.geopingSyncAdpater.getSyncAdapterBinder();
    }

}
