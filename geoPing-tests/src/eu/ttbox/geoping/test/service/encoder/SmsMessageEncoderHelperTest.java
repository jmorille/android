package eu.ttbox.geoping.test.service.encoder;

import android.content.ContentValues;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.service.encoder.GeoPingMessage;
import eu.ttbox.geoping.service.encoder.SmsMessageEncoderHelper;
import eu.ttbox.geoping.service.encoder.SmsParamEncoderHelper;

public class SmsMessageEncoderHelperTest  extends AndroidTestCase {

    public static final String TAG = "SmsMsgEncryptHelperTest";

    public static final String PROVIDER_NETWORK = "network";
    public static final String PROVIDER_GPS = "gps";
            
    private GeoPingMessage getGeoPingMessage01(String provider) {
        GeoTrack geoTrack = new GeoTrack();
        geoTrack.setProvider(provider);
        geoTrack.setLatitudeE6(43158549).setLongitude(25218546).setAccuracy(120);
        geoTrack.setTime(1347481830000l);
        if (PROVIDER_GPS.equals(provider)) {
            geoTrack.setAccuracy(25);
            geoTrack.setAltitude(124);
            geoTrack.setSpeed(23);
            geoTrack.setBearing(257);
         }
        Bundle bundle = convertAsBundle(geoTrack); 
        GeoPingMessage result = new GeoPingMessage("+33612131415", SmsMessageEncoderHelper.ACTION_GEO_LOC, bundle);
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
        GeoPingMessage result = new GeoPingMessage("+33612131415", SmsMessageEncoderHelper.ACTION_GEO_LOC, bundle);
        return result;
    }


    private Bundle convertAsBundle(GeoTrack geoTrack) {
        ContentValues  values = GeoTrackHelper.getContentValues(geoTrack);
        Bundle bundle = new Bundle(values.size());
        for (String key : values.keySet()) {
           Object val =  values.get(key);
           if (val instanceof String) {
               bundle.putString(key, (String) val);
            } else  if (val instanceof Integer) {
                bundle.putInt(key, (Integer) val);
            } else  if (val instanceof Long) {
                bundle.putLong(key, (Long) val);
            } else  if (val instanceof Double) {
                bundle.putDouble(key, (Double) val);
            }
        }
        return bundle;
    }
    
    public void testEncodeMessage() { 
        GeoPingMessage[]  messages = new GeoPingMessage[] {
                new GeoPingMessage("+33612131415", SmsMessageEncoderHelper.ACTION_GEO_PING, null) //
                , getGeoPingMessage01(PROVIDER_NETWORK) //
                , getGeoPingMessage01(PROVIDER_GPS) //
                , getGeoPingMessage02(PROVIDER_NETWORK) //
                , getGeoPingMessage02(PROVIDER_GPS) //
        };
        for (GeoPingMessage msg : messages) {
            String encryped = SmsMessageEncoderHelper.encodeSmsMessage(msg);
            Log.d(TAG, String.format("Sms Encoded Message (%s chars) : %s", encryped.length(), encryped));
            GeoPingMessage decoded = SmsMessageEncoderHelper.decodeSmsMessage(msg.phone, encryped);
            Log.d(TAG, String.format("Sms Decoded Message (action: %s)",msg.action));
            assertEquals(msg.action, decoded.action);
//            assertEquals(msg.body, decoded.body);
        }
    }

    
}
