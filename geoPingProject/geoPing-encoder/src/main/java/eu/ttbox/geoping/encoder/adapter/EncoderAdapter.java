package eu.ttbox.geoping.encoder.adapter;


import java.util.Set;

import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.encoder.params.MessageParamField;

public interface EncoderAdapter {

    MessageActionEnum getAction();

    String getPhone();

    boolean isEmpty();

    boolean containsKey(String key);

    Object get(String key);

    Set<String> keySet();



    String getString(MessageParamField key);

    String getString(MessageParamField key, String defaultValue);

    String getString(String key);

    String getString(String key, String defaultValue);



    int getInt(MessageParamField key, int value);

    int getInt(MessageParamField key);

    int getInt(String key);

    int getInt(String key, int defaultValue);



    long getLong(MessageParamField key);

    long getLong(MessageParamField key, long defaultValue);

    long getLong(String key);

    long getLong(String key, long defaultValue);



    float getFloat(MessageParamField key);

    float getFloat(MessageParamField key, float defaultValue);

    float getFloat(String key);

    float getFloat(String key, float defaultValue);


    //double getDouble(String key );
    //double getDouble(MessageParamField key );

}
