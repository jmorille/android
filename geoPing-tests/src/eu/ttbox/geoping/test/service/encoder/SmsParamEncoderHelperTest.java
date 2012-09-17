package eu.ttbox.geoping.test.service.encoder;

import java.util.HashMap;

import android.location.Location;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.service.encoder.SmsParamEncoderHelper;

public class SmsParamEncoderHelperTest extends AndroidTestCase {

    private static final String TAG = "SmsMessageEncoderHelperTest";

    private float getRamdomFloat() {
        return (float) (Math.random() * 100);
    }

    public GeoTrack getMessageLoc(String provider) {
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

    public GeoTrack getMessageLocRamdom(String provider) {
        Location loc = new Location(provider);
        loc.setTime(System.currentTimeMillis());
        loc.setLatitude((float) (Math.random() * 100));
        loc.setLongitude((float) (Math.random() * 100));
        loc.setAccuracy((float) (Math.random() * 100));
        loc.setAltitude((float) (Math.random() * 100));
        loc.setBearing((float) (Math.random() * 100));
        loc.setSpeed((float) (Math.random() * 100));
        return new GeoTrack("montel", loc);
    }

    public void testEncodeDecodeMessage() {
        GeoTrack[] geoTracks = new GeoTrack[] { //
        getMessageLoc("network"), getMessageLoc("passive"), getMessageLoc("gps"), getMessageLoc("nimportenawak") //
                , getMessageLocRamdom("network"), getMessageLocRamdom("passive"), getMessageLocRamdom("gps"), getMessageLocRamdom("nimportenawak") //
                , getMessageLocRamdom("network"), getMessageLocRamdom("passive"), getMessageLocRamdom("gps"), getMessageLocRamdom("nimportenawak") //
                , getMessageLocRamdom("network"), getMessageLocRamdom("passive"), getMessageLocRamdom("gps"), getMessageLocRamdom("nimportenawak") //
        };

        for (GeoTrack geoTrack : geoTracks) {

            // Encode
            Bundle extras = GeoTrackHelper.getBundleValues(geoTrack);
            String encoded = SmsParamEncoderHelper.encodeMessage(extras, null).toString();
            Log.d(TAG, String.format("Encoded Message (%s chars) : %s", encoded.length(), encoded));
            // Decode
            Bundle decoded = SmsParamEncoderHelper.decodeMessageAsMap(encoded);
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

}
