package eu.ttbox.velib.core;

public class AppConstants {

	public final static double E6 = 1000000d;

    public static final long ONE_SECOND_IN_MS = 1000;

    public static final long ONE_HOUR_IN_MS = 3600*ONE_SECOND_IN_MS;

    public static final long ONE_DAY_IN_MS = 24*ONE_HOUR_IN_MS;

    public final static double GOLD_NUMBER_Phi = 1.6180339887d;
	
	public static final int CONNECTION_TIMEOUT = 30000;
	
    public static final int UNSET_ID = -1;

    public static final String PREFS_ADD_BLOCKED = "addBlocked";

	public static final String PREFS_KEY_APP_COUNT_LAUGHT = "AppLaughtCounter";

	public static final String PREFS_KEY_USER_NUMBER_EXPECTED = "personCountExpected";

	public static final String PREFS_KEY_USER_RENT_DURATION_IN_MIN = "personRentDurationInMin";

	public static final String PREFS_KEY_PROVIDER_SELECT = "providerSelect";

	public static final String PREFS_KEY_CHEK_DISPO_DELTA_DELAY_IN_S = "checkDispoDeltaDelayInMs";

	public static final String PREFS_KEY_CHEK_DISPO_BUBLE_DELTA_DELAY_IN_S = "checkDispoBubbleDeltaDelayInMs";

	public static final String PREFS_KEY_BOUNDYBOX_FIX_DELAY_IN_MS = "boundyBoxFixDelayInMs";

	public static final String PREFS_KEY_MIN_MAP_ZOOMLEVEL_DETAILS = "minMapZoomLevelStationDetail";

//	public static final String PREFS__KEY_TILE_SOURCE = "tilesource";

	public static final String PREFS_KEY_MYLOCATION_DISPLAY_GEOLOC  = eu.ttbox.osm.core.AppConstants.PREFS_KEY_MYLOCATION_DISPLAY_GEOLOC;

    public static final String PREFS_KEY_PROVIDER_LAST_UPDATE_BASE = "PROVIDER_LAST_UPDATE_BASE_";

    public static final String PREFS_KEY_PROVIDER_DELTA_UPDATE_IN_DAY = "PROVIDER_DELTA_UPDATE_IN_DAY";
}
