package eu.ttbox.velib.service.database;

import android.app.SearchManager;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * #see http://www.oschina.net/code/explore/android-4.0.1/core/java/android/provider/CalendarContract.java
 *  
 */
public class Velo {
	private static String TAG = "VeloContentProvider";
	/***
	 * The content:// style URL for the top-level calendar authority
	 */
	public static String AUTHORITY = "eu.ttbox.velib";

	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

	public interface VeloColumns {

	    
	    
		public static final String COL_ID = BaseColumns._ID;// "_ID";
		public static final int NUM_COL_ID = 0;

		public static final String COL_PROVIDER = "PVD";
//		public static final int NUM_COL_PROVIDER = 1;

		public static final String COL_NUMBER = "PVD_ID";
//		public static final int NUM_COL_NUMBER = 2;

		public static final String COL_LATITUDE_E6 = "LAT_E6";
//		public static final int NUM_COL_LATITUDE_E6 = 3;

		public static final String COL_LONGITUDE_E6 = "LNG_E6";
//		public static final int NUM_COL_LONGITUDE_E6 = 4;

		public static final String COL_STATION_TOTAL = "VELO_TOTAL";
//		public static final int NUM_COL_STATION_TOTAL = 5;

		public static final String COL_STATION_CYCLE = "VELO_AVAILABLE";
//		public static final int NUM_COL_STATION_CYCLE = 6;

		public static final String COL_STATION_PARKING = "VELO_FREE";
//		public static final int NUM_COL_STATION_PARKING = 7;

		public static final String COL_STATION_TICKET = "VELO_TICKET";
//		public static final int NUM_COL_STATION_TICKET = 8;

		public static final String COL_STATION_UPDATE_TIME = "VELO_UPDATED";
//		public static final int NUM_COL_STATION_UPDATE_TIME = 9;

		public static final String COL_NAME = SearchManager.SUGGEST_COLUMN_TEXT_1; // "NAME";
//		public static final int NUM_COL_NAME = 10;

		public static final String COL_ADDRESS = "ADDRESS";
//		public static final int NUM_COL_ADDRESS = 11;

		public static final String COL_OPEN = "OPEN";
//		public static final int NUM_COL_OPEN = 12;

		public static final String COL_BONUS = "BONUS";
//		public static final int NUM_COL_BONUS = 13;

		public static final String COL_FAVORY = "FAVORY";
//		public static final int NUM_COL_FAVORY = 14;

		public static final String COL_FAVORY_TYPE = "FAVORY_TYPE";
//		public static final int NUM_COL_FAVORY_TYPE = 15;

		public static final String COL_ALIAS_NAME = "ALIAS";
//		public static final int NUM_COL_ALIAS_NAME = 16;

		public static final String COL_FULLADDRESS = SearchManager.SUGGEST_COLUMN_TEXT_2; // "FULLADDR";
//		public static final int NUM_COL_FULLADDRESS = 17;

		// Alias
//		public static final String ALIAS_COL_DISPO_CYCLE_PARKING = "AGG_COL_DISPO";
//		public static final String ALIAS_COL_LAT_LNG_E6 = "AGG_COL_LAT_LNG_E6";
        
	}

}
