package eu.ttbox.geoping.encoder.params.encoder;

import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.params.IParamEncoder;
import eu.ttbox.geoping.encoder.model.MessageParamType;


public class StringParamEncoder implements IParamEncoder {


    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName  ) {
        return writeTo(src, dest, field, smsFieldName, true);
    }


    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName, boolean isSmsFieldName ) {
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


}
