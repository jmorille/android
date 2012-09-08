package eu.ttbox.geoping.test.service.sender;

import android.location.Location;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.service.sender.SmsLocationHelper;

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
		String msg = SmsLocationHelper.toSmsMessage(loc);
		assertTrue("SMS should be <= 255 chars", msg.length() < 255);
		Log.i(TAG, "SMS Message Size : " + msg.length());
		Log.i(TAG, msg);
		// 2 Lat
		Location locun = SmsLocationHelper.fromSmsMessage(msg);
		assertNotNull(locun);
	}
}
