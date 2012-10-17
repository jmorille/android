package eu.ttbox.geoping.service.slave;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * {link  http://blog.gregfiumara.com/archives/82}
 *
 */
public class BackgroudLocService extends Service {
    
    private static final String TAG = "BackgroudLocService";

    private final IBinder binder = new LocalBinder();

    @Override
    public void onCreate() {
        Log.v(TAG, "BackgroudLocService Created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "BackgroudLocService -- onStartCommand()");

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /*
     * In Android 2.0 and later, onStart() is depreciated. Use onStartCommand()
     * instead, or compile against API Level 5 and use both.
     * http://android-developers
     * .blogspot.com/2010/02/service-api-changes-starting-with.html
     * 
     * @Override public void onStart(Intent intent, int startId) { Log.v(TAG,
     * "BackgroudLocService -- onStart()"); }
     */

    @Override
    public void onDestroy() {
        Log.v(TAG, "BackgroudLocService Destroyed");
    }

    // ===========================================================
    // Binder
    // ===========================================================

    public class LocalBinder extends Binder {
        public BackgroudLocService getService() {
            return BackgroudLocService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }
}
