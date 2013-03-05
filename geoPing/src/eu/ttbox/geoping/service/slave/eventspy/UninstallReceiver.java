package eu.ttbox.geoping.service.slave.eventspy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/*
 * http://alvinalexander.com/java/jwarehouse/android/core/java/com/android/internal/content/PackageMonitor.java.shtml
 * 
 */
public class UninstallReceiver extends BroadcastReceiver {

    private static final String TAG = "UninstallReceiver";

    // http://developer.android.com/reference/android/content/Intent.html#ACTION_BOOT_COMPLETED
    private static final String ACTION_PACKAGE_REMOVED =  Intent.ACTION_PACKAGE_REMOVED;  
    private static final String ACTION_PACKAGE_FULLY_REMOVED =  Intent.ACTION_PACKAGE_FULLY_REMOVED;  

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
//        if (ACTION_PACKAGE_REMOVED.equals(action) || ACTION_PACKAGE_FULLY_REMOVED.equals(action) ) {
            String pkg =getPackageName(intent);
            boolean applicationStatus = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
            Log.d(TAG, "### ############################### ### ");
            Log.d(TAG, "### ### EventSpy PACKAGE_REMOVED : " + action + " ### ### ");
            Log.d(TAG, "### ### EventSpy PACKAGE_REMOVED  Package : " + pkg + " ### ### ");

            
            Log.d(TAG, "### ############################### ### ");
//        }
        
    }
    
    private String getPackageName(Intent intent) {
        Uri uri = intent.getData();
        String pkg = uri != null ? uri.getSchemeSpecificPart() : null;
        return pkg;
    }
    
    
    
}
