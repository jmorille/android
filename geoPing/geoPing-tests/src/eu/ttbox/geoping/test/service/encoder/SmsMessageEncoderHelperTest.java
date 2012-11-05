package eu.ttbox.geoping.test.service.encoder;

import java.util.ArrayList;

import android.content.ContentValues;
import android.location.Geocoder;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.service.encoder.GeoPingMessage;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageEncoderHelper;
import eu.ttbox.geoping.service.encoder.SmsParamEncoderHelper;
import eu.ttbox.geoping.service.encoder.params.IntegerEncoded;
import eu.ttbox.geoping.test.service.encoder.param.PlaceTestHelper;
import eu.ttbox.geoping.test.service.encoder.param.PlaceTestHelper.WorldGeoPoint;

public class SmsMessageEncoderHelperTest extends AndroidTestCase {

    public static final String TAG = "SmsMsgEncryptHelperTest";

    public static final String PROVIDER_NETWORK = "network";
    public static final String PROVIDER_GPS = "gps";
    // public static final String MSG_ENCRYPED =
    // "geoPing?LOC!(th7lhawmo,z31,y1e14h,xt3jbc,aa,s0,pg,b1p)";
    // public static final String MSG_ENCRYPED =
    // "geoPing?LOC!(dma97mu,z3g,yzik0zj,xpp1cl,ap,pg,cn,b75)";
    public static final String MSG_ENCRYPED_LOC = "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)";
    public static final String MSG_ENCRYPED_WRY = "geoPing?WRY";
    public static final String MSG_ENCRYPED_WRY2 = "geoPing?WRY!";

    public static final String MSG_ENCRYPED_MULTI_LOC = "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)(d-mxNS,g-2i3wj;aeo4I;20,ak,pg,c21,b49)(d-mxNS,g2pFAg;9st3B;20,ak,pg,c21,b49)";
    public static final String MSG_ENCRYPED_MULTI_LOC_WRY = "geoPing?WRY!LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)";
    public static final String MSG_ENCRYPED_MULTI_WRY_LOC3 = "geoPing?WRY!LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)(d-mxNS,g-2i3wj;aeo4I;20,ak,pg,c21,b49)(d-mxNS,g2pFAg;9st3B;20,ak,pg,c21,b49)";
    public static final String[] MSG_ENCRYPED = new String[] { MSG_ENCRYPED_LOC, MSG_ENCRYPED_WRY, MSG_ENCRYPED_WRY2 };

    private GeoTrack getMessageLoc(String provider, WorldGeoPoint place) {
        return PlaceTestHelper.getMessageLoc(provider, place);
    }

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

    public void testEncodeDecodeMessageGpsPlace() {
        for (WorldGeoPoint place : WorldGeoPoint.values()) {
            ArrayList<GeoTrack> geoTracks = new ArrayList<GeoTrack>();
            geoTracks.add(getMessageLoc("network", place));
            geoTracks.add(getMessageLoc("gps", place));

            for (GeoTrack geoTrack : geoTracks) {
                Bundle params = convertAsBundle(geoTrack);
                GeoPingMessage msg = new GeoPingMessage("+33612131415", SmsMessageActionEnum.ACTION_GEO_LOC, params);
                for (int radix : new int[] { 10, 36, IntegerEncoded.MAX_RADIX }) {
                    doEncodeDecodeTest(msg, String.format("Encoded Place %s %s radix=%s", place.name(), geoTrack.provider, radix), radix);
                }
            }
        }
    }

    public void testEncodeMessage() {
        GeoPingMessage[] messages = new GeoPingMessage[] { new GeoPingMessage("+33612131415", SmsMessageActionEnum.GEOPING_REQUEST, null) //
                , getGeoPingMessage01(PROVIDER_NETWORK) //
                , getGeoPingMessage01(PROVIDER_GPS) //
                , getGeoPingMessage02(PROVIDER_NETWORK) //
                , getGeoPingMessage02(PROVIDER_GPS) //
        };
        for (GeoPingMessage msg : messages) {
            doEncodeDecodeTest(msg, "Fix", SmsParamEncoderHelper.NUMBER_ENCODER_RADIX);
        }
    }

