package eu.ttbox.geoping.test.service;

import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.domain.GeoTrackSmsMsg;
import eu.ttbox.geoping.service.SmsMsgEncryptHelper;

public class SmsMsgEncryptHelperTest extends AndroidTestCase {

	public static final String TAG ="SmsMsgEncryptHelperTest";
	
	public void testEncodeMessage() {
		GeoTrackSmsMsg msg = new GeoTrackSmsMsg("number", "myaction", "The body of message");
		String encryped = SmsMsgEncryptHelper.encodeSmsMessage(msg);
		Log.d(TAG, encryped);
//		System.out.println(encryped);
	}
}
