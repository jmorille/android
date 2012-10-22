package eu.ttbox.geoping.test.service.encoder;

import java.util.ArrayList;

import android.os.Bundle;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.service.encoder.SmsParamEncoderHelper;
import eu.ttbox.geoping.service.encoder.params.IntegerEncoded;
import eu.ttbox.geoping.test.service.encoder.param.PlaceTestHelper;
import eu.ttbox.geoping.test.service.encoder.param.PlaceTestHelper.WorldGeoPoint;

public class SmsParamEncoderHelperTest extends AndroidTestCase {

    private static final String TAG = "SmsParamEncoderHelperTest";

  
    public static final String PROVIDER_NETWORK = "network";
    public static final String PROVIDER_GPS = "gps";
   
    private GeoTrack getMessageLoc(String provider, WorldGeoPoint place) {
         return PlaceTestHelper.getMessageLoc(provider, place);
    }
 
    public GeoTrack getMessageLocRamdom(String provider) {
         return PlaceTestHelper.getMessageLocRamdom(  provider) ;
    }

    public void testEncodeDecodeAnyRamdomMessage() {
        GeoTrack[] geoTracks = new GeoTrack[] { //
        getMessageLocRamdom("network"), getMessageLocRamdom("passive"), getMessageLocRamdom("gps"), getMessageLocRamdom("nimportenawak") //
                , getMessageLocRamdom("network"), getMessageLocRamdom("passive"), getMessageLocRamdom("gps"), getMessageLocRamdom("nimportenawak") //
                , getMessageLocRamdom("network"), getMessageLocRamdom("passive"), getMessageLocRamdom("gps"), getMessageLocRamdom("nimportenawak") //
        };

        for (GeoTrack geoTrack : geoTracks) {
            doEncodeDecodeTest(geoTrack, "Encoded Ramdom Message", SmsParamEncoderHelper.NUMBER_ENCODER_RADIX);
        }
    }

    public void testEncodeDecodeMessage() {

        for (WorldGeoPoint place : WorldGeoPoint.values()) {
            ArrayList<GeoTrack> geoTracks = new ArrayList<GeoTrack>();
            geoTracks.add(getMessageLoc("network", place));
            geoTracks.add(getMessageLoc("passive", place));
            geoTracks.add(getMessageLoc("gps", place));
            geoTracks.add(getMessageLoc("nimportenawak", place));

            for (GeoTrack geoTrack : geoTracks) {
                for (int radix : new int[] { 10, 36, IntegerEncoded.MAX_RADIX }) {
                    doEncodeDecodeTest(geoTrack, String.format("Encoded Message %s radix=%s", place.name(), radix), radix);
                }
            }
        }
    }


    public void testEncodeDecodeMessageGpsPlace() { 
        for (WorldGeoPoint place : WorldGeoPoint.values()) {
            ArrayList<GeoTrack> geoTracks = new ArrayList<GeoTrack>();
            geoTracks.add(getMessageLoc("network", place));
            geoTracks.add(getMessageLoc("gps", place));

            for (GeoTrack geoTrack : geoTracks) {
                for (int radix : new int[] { 10, 36, IntegerEncoded.MAX_RADIX }) {
                    doEncodeDecodeTest(geoTrack, String.format("Encoded Place %s %s radix=%s", place.name(), geoTrack.provider, radix), radix);
                }
            }
        }
    }

    private void doEncodeDecodeTest(GeoTrack geoTrack, String logTitle, int radix) {
        // Encode
        Bundle extras = GeoTrackHelper.getBundleValues(geoTrack);
        String encoded = SmsParamEncoderHelper.encodeMessage(extras, null, radix).toString();
        Log.d(TAG, String.format("%s (%s chars) : %s", logTitle, encoded.length(), encoded));
        // Decode
        Bundle decoded = SmsParamEncoderHelper.decodeMessageAsMap(encoded, null, radix);
        assertEquals(geoTrack.provider, decoded.get(GeoTrackColumns.COL_PROVIDER));
        assertEquals(geoTrack.time, decoded.get(GeoTrackColumns.COL_TIME));
        assertEquals(geoTrack.getLatitudeE6(), decoded.get(GeoTrackColumns.COL_LATITUDE_E6));
        assertEquals(geoTrack.getLongitudeE6(), decoded.get(GeoTrackColumns.COL_LONGITUDE_E6));
        assertEquals(geoTrack.accuracy, decoded.get(GeoTrackColumns.COL_ACCURACY));
        if (PROVIDER_GPS.equals(geoTrack.provider)) {
            assertEquals(geoTrack.getAltitude(), decoded.get(GeoTrackColumns.COL_ALTITUDE));
            assertEquals(geoTrack.bearing, decoded.get(GeoTrackColumns.COL_BEARING));
            assertEquals(geoTrack.speed, decoded.get(GeoTrackColumns.COL_SPEED));
        } else {
            assertNull(decoded.get(GeoTrackColumns.COL_ALTITUDE));
            assertNull(decoded.get(GeoTrackColumns.COL_BEARING));
            assertNull(decoded.get(GeoTrackColumns.COL_SPEED));
        }
    }

}
