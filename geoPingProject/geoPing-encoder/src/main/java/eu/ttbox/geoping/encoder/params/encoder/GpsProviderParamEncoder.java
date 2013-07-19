package eu.ttbox.geoping.encoder.params.encoder;

import java.util.HashMap;

import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.params.IParamEncoder;
import eu.ttbox.geoping.encoder.model.MessageParamType;


public class GpsProviderParamEncoder implements IParamEncoder {


    // ===========================================================
    // Location Provider Encoder
    // ===========================================================

    private static HashMap<String, String> locProviderEncoder;
    private static HashMap<String, String> locProviderDecoder;

    private static String[][] locProviders = new String[][] { //
            { "gps", "g" }, //
            { "network", "n" }, //
            { "passive", "p" } //
    };

    static {
        HashMap<String, String> encoder = new HashMap<String, String>(3);
        HashMap<String, String> decoder = new HashMap<String, String>(3);

        for (String[] provider : locProviders) {
            encoder.put(provider[0], provider[1]);
            decoder.put(provider[1], provider[0]);
        }
        locProviderEncoder = encoder;
        locProviderDecoder = decoder;
    }


    // ===========================================================
    //  Encoder
    // ===========================================================
    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName  ) {
        return writeTo(src, dest, field, smsFieldName, true);
    }


    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName, boolean isSmsFieldName ) {
        boolean isWrite = false;
        String value = (String)src.get(field.dbFieldName);
        value = locProviderDecoder.get(value);
        if (value!=null && value.length()>0) {
            if (isSmsFieldName) {
                dest.append( smsFieldName);
            }
            dest.append(value);
            isWrite = true;
        }
        return isWrite;
    }

    public String decodeToGpsProvider(String value) {
        String result = locProviderDecoder.get(value);
        return result == null ? value : result;
    }

}
