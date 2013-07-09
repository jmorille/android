package eu.ttbox.geoping.core;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class VersionUtils {
 
    public static boolean isHc11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    public static boolean isIcs14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    public static boolean isJb16 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;

    public static class AndroidPermissions {
        public static final String READ_CONTACTS = "android.permission.READ_CONTACTS";

        public static final String RECEIVE_BOOT_COMPLETED = "android.permission.RECEIVE_BOOT_COMPLETED";
        public static final String PROCESS_OUTGOING_CALLS = "android.permission.PROCESS_OUTGOING_CALLS";
        public static final String READ_PHONE_STATE = "android.permission.READ_PHONE_STATE";

        public static boolean isPermissionGranted(Context context, String permissionToCheck) {
            return PackageManager.PERMISSION_GRANTED == context.getPackageManager().checkPermission(permissionToCheck, context.getPackageName());
        }

    }
}
