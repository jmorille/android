package eu.ttbox.geoping.encoder.params.sandbox;

 

import java.util.ArrayList;
import java.util.BitSet;

import eu.ttbox.geoping.encoder.params.helper.BitSetHelper;

public class GeoPingLocationProviders {

	private static final int SPLIT_LIMIT = 4;
	private static final int MAX_LIMIT = SPLIT_LIMIT * 2;

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


	public void setSelected(ProviderEnum provider) {
		for (ProviderEnum pv : ProviderEnum.values()) {
			if (pv == provider) {
				setSelected(pv, true);
			} else {
				setSelected(pv, false);
			}
		}
	}

	public ProviderEnum getSelected() {
		ProviderEnum selected = null;
		if (bits != null) {
			int idxBit = bits.nextSetBit(0);
			if (idxBit > -1 && idxBit < SPLIT_LIMIT) {
				selected = ProviderEnum.values()[idxBit];
			}
		}
		return selected;
	}

	private void setSelected(ProviderEnum provider, boolean state) {
		bits.set(provider.id, state);
	}

	public void setAvailable(ProviderEnum provider) {
		setAvailable(provider, true);
	}

	public void setAvailable(ProviderEnum[] providers) {
		for (ProviderEnum provider : providers) {
			setAvailable(provider, true);
		}
	}
	
	private void setAvailable(ProviderEnum provider, boolean state) {
		bits.set(provider.id + SPLIT_LIMIT, state);
	}

	public ArrayList<ProviderEnum> getAvailables() {
		ArrayList<ProviderEnum> availables = new ArrayList<ProviderEnum>();

		int idxBit = bits.nextSetBit(SPLIT_LIMIT);
		do {
			//TODO Log.d("GeoPingLocationProviders", "getAvailables  idxBit=" + idxBit);
			if (idxBit > -1) {
				int idxEnum = idxBit - SPLIT_LIMIT;
				ProviderEnum val =  ProviderEnum.values()[idxEnum];
				availables.add(val);
			}
			idxBit = bits.nextSetBit(idxBit+1);
		} while (idxBit > -1 && idxBit < MAX_LIMIT);

		 
		return availables;
	}

	public long getBitSetAsLong() {
		return BitSetHelper.convert(bits);
	}

	@Override
	public String toString() {
		return "GeoPingLocationProviders [bits=" + bits + "]";
	}

}
