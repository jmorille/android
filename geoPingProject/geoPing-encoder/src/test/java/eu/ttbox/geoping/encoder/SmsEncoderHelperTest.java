package eu.ttbox.geoping.encoder;


import org.junit.Test;

import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.adapter.MapEncoderAdpater;
import eu.ttbox.geoping.encoder.model.MessageActionEnum;

public class SmsEncoderHelperTest {

    @Test
    public void testEncode() {
        EncoderAdapter params = new MapEncoderAdpater();
        String encoded =  SmsEncoderHelper.encodeSmsMessage(MessageActionEnum.LOC_DECLARATION, params, null);
        System.out.println("Encoded Message : " + encoded);
    }


}
