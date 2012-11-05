package eu.ttbox.geoping.test.service.sender;

import android.location.Location;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.domain.GeoTrackSmsMsg;
import eu.ttbox.geoping.service.SmsMsgActionHelper;
import eu.ttbox.geoping.service.SmsMsgEncryptHelper;

public class SmsLocationHelperTest extends AndroidTestCase {

	public static final String TAG = "SmsLocationHelperTest";

	public void testToSmsMessage() {
		String provider = "network";
		Location loc = new Location(provider);
		loc.setTime(System.currentTimeMillis());
		loc.setLatitude(43.15854941164189446d);
		loc.setLongitude(25.218546646446d);
		loc.setAccuracy(120.258446418974f);
		loc.setAltitude(124.6546533464d);
		loc.setBearing(257.16416464646446464646413f);
		loc.setSpeed(125.1464646464468946444646f);
		// Convertion 2 String
		GeoTrackSmsMsg geoTrackMsg =  SmsMsgActionHelper.geoLocMessage(loc);
		String msg = SmsMsgEncryptHelper.encodeSmsMessage(geoTrackMsg);
		assertTrue("SMS should be <= 255 chars", msg.length() < 255);
		Log.i(TAG, "SMS Message Size : " + msg.length());
		Log.i(TAG, msg);
		// 2 Lat
		GeoTrackSmsMsg locun = SmsMsgEncryptHelper.decodeSmsMessage("0612345678", msg);
		assertNotNull(locun);
	}
}
