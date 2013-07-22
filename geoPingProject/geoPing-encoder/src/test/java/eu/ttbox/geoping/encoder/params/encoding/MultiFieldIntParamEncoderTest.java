package eu.ttbox.geoping.encoder.params.encoding;


import org.junit.Assert;
import org.junit.Test;

import eu.ttbox.geoping.encoder.adapter.MapEncoderAdpater;
import eu.ttbox.geoping.encoder.model.MessageParamEnum;
import eu.ttbox.geoping.encoder.params.AssertHelper;
import eu.ttbox.geoping.encoder.params.MessageParamField;

public class MultiFieldIntParamEncoderTest {

    private MultiFieldParamEncoder service = new MultiFieldParamEncoder();

    private final MessageParamField field =   MessageParamEnum.GEO_E6.type;


    @Test
    public void testEncodeLatLngAlt() {
        // Create Map To encode
        MapEncoderAdpater mapEncode = new MapEncoderAdpater();
        mapEncode.putInt(MessageParamField.LOC_LATITUDE_E6, 43158549);
        mapEncode.putInt(MessageParamField.LOC_LONGITUDE_E6, 25218546);
        mapEncode.putInt(MessageParamField.LOC_ALTITUDE, 120);

        // Encode Message
        doEncodeDecode(mapEncode, false);
    }

    @Test
    public void testEncodeLatLng() {
        // Create Map To encode
        MapEncoderAdpater mapEncode = new MapEncoderAdpater();
        mapEncode.putInt(MessageParamField.LOC_LATITUDE_E6, 43158549);
        mapEncode.putInt(MessageParamField.LOC_LONGITUDE_E6, 25218546);

        // Encode Message
        doEncodeDecode(mapEncode, false);
    }

    @Test
    public void testEncodeLat() {
        // Create Map To encode
        MapEncoderAdpater mapEncode = new MapEncoderAdpater();
        mapEncode.putInt(MessageParamField.LOC_LATITUDE_E6, 43158549);

        // Encode Message
        doEncodeDecode(mapEncode, false);
    }


    private void doEncodeDecode( MapEncoderAdpater mapEncode ,  boolean printIt) {
        int fullSize = mapEncode.toString().length();
 
        StringBuilder dest = new StringBuilder();
        // Do encode
        service.writeTo(mapEncode,  dest,   field, 'y', false);
        String encoded = dest.toString();
        if (printIt)
            System.out.println("Encoded String : " + encoded + " for " + mapEncode);

        // Decode
        MapEncoderAdpater decodedMap = new MapEncoderAdpater();
        service.readTo(decodedMap, encoded, field);

        AssertHelper.assertMap(mapEncode, decodedMap, printIt);

        if (printIt) {
            System.out.println(String.format("Encoded Message (%s chars /%s) : %s for String value %s", encoded.length(), fullSize, encoded, mapEncode));

        }
    }

}
