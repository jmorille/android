package eu.ttbox.geoping.encoder.params.encoding;

import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.params.IParamEncoder;
import eu.ttbox.geoping.encoder.params.MessageParamField;
import eu.ttbox.geoping.encoder.params.helper.Base64;
import eu.ttbox.geoping.encoder.params.helper.Base64DecoderException;


public class StringBase64ParamEncoder implements IParamEncoder {

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
            String encoded =   Base64.encodeWebSafe(value.getBytes(), false);
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
        String decodedValue = null;
        if (encoded != null) {
            try {
                decodedValue = new String( Base64.decodeWebSafe(encoded));
            } catch (Base64DecoderException e) {
              //TODO   Log.e(TAG, "Base64DecoderException : " + e.getMessage(), e);
                decodedValue = null;
            }
        }
        if (decodedValue!=null) {
            dest.putString( field.dbFieldName, decodedValue);
            return 1;
        } else {
            return 0;
        }
    }

}
