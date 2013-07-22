package eu.ttbox.geoping.service.encoder.adpater;


import android.os.Bundle;

import java.util.Set;

import eu.ttbox.geoping.encoder.adapter.EncoderDecoderAdapter;
import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.encoder.params.MessageParamField;

public class BundleEncoderAdapter implements EncoderDecoderAdapter {

    private Bundle mMap;
    private MessageActionEnum action;
    private String phone;


    // ===========================================================
    //   Constructor
    // ===========================================================
    public BundleEncoderAdapter() {
        this(null, new Bundle());
    }

    public BundleEncoderAdapter(MessageActionEnum action) {
        this(action, new Bundle());
    }

    public BundleEncoderAdapter(MessageActionEnum action, Bundle mMap) {
        this.action = action;
        this.mMap = mMap;
    }

    @Override
    public BundleEncoderAdapter newInstance() {
        return new BundleEncoderAdapter();
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

    public Bundle getMap() {
        return mMap;
    }

    public boolean isEmpty() {
        return mMap.isEmpty();
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
    //   Encoder Accessor
    // ===========================================================

    @Override
    public Object get(String key) {
        return mMap.get(key);
    }


    public boolean getBoolean(String key) {
        return mMap.getBoolean(key);
    }


    public boolean getBoolean(String key, boolean defaultValue) {
       return mMap.getBoolean(key, defaultValue);
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
        return mMap.getInt(key);
    }
    @Override
    public int getInt(String key, int defaultValue) {
        return  mMap.getInt(key, defaultValue);
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
        return mMap.getLong(key);
    }
    @Override
    public long getLong(String key, long defaultValue) {
       return mMap.getLong(key, defaultValue);
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
        return mMap.getFloat(key);
    }
    @Override
    public float getFloat(String key, float defaultValue) {
        return mMap.getFloat(key, defaultValue);
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
        return mMap.getString(key);
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
            //TODO   mMap.typeWarning(key, o, "String", e);
            return defaultValue;
        }
    }


    // ===========================================================
    //   Decoder Accessor
    // ===========================================================


    @Override
    public void putString(String key, String value) {
        mMap.putString(key, value);
    }

    @Override
    public void putString(MessageParamField key, String value) {
        mMap.putString(key.dbFieldName, value);
    }

    @Override
    public void putInt(String key, int value) {
        mMap.putInt(key, value);
    }

    @Override
    public void putInt(MessageParamField key, int value) {
        mMap.putInt(key.dbFieldName, value);
    }


    @Override
    public void putLong(String key, long value) {
        mMap.putLong(key, value);
    }
    @Override
    public void putLong(MessageParamField key, long value) {
        mMap.putLong(key.dbFieldName, value);
    }

    @Override
    public void putFloat(String key, float value){
        mMap.putFloat(key, Float.valueOf(value));
    }
    @Override
    public void putFloat(MessageParamField key, float value){
        mMap.putFloat(key.dbFieldName, Float.valueOf(value));
    }

    @Override
    public void putDouble(String key, double value) {
        mMap.putDouble(key, value);
    }
    @Override
    public void putDouble(MessageParamField key, double value) {
        mMap.putDouble(key.dbFieldName, value);
    }


    // ===========================================================
    // Override
    // ===========================================================


    @Override
    public String toString() {
        return "BundleEncoderAdapter{" +
                "action=" + action +
                ", phone='" + phone + '\'' +
                ", mMap=" + mMap +
                '}';
    }
}
