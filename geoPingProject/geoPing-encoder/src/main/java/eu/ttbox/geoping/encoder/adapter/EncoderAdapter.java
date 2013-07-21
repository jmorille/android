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

    String getString(String key);
   // String getString(MessageParamField key );

  //  int getInt(String key);
  //  int getInt(MessageParamField key );

  //  long getLong(String colId);
  //  long getLong(MessageParamField key);

   // float getFloat(String key);
   // float getFloat(MessageParamField key );

    //double getDouble(String key );
    //double getDouble(MessageParamField key );

}
