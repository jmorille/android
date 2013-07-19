package eu.ttbox.geoping.encoder.params;


import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;

public interface IParamEncoder {

    // ===========================================================
    //   Encoder - Decoder Accessor
    // ===========================================================

    boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamField field, char smsFieldName  );

    boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamField field, char smsFieldName, boolean isSmsFieldName );

    // ===========================================================
    //   Decoder Accessor
    // ===========================================================

    int readTo(DecoderAdapter dest, String encoded, MessageParamField field );

}
