package eu.ttbox.geoping.core;

public class AppConstants {

    public final static double E6 = 1000000d;

    // Constant
    public static final long UNSET_TIME = -1l;
    public static final long UNSET_ID = -1l;
    
    //
    public static final String LOCAL_DB_KEY = "local";
    public static final int SMS_MAX_SIZE = 160;

    public static final char PHONE_SEP = ';';  

    public static final int PER_PERSON_ID_MULTIPLICATOR = 10000;
    public static final String KEY_DB_LOCAL = "local";
    // Request Notification
    public static final String PREFS_SMS_DELETE_ON_MESSAGE = "smsDeleteOnMessage";
    public static final String PREFS_SMS_REQUEST_NOTIFY_ME = "smsRequestNotif";
    public static final String PREFS_LOCAL_SAVE = "localSave";
    
    // TODO Security
    
    // TODO in prefs
    public static final String PREFS_KEY_MYLOCATION_DISPLAY_GEOLOC = "MYLOCATION_DISPLAY_GEOLOC";

    public static final String PREFS_KEY_TILE_SOURCE = "KEY_TILE_SOURCE";

    public static final String PREFS_APP_COUNT_LAUGHT = "APP_COUNT_LAUGHT";

    // Prefs Slave Authorize set
    public static final String PREFS_PHONES_SET_AUTHORIZE_ALWAYS = "PAIRING_PHONES_AUTHORIZE_ALWAYS";
    public static final String PREFS_PHONES_SET_AUTHORIZE_NEVER = "PAIRING_PHONES_AUTHORIZE_NEVER";
    public static final String PREFS_GEOPING_REQUEST_NOTIFYME = "GEOPING_REQUEST_NOTIFYME";

    
}
