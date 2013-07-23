package eu.ttbox.geoping.encoder.adapter;



import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.encoder.params.MessageParamField;

public class MapEncoderAdpater implements EncoderDecoderAdapter {

    private Map<String, Object> mMap;

    private MessageActionEnum action;
    private String phone;


    // ===========================================================
    //   Constructor
    // ===========================================================

    public MapEncoderAdpater() {
        this(new HashMap<String, Object>());
    }

    public MapEncoderAdpater(Map<String, Object> mMap) {
        this.mMap = mMap;
    }

    @Override
    public MapEncoderAdpater newInstance() {
        return new MapEncoderAdpater();
    }

    // ===========================================================
    //   Direct Accessor
    // ===========================================================

    @Override
    public MessageActionEnum getAction() {
        return action;
    }

    @Override
    public void setAction(MessageActionEnum action) {
        this.action = action;
    }

    @Override
    public String getPhone() {
        return phone;
    }

    @Override
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public  Map<String, Object> getMap(){
        return mMap;
    }

    public boolean isEmpty() {
        return mMap==null || mMap.isEmpty();
    }

    @Override
    public boolean containsKey(String key) {
        return mMap.containsKey(key);
    }

     @Override
    public Set<String> keySet() {
        return mMap.keySet();
    }

    // ===========================================================
    //   Error Management
    // ===========================================================


    private void typeWarning(String key, Object value, String className,
                             ClassCastException e) {
        typeWarning(key, value, className, "<null>", e);
    }

    private void typeWarning(String key, Object value, String className,
                             Object defaultValue, ClassCastException e) {
        StringBuilder sb = new StringBuilder();
        sb.append("Key ");
        sb.append(key);
        sb.append(" expected ");
        sb.append(className);
        sb.append(" but value was a ");
        sb.append(value.getClass().getName());
        sb.append(".  The default value ");
        sb.append(defaultValue);
        sb.append(" was returned.");
        // Print Type Warning to Log
        logTypeWarning(sb, e);
    }


    public void logTypeWarning(StringBuilder typeWarning, ClassCastException e ) {
        //TODO     Log.w(LOG_TAG, sb.toString());
        // TODO     Log.w(LOG_TAG, "Attempt to cast generated internal exception:", e);
    }

    // ===========================================================
    //   Encoder Accessor
    // ===========================================================


    @Override
    public Object get(String key) {
        return mMap.get(key);
    }


    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }


    public boolean getBoolean(String key, boolean defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Boolean) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Boolean", defaultValue, e);
            return defaultValue;
        }
    }

    @Override
    public int getInt(MessageParamField key, int value) {
        return getInt(key.dbFieldName, value);
    }
    @Override
    public int getInt(MessageParamField key) {
        return getInt(key.dbFieldName);
    }
    @Override
    public int getInt(String key) {
        return getInt(key, 0);
    }
    @Override
    public int getInt(String key, int defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Integer) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Integer", defaultValue, e);
            return defaultValue;
        }
    }

    @Override
    public long getLong(MessageParamField key ) {
        return  getLong(key.dbFieldName);
    }
    @Override
    public long getLong(MessageParamField key, long defaultValue) {
        return getLong(key.dbFieldName, defaultValue);
    }
    @Override
    public long getLong(String key ) {
        return getLong(key, 0 );
    }
    @Override
    public long getLong(String key, long defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Long) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Long", defaultValue, e);
            return defaultValue;
        }
    }

    @Override
    public float getFloat(MessageParamField key ) {
        return  getFloat(key.dbFieldName);
    }
    @Override
    public float getFloat(MessageParamField key, float defaultValue) {
        return getFloat(key.dbFieldName, defaultValue);
    }
    @Override
    public float getFloat(String key ) {
        return getFloat(key, 0 );
    }
    @Override
    public float getFloat(String key, float defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (Float) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "Float", defaultValue, e);
            return defaultValue;
        }
    }


    @Override
    public String getString(MessageParamField key) {
        return getString(key.dbFieldName);
    }

    @Override
    public String getString(MessageParamField key, String defaultValue) {
        return getString(key.dbFieldName, defaultValue);
    }

    @Override
    public String getString(String key) {
        return  getString(key, null);
    }
    @Override
    public String getString(String key, String defaultValue) {
        Object o = mMap.get(key);
        if (o == null) {
            return defaultValue;
        }
        try {
            return (String) o;
        } catch (ClassCastException e) {
            typeWarning(key, o, "String", e);
            return defaultValue;
        }
    }


    // ===========================================================
    //   Decoder Accessor
    // ===========================================================


    @Override
    public void putString(String key, String value) {
        mMap.put(key, value);
    }

    @Override
    public void putString(MessageParamField key, String value) {
        mMap.put(key.dbFieldName, value);
    }

    @Override
    public void putInt(String key, int value) {
        mMap.put(key, value);
    }

    @Override
    public void putInt(MessageParamField key, int value) {
        mMap.put(key.dbFieldName, value);
    }


    @Override
    public void putLong(String key, long value) {
        mMap.put(key, value);
    }
    @Override
    public void putLong(MessageParamField key, long value) {
        mMap.put(key.dbFieldName, value);
    }

    @Override
    public void putFloat(String key, float value){
        mMap.put(key, Float.valueOf( value));
    }
    @Override
    public void putFloat(MessageParamField key, float value){
        mMap.put(key.dbFieldName, Float.valueOf( value));
    }

    @Override
    public void putDouble(String key, double value) {
        mMap.put(key, value);
    }
    @Override
    public void putDouble(MessageParamField key, double value) {
        mMap.put(key.dbFieldName, value);
    }


    // ===========================================================
    //   Overide
    // ===========================================================


    @Override
    public String toString() {
        return "MapEncoderAdpater{" +
                "action=" + action +
                ", phone='" + phone + '\'' +
                ", mMap=" + mMap +
                '}';
    }
}
