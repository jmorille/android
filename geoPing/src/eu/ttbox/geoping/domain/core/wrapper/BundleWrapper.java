package eu.ttbox.geoping.domain.core.wrapper;

import java.util.Set;

import android.os.Bundle;

public class BundleWrapper implements HelperWrapper<Bundle> {

    private Bundle bundle;

    public BundleWrapper() {
        this.bundle = new Bundle();
    }
    
    public BundleWrapper(int size) {
        this.bundle = new Bundle(size);
    }

    // public void putAll(Bundle map) {

    public BundleWrapper(Bundle bundle) {
        super();
        this.bundle = bundle;
    }

    public Bundle getWrappedValue() {
        return bundle;
    }

    public Bundle getBundle() {
        return bundle;
    }

    public int size() {
        return bundle.size();
    }

    public boolean isEmpty() {
        return bundle.isEmpty();
    }

    public void clear() {
        bundle.clear();
    }

    public boolean containsKey(String key) {
        return bundle.containsKey(key);
    }

    public Object get(String key) {
        return bundle.get(key);
    }

    public void remove(String key) {
        bundle.remove(key);
    }

    public Set<String> keySet() {
        return bundle.keySet();
    }

    // Accessors

    @Override
    public void putLong(String colId, Long valueOf) {
        bundle.putLong(colId, valueOf);
    }

    @Override
    public long getLong(String key) {
        return bundle.getLong(key);
    }

    // @Override
    // public long getLong(String key, long defaultValue) {
    // return bundle.getLong(key, defaultValue);
    // }

    @Override
    public void putString(String colUserid, String userId) {
        bundle.putString(colUserid, userId);
    }

    @Override
    public String getString(String key) {
        return bundle.getString(key);
    }

    // @Override
    // public String getString(String key, String defaultValue) {
    // return bundle.getString(key, defaultValue);
    // }

    @Override
    public void putInt(String colLatitudeE6, int latitudeE6) {
        bundle.putInt(colLatitudeE6, latitudeE6);

    }

    @Override
    public int getInt(String key) {
        return bundle.getInt(key);
    }
    //
    // @Override
    // public int getInt(String key, int defaultValue) {
    // return bundle.getInt(key, defaultValue);
    // }

}
