package eu.ttbox.geoping.service.encoder.params;

import java.util.BitSet;

import eu.ttbox.geoping.service.encoder.params.BitSetHelper;

public class GeoPingLocationProviders {

    public enum ProviderEnum {
        passive(0), network(1), gps(2), other(3);

        public final int id;

        ProviderEnum(int id) {
            this.id = id;
        }
    }

    private final BitSet bits;

    public GeoPingLocationProviders() {
        super();
        this.bits = new BitSet();
    }

    public GeoPingLocationProviders(long bitsAsLong) {
        super();
        this.bits = BitSetHelper.convert(bitsAsLong);
    }

    public void set(ProviderEnum provider) {
        set(provider, true);
    }
    public void set(ProviderEnum provider, boolean state) {
        bits.set(provider.id, state);
    }

    public void set2(ProviderEnum provider) {
        set2(provider, true);
    }

    public void set2(ProviderEnum provider, boolean state) {
        bits.set(provider.id+4, state);
    }

    public long getBitSetAsLong() {
        return BitSetHelper.convert(bits);
    }

    @Override
    public String toString() {
        return "GeoPingLocationProviders [bits=" + bits + "]";
    }

    
    
}
