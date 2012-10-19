package eu.ttbox.geoping.test.service.encoder;

import android.content.ContentValues;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.service.encoder.GeoPingMessage;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageEncoderHelper;

public class SmsMessageEncoderHelperTest extends AndroidTestCase {

    public static final String TAG = "SmsMsgEncryptHelperTest";

    public static final String PROVIDER_NETWORK = "network";
    public static final String PROVIDER_GPS = "gps";
    public static final String MSG_ENCRYPED = "geoPing?LOC!(th7lhawmo,z31,y1e14h,xt3jbc,aa,s0,pg,b1p)";
    

    private GeoPingMessage getGeoPingMessage01(String provider) {
        GeoTrack geoTrack = new GeoTrack() //
                .setProvider(provider)//
                .setLatitudeE6(43158549)//
                .setLongitude(25218546)//
                .setAccuracy(120) //
                .setTime(1347481830000l);
        if (PROVIDER_GPS.equals(provider)) {
            geoTrack.setAccuracy(25) //
                    .setAltitude(124)//
                    .setSpeed(23)//
                    .setBearing(257);
        }
        Bundle bundle = convertAsBundle(geoTrack);
        GeoPingMessage result = new GeoPingMessage("+33612131415", SmsMessageActionEnum.ACTION_GEO_LOC, bundle);
        return result;
    }

    private GeoPingMessage getGeoPingMessage02(String provider) {
        GeoTrack geoTrack = new GeoTrack();
        geoTrack.setProvider(provider);
        geoTrack.setLatitudeE6(48873768).setLongitude(2333723).setAccuracy(39);
        geoTrack.setTime(1347481830000l);
        if (PROVIDER_GPS.equals(provider)) {
            geoTrack.setAccuracy(50);
            geoTrack.setAltitude(0);
            geoTrack.setSpeed(0);
            geoTrack.setBearing(0);
        }
        Bundle bundle = convertAsBundle(geoTrack);
        GeoPingMessage result = new GeoPingMessage("+33612131415", SmsMessageActionEnum.ACTION_GEO_LOC, bundle);
        return result;
    }

    private Bundle convertAsBundle(GeoTrack geoTrack) {
        ContentValues values = GeoTrackHelper.getContentValues(geoTrack);
        Bundle bundle = new Bundle(values.size());
        for (String key : values.keySet()) {
            Object val = values.get(key);
            if (val instanceof String) {
                bundle.putString(key, (String) val);
            } else if (val instanceof Integer) {
                bundle.putInt(key, (Integer) val);
            } else if (val instanceof Long) {
                bundle.putLong(key, (Long) val);
            } else if (val instanceof Double) {
                bundle.putDouble(key, (Double) val);
            }
        }
        return bundle;
    }

    public void testEncodeMessage() {
        GeoPingMessage[] messages = new GeoPingMessage[] { new GeoPingMessage("+33612131415", SmsMessageActionEnum.GEOPING_REQUEST, null) //
                , getGeoPingMessage01(PROVIDER_NETWORK) //
                , getGeoPingMessage01(PROVIDER_GPS) //
                , getGeoPingMessage02(PROVIDER_NETWORK) //
                , getGeoPingMessage02(PROVIDER_GPS) //
        };
        for (GeoPingMessage msg : messages) {
            String encryped = SmsMessageEncoderHelper.encodeSmsMessage(msg.action, msg.params);
            Log.d(TAG, String.format("Sms Encoded Message (%s chars) : %s", encryped.length(), encryped));
            GeoPingMessage decoded = SmsMessageEncoderHelper.decodeSmsMessage(msg.phone, encryped);
            Log.d(TAG, String.format("Sms Decoded Message (action: %s)", msg.action));
            assertEquals(msg.action, decoded.action);
            if (msg.params != null) {
                assertEquals(msg.params.getString(GeoTrackColumns.COL_PROVIDER), decoded.params.getString(GeoTrackColumns.COL_PROVIDER));
                assertEquals(msg.params.getInt(GeoTrackColumns.COL_LATITUDE_E6), decoded.params.getInt(GeoTrackColumns.COL_LATITUDE_E6));
                assertEquals(msg.params.getInt(GeoTrackColumns.COL_LONGITUDE_E6), decoded.params.getInt(GeoTrackColumns.COL_LONGITUDE_E6));
                assertEquals(msg.params.getLong(GeoTrackColumns.COL_TIME), decoded.params.getLong(GeoTrackColumns.COL_TIME));
                //
                String provider = msg.params.getString(GeoTrackColumns.COL_PROVIDER);
                if (provider.equals(PROVIDER_NETWORK)) {
                    assertFalse(decoded.params.containsKey(GeoTrackColumns.COL_ALTITUDE));
                    assertFalse(decoded.params.containsKey(GeoTrackColumns.COL_SPEED));
                    assertFalse(decoded.params.containsKey(GeoTrackColumns.COL_BEARING));
                } else if (provider.equals(PROVIDER_GPS)) {
                    assertEquals(msg.params.getInt(GeoTrackColumns.COL_ALTITUDE), decoded.params.getInt(GeoTrackColumns.COL_ALTITUDE));
                    assertEquals(msg.params.getInt(GeoTrackColumns.COL_SPEED), decoded.params.getInt(GeoTrackColumns.COL_SPEED));
                    assertEquals(msg.params.getInt(GeoTrackColumns.COL_BEARING), decoded.params.getInt(GeoTrackColumns.COL_BEARING));
                }
            }
        }
    }

    public void testDecode() {
        
        GeoPingMessage decoded = SmsMessageEncoderHelper.decodeSmsMessage("+33612131415", MSG_ENCRYPED);
        GeoTrack geoTrack = GeoTrackHelper.getEntityFromBundle(decoded.params);
        // has
        assertTrue(geoTrack.hasTime());
        assertTrue(geoTrack.hasAccuracy());
        assertTrue(geoTrack.hasBearing());
        assertTrue(geoTrack.hasAltitude());
        assertTrue(geoTrack.hasLatitude());
        assertTrue(geoTrack.hasLongitude());
        //
        Log.d(TAG, "GeoTrack : " + geoTrack);
        Log.d(TAG, "Time : " + geoTrack.getTimeAsDate());

    }

}
