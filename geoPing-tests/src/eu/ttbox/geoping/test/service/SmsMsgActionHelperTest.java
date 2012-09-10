package eu.ttbox.geoping.test.service;

import eu.ttbox.geoping.service.SmsMsgActionHelper;
import android.location.Location;
import android.test.AndroidTestCase;
import android.util.Log;

public class SmsMsgActionHelperTest extends AndroidTestCase {

    private static final String TAG = "SmsMsgActionHelperTest";
    
    public Location getMessageLoc() {
        String provider = "network";
        Location loc = new Location(provider);
        loc.setTime(System.currentTimeMillis());
        loc.setLatitude(43.15854941164189446d);
        loc.setLongitude(25.218546646446d);
        loc.setAccuracy(120.258446418974f);
        loc.setAltitude(124.6546533464d);
        loc.setBearing(257.16416464646446464646413f);
        loc.setSpeed(125.1464646464468946444646f);
        return loc;
    }
    
    public void testConvertLocationAsJsonString() {
        Location loc = getMessageLoc();
        String locJson = SmsMsgActionHelper.convertLocationAsJsonString(loc);
        Log.d(TAG, "Json encode Size : " + locJson.length() + " / for msg : " + locJson);
        assertEquals(109, locJson.length());
    }

    public void testConvertLocationAsJacksonString() {
        Location loc = getMessageLoc();
        String locJson = SmsMsgActionHelper.convertLocationAsJacksonString(loc);
        Log.d(TAG, "Jackson encode Size : " + locJson.length() + " / for msg : " + locJson);
        assertEquals(109, locJson.length());
    }

}
