package eu.ttbox.geoping.domain.core.wrapper;

import android.content.ContentValues;

public class ContentValuesWrapper implements HelperWrapper<ContentValues> {

    private ContentValues contentValues;

    public ContentValuesWrapper() {
        super();
        this.contentValues = new ContentValues();
    }
    
    public ContentValuesWrapper(int size) {
        super();
        this.contentValues = new ContentValues(size);
    }

    public ContentValuesWrapper(ContentValues contentValues) {
        super();
        this.contentValues = contentValues;
    }

    public ContentValues getWrappedValue() {
        return contentValues;
    }

    public ContentValues getContentValues() {
        return contentValues;
    }

    @Override
    public int size() {
        return contentValues.size();
    }

    @Override
    public boolean isEmpty() {
        return contentValues.size() < 1;
    }

    @Override
    public void clear() {
        contentValues.clear();
    }

    @Override
    public boolean containsKey(String key) {
        return contentValues.containsKey(key);
    }

    @Override
    public Object get(String key) {
        return contentValues.get(key);
    }

    @Override
    public void remove(String key) {
        contentValues.remove(key);
    }

    // @Override
    // public Set<String> keySet() {
    // return contentValues.keySet();
    // }
    @Override
    public void putLong(String key, Long value) {
        contentValues.put(key, value);

    }

    @Override
    public long getLong(String key) {
        return contentValues.getAsLong(key);
    }

    @Override
    public void putString(String key, String value) {
        contentValues.put(key, value);
    }

    @Override
    public String getString(String key) {
        return contentValues.getAsString(key);
    }

    @Override
    public void putInt(String key, int value) {
        contentValues.put(key, value);
    }

    @Override
    public int getInt(String key) {
        return contentValues.getAsInteger(key);
    }

 // Float
    public void putFloat(String key, float value){
        contentValues.put(key, value);

    }

    public float getFloat(String key){
        return contentValues.getAsFloat(key);
    }
}
