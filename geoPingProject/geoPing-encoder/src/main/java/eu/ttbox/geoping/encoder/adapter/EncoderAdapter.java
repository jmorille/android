package eu.ttbox.geoping.encoder.adapter;


import java.util.Set;

public interface EncoderAdapter {

    public boolean containsKey(String key);

    public Object get(String key);

    public Set<String> keySet() ;


    public String getString(String key);
}