    private void doEncodeDecodeTest(GeoPingMessage msg, String logTitle, int radix) {
        String encryped = SmsMessageEncoderHelper.encodeSmsMessage(msg.action, msg.params);
        Log.d(TAG, String.format("Sms Encoded Message %s (%s chars) : %s", logTitle, encryped.length(), encryped));
        GeoPingMessage decoded = SmsMessageEncoderHelper.decodeSmsMessage(msg.phone, encryped);
        // Log.d(TAG,
        // String.format("Sms Decoded Message %s (action: %s)",logTitle,
        // msg.action));
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

    public void testDecodeMessageLoc() {
        GeoPingMessage decoded = SmsMessageEncoderHelper.decodeSmsMessage("+33612131415", MSG_ENCRYPED_LOC);
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

    public void testNotDecodeBadMessage() {
        String[] badMessages = new String[] { "Coucou couc", //
                "Tu connais l'appli GeoPing?", //
                "WRY", //
                "geoPing?", //
                "geoPing?W", //
                "geoPing?W(ssaa)" //
        };
        for (String msg : badMessages) {
            GeoPingMessage decoded = SmsMessageEncoderHelper.decodeSmsMessage("+33612131415", msg);
            Log.d(TAG, String.format("Not Extract %s from : %s", decoded, msg));
            assertNull(decoded);
        }
    }

    public void testDecode() {
        for (String msg : MSG_ENCRYPED) {
            GeoPingMessage decoded = SmsMessageEncoderHelper.decodeSmsMessage("+33612131415", msg);
            Log.d(TAG, String.format("Extract %s from : %s", decoded, msg));
            assertNotNull(decoded.action);

        }
    }

    public void testDecodeMultiMessageWryLoc() {
        String msg = "geoPing?WRY!LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)";
        // Message 01
        GeoPingMessage decoded = SmsMessageEncoderHelper.decodeSmsMessage("+33612131415", msg);
        Log.d(TAG, String.format("Extract %s from : %s", decoded, msg));
        assertNotNull(decoded.action);
        assertEquals(SmsMessageActionEnum.GEOPING_REQUEST, decoded.action);
        // Message 02
        assertNotNull(decoded.multiMessages);
        assertEquals(1, decoded.multiMessages.size());
        GeoPingMessage decoded2 = decoded.multiMessages.get(0);
        assertNotNull(decoded2);
        assertEquals(SmsMessageActionEnum.ACTION_GEO_LOC, decoded2.action);
        assertNotNull(decoded2.params);
    }

    public void testDecodeMultiMessageMultiBiLoc() {
        String[] messages = new String[] { "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)(d-kTzc,g2KPp7;-50weC,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49) (d-kTzc,g2KPp7;-50weC,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)  (d-kTzc,g2KPp7;-50weC,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)   (d-kTzc,g2KPp7;-50weC,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)   (d-kTzc,g2KPp7;-50weC,a1W,pn)" //
               , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)LOC(d-kTzc,g2KPp7;-50weC,a1W,pn)" //
                 };
        for (String msg : messages) {
            // Main Message 01
            GeoPingMessage decoded = SmsMessageEncoderHelper.decodeSmsMessage("+33612131415", msg);
            Log.d(TAG, String.format("Extract %s from : %s", decoded, msg));
            // Message 02
            SmsMessageActionEnum[] expMultiEctions = new SmsMessageActionEnum[] { SmsMessageActionEnum.ACTION_GEO_LOC  };
            doValidateMultiMessages(decoded, SmsMessageActionEnum.ACTION_GEO_LOC, expMultiEctions);
        }
    }
    public void testDecodeMultiMessageMultiTriiLoc() {
        String[] messages = new String[] { "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)(d-kTzc,g2KPp7;-50weC,a1W,pn)(d-kTkU,g3iZPk;9ROI,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49) (d-kTzc,g2KPp7;-50weC,a1W,pn)(d-kTkU,g3iZPk;9ROI,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)  (d-kTzc,g2KPp7;-50weC,a1W,pn)   (d-kTkU,g3iZPk;9ROI,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)(d-kTzc,g2KPp7;-50weC,a1W,pn)  (d-kTkU,g3iZPk;9ROI,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)   (d-kTzc,g2KPp7;-50weC,a1W,pn)LOC(d-kTkU,g3iZPk;9ROI,a1W,pn)" //
               , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)LOC(d-kTzc,g2KPp7;-50weC,a1W,pn)LOC(d-kTkU,g3iZPk;9ROI,a1W,pn)" //
                 };
        for (String msg : messages) {
            // Main Message 01
            GeoPingMessage decoded = SmsMessageEncoderHelper.decodeSmsMessage("+33612131415", msg);
            Log.d(TAG, String.format("Extract %s from : %s", decoded, msg));
            // Message 02
            SmsMessageActionEnum[] expMultiEctions = new SmsMessageActionEnum[] {    SmsMessageActionEnum.ACTION_GEO_LOC,  SmsMessageActionEnum.ACTION_GEO_LOC  };
            doValidateMultiMessages(decoded, SmsMessageActionEnum.ACTION_GEO_LOC, expMultiEctions);
        }
    }

    private void doValidateMultiMessages(GeoPingMessage decoded, SmsMessageActionEnum expMainAction, SmsMessageActionEnum[] expMultiActions) {
        // Main Messages
        assertNotNull(decoded.action);
        assertEquals(SmsMessageActionEnum.ACTION_GEO_LOC, decoded.action);
        // Check Expected Size
        assertNotNull(decoded.multiMessages);
        assertEquals(expMultiActions.length, decoded.multiMessages.size());
        for (int i = 0; i < expMultiActions.length; i++) {
            GeoPingMessage decoded2 = decoded.multiMessages.get(i);
            assertNotNull(decoded2);
            assertEquals(expMultiActions[i], decoded2.action);
            assertNotNull(decoded2.params);
        }
    }
    
//    public void testEncodeMultiMessageMultiLoc() {
//        GeoTrack[] geoTracks =  new GeoTrack[] { //
//                PlaceTestHelper.getMessageLoc(PlaceTestHelper.PROVIDER_GPS, WorldGeoPoint.NEW_YORK) //
//                ,   PlaceTestHelper.getMessageLoc(PlaceTestHelper.PROVIDER_GPS, WorldGeoPoint.PARIS) //
//        };
//        GeoPingMessage  message = null; 
//        for (GeoTrack geoTrack : geoTracks) {
//            Bundle bundle = convertAsBundle(geoTrack);
//            GeoPingMessage result = new GeoPingMessage("+33612131415", SmsMessageActionEnum.ACTION_GEO_LOC, bundle);
//            if (message==null) {
//                message= result;
//            } else {
//                message.addMultiMessage(result);
//            }
//        }
//        // Encode
////        SmsMessageEncoderHelper.en
//    }

}
