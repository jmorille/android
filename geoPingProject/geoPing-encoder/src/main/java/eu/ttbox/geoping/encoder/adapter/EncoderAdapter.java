package eu.ttbox.geoping.encoder.adapter;


import java.util.Set;

public interface EncoderAdapter {

    boolean isEmpty();

    public boolean containsKey(String key);

    public Object get(String key);

    public Set<String> keySet() ;


    public String getString(String key);
}
