package eu.ttbox.geoping.encoder;



import  org.junit.Assert;

import org.junit.Test;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.adapter.MapEncoderAdpater;
import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.encoder.params.AssertHelper;
import eu.ttbox.geoping.encoder.params.MessageParamField;


public class SmsEncoderHelperTest {

    private static long getTestDate() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, 2013);
        cal.set(Calendar.MONTH, Calendar.JULY);
        cal.set(Calendar.DAY_OF_MONTH, 14);
        // Midnight
        cal.set(Calendar.HOUR_OF_DAY, 9);
        cal.set(Calendar.MINUTE, 22);
        cal.set(Calendar.SECOND, 34);
        cal.set(Calendar.MILLISECOND, 0);
        long timeAtMidnight = cal.getTimeInMillis();
        return timeAtMidnight;
    }

    @Test
    public void testEncode() {
        long dateTest = getTestDate();
        // Create Map To encode
        MapEncoderAdpater mapEncode = new MapEncoderAdpater();
        mapEncode.putInt(MessageParamField.COL_ACCURACY, 120);
        mapEncode.putString(MessageParamField.COL_PROVIDER, "gps");
        mapEncode.putLong(MessageParamField.COL_TIME, dateTest);

        // Encode Message
        String encoded =  SmsEncoderHelper.encodeSmsMessage(MessageActionEnum.LOC_DECLARATION, mapEncode, null);
        System.out.println("Encoded Message : " + encoded);
        // Decode Message
       
        String phone = "+4412345678";
        List<MapEncoderAdpater> decodedMessages =  SmsEncoderHelper.decodeSmsMessage( new MapEncoderAdpater(), phone, encoded, null);
        Assert.assertNotNull(decodedMessages);
        Assert.assertEquals(1, decodedMessages.size() );
        MapEncoderAdpater mapDecode =  decodedMessages.get(0);
        AssertHelper.assertMap(mapEncode.getMap(), mapDecode.getMap(), false);

    }




}
