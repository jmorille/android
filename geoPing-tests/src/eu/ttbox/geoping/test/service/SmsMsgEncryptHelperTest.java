package eu.ttbox.geoping.test.service;

import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.domain.GeoTrackSmsMsg;
import eu.ttbox.geoping.service.SmsMsgEncryptHelper;

public class SmsMsgEncryptHelperTest extends AndroidTestCase {

    public static final String TAG = "SmsMsgEncryptHelperTest";

    public void testEncodeMessage() {
        GeoTrackSmsMsg[] sms = new GeoTrackSmsMsg[] { //
        new GeoTrackSmsMsg("number", "noaction", null) //
                , new GeoTrackSmsMsg("number", "myaction", "The body of message") //
                , new GeoTrackSmsMsg("number", SmsMsgEncryptHelper.ACTION_GEO_PING, null) //
                , new GeoTrackSmsMsg("number", SmsMsgEncryptHelper.ACTION_GEO_LOC, "(al:124,ln:25218546,t:1347480505988,s:125,b:257,p:network,ac:120,la:43158549)") //
                , new GeoTrackSmsMsg("number", SmsMsgEncryptHelper.ACTION_GEO_LOC, "(al:16,ln:2334600,t:1347481830000,s:0,b:94,p:gps,ac:30,la:48874001)") //
                , new GeoTrackSmsMsg("number", SmsMsgEncryptHelper.ACTION_GEO_LOC, "{ac:39,la:48873768,ln:2333723,t:1347483426003,p:network}") //
         };

        for (GeoTrackSmsMsg msg : sms) {
            String encryped = SmsMsgEncryptHelper.encodeSmsMessage(msg);
            Log.d(TAG, String.format("Encoded Message (%s chars) : %s", encryped.length(), encryped));
            GeoTrackSmsMsg decoded = SmsMsgEncryptHelper.decodeSmsMessage(msg.smsNumber, encryped);
            assertEquals(msg.action, decoded.action);
            assertEquals(msg.body, decoded.body);
        }
    }

}
