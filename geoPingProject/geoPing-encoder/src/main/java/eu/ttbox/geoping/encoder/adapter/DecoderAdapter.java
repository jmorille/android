package eu.ttbox.geoping.encoder.adapter;

import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.encoder.params.MessageParamField;

public interface DecoderAdapter {

    DecoderAdapter newInstance();


    void setAction(MessageActionEnum action);
    void setPhone(String phone);


    boolean isEmpty();
    boolean containsKey(String key);

    void putString(String dbFieldName, String decodedValue);
    void putString(MessageParamField key, String value);

    void putInt(String key, int value);
    void putInt(MessageParamField key, int value);

    void putLong(String colId, long valueOf);
    void putLong(MessageParamField key, long value);

    void putFloat(String key, float value);
    void putFloat(MessageParamField key, float value);

    void putDouble(String key, double value);
    void putDouble(MessageParamField key, double value);

}
