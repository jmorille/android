package eu.ttbox.geoping.service.response;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.GeoTrackSmsMsg;
import eu.ttbox.geoping.service.core.WorkerService;

public class GeoPingResponseHandlerService extends WorkerService {

    private static final String TAG = "GeoPingResponseHandlerService";

    private final IBinder binder = new LocalBinder();
    
    // Service
    private SharedPreferences appPreferences;
    
    // config
    boolean notifyGeoPingResponse = false;
    
    
    public class LocalBinder extends Binder {
        public GeoPingResponseHandlerService getService() {
            return GeoPingResponseHandlerService.this;
        }
    }
    
    public GeoPingResponseHandlerService(String name) {
        super(TAG); 
    }
 
    @Override
    public void onCreate() {
        super.onCreate();
        // service
        this.appPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.notifyGeoPingResponse = appPreferences.getBoolean(AppConstants.PREFS_SMS_REQUEST_NOTIFY_ME, false); 

        Log.d(TAG, "#################################");
        Log.d(TAG, "### GeoPingResponseHandlerService Service Started.");
        Log.d(TAG, "#################################");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
       if (intent!=null) { 
           // TODO
       }
        
    }

    
    private void displayPingRequestNotification(GeoTrackSmsMsg clearMsg ) {
//      NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
  }

}
