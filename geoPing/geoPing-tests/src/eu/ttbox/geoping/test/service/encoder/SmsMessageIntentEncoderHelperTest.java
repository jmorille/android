package eu.ttbox.geoping.test.service.encoder;

import java.util.Calendar;
import java.util.Date;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageIntentEncoderHelper;

public class SmsMessageIntentEncoderHelperTest extends AndroidTestCase {

    private static final String TAG = "SmsMessageIntentEncoderHelperTest";

    private static final String PHONE = "+33612131415";

    public GeoTrack getMessageLoc(String provider) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_YEAR, 252);
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 19);
        cal.set(Calendar.SECOND, 20);
        cal.set(Calendar.MILLISECOND, 21);
        Log.d(TAG, String.format("Create Message from %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS,%1$tL", cal.getTimeInMillis()));

        Location loc = new Location(provider);
        loc.setTime(cal.getTimeInMillis());
        loc.setLatitude(43.15854941164189446d);
        loc.setLongitude(25.218546646446d);
        loc.setAccuracy(120.258446418974f);
        loc.setAltitude(124.6546533464d);
        loc.setBearing(257.16416464646446464646413f);
        loc.setSpeed(125.1464646464468946444646f);
        return new GeoTrack(null, loc);
    }

    @SuppressWarnings("deprecation")
    public void testEncodeDecodedGeopingResponse() {
        // Create Message from 2012-09-08 18:19:20,021
        GeoTrack geotrack = getMessageLoc("gps");
        Bundle params = GeoTrackHelper.getBundleValues(geotrack);
        String encryped = SmsMessageIntentEncoderHelper.encodeSmsMessage(SmsMessageActionEnum.ACTION_GEO_LOC, params);
        Log.d(TAG, String.format("Sms Encoded Message (%s chars) : %s", encryped.length(), encryped));
        Intent intent =   SmsMessageIntentEncoderHelper.decodeAsIntent(getContext(), PHONE, encryped);
        Log.d(TAG, String.format("Sms Decoded Message (action: %s)", intent.getAction()));
        GeoTrack geoTrack = GeoTrackHelper.getEntityFromIntent(intent);
        Log.d(TAG, "GeoTrack : " + geoTrack);
        // has
        assertTrue(geoTrack.hasTime());
        assertTrue(geoTrack.hasAccuracy());
        assertTrue(geoTrack.hasBearing());
        assertTrue(geoTrack.hasAltitude());
        assertTrue(geoTrack.hasLatitude());
        assertTrue(geoTrack.hasLongitude());
        //
       
        Log.d(TAG, "Decoded GeoTrack Time : " + geoTrack.getTimeAsDate());
        Date time = geotrack.getTimeAsDate();
        assertEquals(18, time.getHours());
        assertEquals(19, time.getMinutes());
        assertEquals(20, time.getSeconds());
    }

    public void testDecodeGeopingResponse() {
        String encryped = "geoPing?LOC(d-BNOL,h31,g2V5vD;1HOuK;20,s0,pg,b1p,aa)";
        Intent decoded = SmsMessageIntentEncoderHelper.decodeAsIntent(getContext(), PHONE, encryped);
        GeoTrack geoTrack = GeoTrackHelper.getEntityFromIntent(decoded); 

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
