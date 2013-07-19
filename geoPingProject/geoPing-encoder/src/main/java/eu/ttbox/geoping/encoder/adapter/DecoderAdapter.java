package eu.ttbox.geoping.encoder.adapter;

public interface DecoderAdapter {

    boolean containsKey(String key);

    void putString(String dbFieldName, String decodedValue);

    void putInt(String key, int value);

    void putLong(String colId, long valueOf);

    void putFloat(String key, float value);

    void putDouble(String key, double value);

}
