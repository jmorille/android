package eu.ttbox.geoping.encoder.params.encoding;

import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.params.IParamEncoder;
import eu.ttbox.geoping.encoder.params.MessageParamField;


public class StringParamEncoder implements IParamEncoder {

    // ===========================================================
    //   Encoder Accessor
    // ===========================================================

    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamField field, char smsFieldName  ) {
        return writeTo(src, dest, field, smsFieldName, true);
    }


    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamField field, char smsFieldName, boolean isSmsFieldName ) {
        boolean isWrite = false;
        String value = (String)src.get(field.dbFieldName);
        if (value!=null && value.length()>0) {
            if (isSmsFieldName) {
                dest.append( smsFieldName);
            }
            dest.append(value);
            isWrite = true;
        }
        return isWrite;
    }

    // ===========================================================
    //   Decoder Accessor
    // ===========================================================

    @Override
    public int readTo(DecoderAdapter dest, String encoded, MessageParamField field ) {
        String decodedValue = encoded;
        dest.putString( field.dbFieldName, decodedValue);
        return 1;
    }

}
