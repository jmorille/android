package eu.ttbox.geoping.encoder.params;


import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.model.MessageParamType;

public interface IParamEncoder {

    boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName  );

    boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName, boolean isSmsFieldName );

}
