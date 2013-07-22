package eu.ttbox.geoping.encoder;


import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.adapter.MapEncoderAdpater;
import eu.ttbox.geoping.encoder.model.MessageActionEnum;

public class SmsEncoderMultiMessageHelperTest {

    public static final String MSG_ENCRYPED_MULTI_LOC = "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)(d-mxNS,g-2i3wj;aeo4I;20,ak,pg,c21,b49)(d-mxNS,g2pFAg;9st3B;20,ak,pg,c21,b49)";
    public static final String MSG_ENCRYPED_MULTI_WRY_LOC3 = "geoPing?WRY!LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)(d-mxNS,g-2i3wj;aeo4I;20,ak,pg,c21,b49)(d-mxNS,g2pFAg;9st3B;20,ak,pg,c21,b49)";

    @Test
    public void testDecodeMultiLoc() {
        String encoded = MSG_ENCRYPED_MULTI_LOC;
        String phone = "+4412345678";
         List<MapEncoderAdpater> decodedMessages =  SmsEncoderHelper.decodeSmsMessage(new MapEncoderAdpater(), phone, encoded, null);
        Assert.assertNotNull(decodedMessages);
        Assert.assertEquals(3, decodedMessages.size());
        for (DecoderAdapter deco : decodedMessages) {
            Assert.assertEquals(MessageActionEnum.LOC, deco.getAction());
            Assert.assertEquals(phone, deco.getPhone());
        }

    }

    @Test
    public void testDecodeMultiWryAndLoc() {
        String encoded = MSG_ENCRYPED_MULTI_WRY_LOC3;
        String phone = "+4412345678";
         List<MapEncoderAdpater> decodedMessages =  SmsEncoderHelper.decodeSmsMessage(new MapEncoderAdpater(), phone, encoded, null);
        Assert.assertNotNull(decodedMessages);
        Assert.assertEquals(4, decodedMessages.size());
        // Test First
        Assert.assertEquals(MessageActionEnum.GEOPING_REQUEST, decodedMessages.get(0).getAction());
        Assert.assertEquals(phone, decodedMessages.get(0).getPhone());
        // Test Last
        for (int i = 1; i < decodedMessages.size(); i++) {
            DecoderAdapter deco = decodedMessages.get(i);
            Assert.assertEquals(MessageActionEnum.LOC, deco.getAction());
            Assert.assertEquals(phone, deco.getPhone());
        }
    }

    @Test
    public void testDecodeMultiTirLoc() {
        String[] messages = new String[]{"geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)(d-kTzc,g2KPp7;-50weC,a1W,pn)(d-kTkU,g3iZPk;9ROI,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49) (d-kTzc,g2KPp7;-50weC,a1W,pn)(d-kTkU,g3iZPk;9ROI,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)  (d-kTzc,g2KPp7;-50weC,a1W,pn)   (d-kTkU,g3iZPk;9ROI,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)(d-kTzc,g2KPp7;-50weC,a1W,pn)  (d-kTkU,g3iZPk;9ROI,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)   (d-kTzc,g2KPp7;-50weC,a1W,pn)LOC(d-kTkU,g3iZPk;9ROI,a1W,pn)" //
                , "geoPing?LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)LOC(d-kTzc,g2KPp7;-50weC,a1W,pn)LOC(d-kTkU,g3iZPk;9ROI,a1W,pn)" //
        };
        String phone = "+4412345678";
        for (String encoded : messages) {
             List<MapEncoderAdpater> decodedMessages =  SmsEncoderHelper.decodeSmsMessage(new MapEncoderAdpater(), phone, encoded, null);
            Assert.assertNotNull(decodedMessages);
            Assert.assertEquals(3, decodedMessages.size());
            for (DecoderAdapter deco : decodedMessages) {
                Assert.assertEquals(MessageActionEnum.LOC, deco.getAction());
                Assert.assertEquals(phone, deco.getPhone());
            }

        }
    }
}
