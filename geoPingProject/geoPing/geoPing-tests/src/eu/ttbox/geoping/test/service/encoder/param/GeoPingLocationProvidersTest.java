package eu.ttbox.geoping.test.service.encoder.param;

import java.util.ArrayList;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;
import eu.ttbox.geoping.service.encoder.params.GeoPingLocationProviders;
import eu.ttbox.geoping.service.encoder.params.GeoPingLocationProviders.ProviderEnum;

public class GeoPingLocationProvidersTest extends AndroidTestCase {

	public static final String TAG = "GeoPingLocationProvidersTest";

	@SmallTest
	public void testSetProviderAndReadSelected() {
		// Test Ordered
		for (ProviderEnum val : ProviderEnum.values()) {
			GeoPingLocationProviders pvd = new GeoPingLocationProviders();
			pvd.setSelected(val);
			Log.d(TAG, String.format("Set Selected (%s chars) =>  %s ", val, pvd));
			assertEquals(val, pvd.getSelected());
		}
		// Test Inverse Order
		for (int i = ProviderEnum.values().length - 1; i >= 0; i--) {
			ProviderEnum val = ProviderEnum.values()[i];
			GeoPingLocationProviders pvd = new GeoPingLocationProviders();
			pvd.setSelected(val);
			Log.d(TAG, String.format("Set Selected (%s chars) =>  %s ", val, pvd));
			assertEquals(val, pvd.getSelected());
		}
	}

	@SmallTest
	public void testSetProviderAndReadSelectedRewrite() {
		GeoPingLocationProviders pvd = new GeoPingLocationProviders();
		// Test Ordered
		for (ProviderEnum val : ProviderEnum.values()) {
			pvd.setSelected(val);
			Log.d(TAG, String.format("Set Selected (%s chars) =>  %s ", val, pvd));
			assertEquals(val, pvd.getSelected());
		}
		// Test Inverse Order
		for (int i = ProviderEnum.values().length - 1; i >= 0; i--) {
			ProviderEnum val = ProviderEnum.values()[i];
			pvd.setSelected(val);
			Log.d(TAG, String.format("Set Selected (%s chars) =>  %s ", val, pvd));
			assertEquals(val, pvd.getSelected());
		}
		// Test Ordered
		for (ProviderEnum val : ProviderEnum.values()) {
			pvd.setSelected(val);
			Log.d(TAG, String.format("Set Selected (%s chars) =>  %s ", val, pvd));
			assertEquals(val, pvd.getSelected());
		}
	}

	@SmallTest
	public void testSetAvailable() {
		ProviderEnum[][] tests = new ProviderEnum[][] { //
		{ ProviderEnum.gps }, //
				{ ProviderEnum.passive }, //
				{ ProviderEnum.other }, //
				{ ProviderEnum.network }, //
				{ ProviderEnum.gps }, //
				{ ProviderEnum.network, ProviderEnum.gps  }, //
				{ ProviderEnum.network, ProviderEnum.gps, ProviderEnum.passive  }, //
				{ ProviderEnum.network, ProviderEnum.gps, ProviderEnum.passive ,  ProviderEnum.other  }, //
		};
		for (ProviderEnum[] test : tests) {
			GeoPingLocationProviders pvd = new GeoPingLocationProviders();
			pvd.setAvailable(test);
			ArrayList<ProviderEnum>  availables = pvd.getAvailables();
			assertEquals(test.length, availables.size());
			for ( ProviderEnum testVal : test ) {
				assertTrue(availables.contains(testVal));
			}
		}
	}

	//
	// @SmallTest
	// public void testSetProvider() {
	// GeoPingLocationProviders pvd = new GeoPingLocationProviders();
	// pvd.setSelected(ProviderEnum.gps );
	// pvd.setSelected(ProviderEnum.passive );
	// pvd.setSelected(ProviderEnum.network );
	// // pvd.set(ProviderEnum.other );
	//
	// pvd.setAvailable(ProviderEnum.gps );
	// pvd.setAvailable(ProviderEnum.passive );
	// pvd.setAvailable(ProviderEnum.network );
	// // pvd.set2(ProviderEnum.other );
	//
	// // Encode
	// long pvdAsLong = pvd.getBitSetAsLong();
	// String encoded = LongEncoded.toString(pvdAsLong, LongEncoded.MAX_RADIX);
	// Log.d(TAG,
	// String.format("Encoded Message (%s chars) : %s for Long value %s / %s",
	// encoded.length(), encoded, pvdAsLong, pvd));
	// }

	// public void testSetProvider2() {
	// GeoPingLocationProviders pvd = new GeoPingLocationProviders();
	// // pvd.setAvailable(ProviderEnum.gps );
	// // pvd.setAvailable(ProviderEnum.passive );
	// // pvd.setAvailable(ProviderEnum.network );
	// // pvd.setAvailable(ProviderEnum.other );
	//
	// pvd.setAvailable(ProviderEnum.gps );
	// // pvd.set2(ProviderEnum.passive );
	// // pvd.set2(ProviderEnum.network );
	// // pvd.set2(ProviderEnum.other );
	//
	// // Encode
	// long pvdAsLong = pvd.getBitSetAsLong();
	// String encoded = LongEncoded.toString(pvdAsLong, LongEncoded.MAX_RADIX);
	// Log.d(TAG,
	// String.format("Encoded 2 Message (%s chars) : %s for Long value %s / %s",
	// encoded.length(), encoded, pvdAsLong, pvd));
	// }
	//
	// public void testGetProvider2() {
	// String encoded = "g";
	// long pvdAsLong = LongEncoded.parseLong(encoded, LongEncoded.MAX_RADIX);
	// GeoPingLocationProviders pvd = new GeoPingLocationProviders(pvdAsLong);
	// Log.d(TAG,
	// String.format("Decoded Message (%s chars) : %s to Long value %s / %s",
	// encoded.length(), encoded, pvdAsLong, pvd));
	// }
}
