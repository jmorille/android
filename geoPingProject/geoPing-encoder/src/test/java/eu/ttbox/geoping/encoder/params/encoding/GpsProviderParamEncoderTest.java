package eu.ttbox.geoping.encoder.params.encoding;


import org.junit.Assert;
import org.junit.Test;

import eu.ttbox.geoping.encoder.adapter.MapEncoderAdpater;
import eu.ttbox.geoping.encoder.params.MessageParamField;

public class GpsProviderParamEncoderTest {

    private GpsProviderParamEncoder service = new GpsProviderParamEncoder();

    private final MessageParamField field = MessageParamField.COL_PROVIDER;


    @Test
    public void testAlphabet() {
        int i = 0;
        for (String toEncode : new String[] {"gps", "network", "passive", "nimportenawak", "autre"  } ) {
            doEncodeDecodeTest(toEncode, false);
        }
    }


    private void doEncodeDecodeTest(String toEncode,  boolean printIt) {
        int fullSize = toEncode.length();

        // Encode
        MapEncoderAdpater src = new MapEncoderAdpater();
        src.putString(field, toEncode);
        StringBuilder dest = new StringBuilder();
        // Do encode
        service.writeTo(src,  dest,   field, 'y', false);
        String encoded = dest.toString();
        if (printIt) {
            System.out.println("Gps Provider [" + toEncode + "] : encoded to [" + encoded + "]");
        }
        // Decode
        MapEncoderAdpater decodedMap = new MapEncoderAdpater();
        service.readTo(decodedMap, encoded, field);
        String decoded = decodedMap.getString(field);
        Assert.assertEquals(toEncode, decoded);
        if (printIt) {
            System.out.println(String.format("Encoded Message (%s chars /%s) : %s for String value %s", encoded.length(), fullSize, encoded, toEncode));
        }
    }

}
