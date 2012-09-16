package eu.ttbox.geoping.test.service.encoder;

import java.util.HashMap;

import android.location.Location;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.service.encoder.SmsMessageEncoderHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageTypeEnum;

public class SmsMessageEncoderHelperTest extends AndroidTestCase {

    private static final String TAG = "SmsMessageEncoderHelperTest";

    public GeoTrack getMessageLoc() {
        String provider = "network";
        Location loc = new Location(provider);
        loc.setTime(System.currentTimeMillis());
        loc.setLatitude(43.15854941164189446d);
        loc.setLongitude(25.218546646446d);
        loc.setAccuracy(120.258446418974f);
        loc.setAltitude(124.6546533464d);
        loc.setBearing(257.16416464646446464646413f);
        loc.setSpeed(125.1464646464468946444646f);
        return new GeoTrack("montel", loc);
    }

    public void testEncodeDecodeMessage() {
        GeoTrack geoTrack = getMessageLoc();
        // Encode
        String encoded = SmsMessageEncoderHelper.encodeMessage(geoTrack);
        Log.d(TAG, String.format("Encoded Message (%s chars) : %s", encoded.length(), encoded));
        // Decode
        HashMap<String, Object> decoded = SmsMessageEncoderHelper.decodeMessageAsMap(encoded);
        assertEquals(geoTrack.provider, decoded.get(GeoTrackColumns.COL_PROVIDER));
        assertEquals(geoTrack.time, decoded.get(GeoTrackColumns.COL_TIME));
        assertEquals(geoTrack.getLatitudeE6(), decoded.get(GeoTrackColumns.COL_LATITUDE_E6));
        assertEquals(geoTrack.getLongitudeE6(), decoded.get(GeoTrackColumns.COL_LONGITUDE_E6));
        assertEquals(geoTrack.altitude, decoded.get(GeoTrackColumns.COL_ALTITUDE));
        assertEquals(geoTrack.accuracy, decoded.get(GeoTrackColumns.COL_ACCURACY)); 
        assertEquals(geoTrack.bearing, decoded.get(GeoTrackColumns.COL_BEARING));
        assertEquals(geoTrack.speed, decoded.get(GeoTrackColumns.COL_SPEED));  
    }

}
