package eu.ttbox.velib.core;


import android.os.Build;

public class VersionUtils {

    public static boolean isHc11 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    public static boolean isIcs14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    public static boolean isJb16 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;

}
