package eu.ttbox.geoping.encoder.adapter;


import java.util.Map;

public class MapDecoderAdpater implements  DecoderAdapter {

    private Map<String, Object> mMap;

    public MapDecoderAdpater(Map<String, Object> mMap) {
        this.mMap = mMap;
    }

    @Override
    public boolean containsKey(String key) {
        return mMap.containsKey(key);
    }



}
