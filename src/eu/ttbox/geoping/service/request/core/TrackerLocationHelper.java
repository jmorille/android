package eu.ttbox.geoping.service.request.core;

import android.location.Location;
import android.util.Log;

/**
 * 
 * @see http://developer.android.com/guide/topics/location/obtaining-user-location.html
 * @author deostem
 *
 */
public class TrackerLocationHelper {
	
    private static final String TAG = "TrackerLocationHelper";
    
    private static final int TWO_MINUTES = 1000 * 60 * 2;
	
	
	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	public static boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }
	    if (location==null) {
	        // no location could not be better to anything
	        return false;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        Log.d(TAG, "YES Location is Significantly Newer (more than two minutes since the current location)");
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	           Log.d(TAG, "NO Location is Significantly Older (the new location is more than two minutes older, it must be worse)");
 	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
            Log.d(TAG, "YES is More Accurate");
	        return true;
	    } else if (isNewer && !isLessAccurate) {
            Log.d(TAG, "YES is Newer and Not Less Accurate");
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            Log.d(TAG, "YES is Newer and NotSignificantlyLess Accurate from the Same Provider");
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private static  boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
}
