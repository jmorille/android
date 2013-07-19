package eu.ttbox.geoping.encoder.params.encoder;

import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.model.MessageParamType;
import eu.ttbox.geoping.encoder.params.IParamEncoder;
import eu.ttbox.geoping.encoder.params.helper.IntegerEncoded;
import eu.ttbox.geoping.encoder.params.helper.LongEncoded;


public class LongParamEncoder  implements IParamEncoder {

    public final int radix;


    // ===========================================================
    //   Contructor
    // ===========================================================



    public LongParamEncoder( ) {
       this( LongEncoded.MAX_RADIX);
    }


    public LongParamEncoder(int radix) {
        this.radix = radix;
    }

    // ===========================================================
    //   Encoder - Decoder Accessor
    // ===========================================================
    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName  ) {
        return writeTo(src, dest, field, smsFieldName, true);
    }


    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName, boolean isSmsFieldName ) {
        boolean isWrite = false;
        Long value =  (Long) src.get(field.dbFieldName) ;
        if (value != null) {
            String valueString = LongEncoded.toString(value, LongEncoded.MAX_RADIX);
            if (isSmsFieldName) {
                dest.append( smsFieldName);
            }
            dest.append(value);
            isWrite = true;
        }
        return isWrite;
    }
}
