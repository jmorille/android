package eu.ttbox.geoping.test;

import android.test.AndroidTestCase;
import android.util.Log;

public class GaBuZoMeuConverterTest extends AndroidTestCase {

	private static final String TAG = "GaBuZoMeuConverterTest";

	public void testEncode() {
		int[] numbers = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 42, 73 };
		for (int nb : numbers) {
			String converted = Integer.toString(nb, 4);
			// String shadok = converted.replaceAll("0", "G").replaceAll("1",
			// "B").replaceAll("2", "Z").replaceAll("3", "M");
			String shadok = converted.replaceAll("0", "GA ").replaceAll("1", "BU ").replaceAll("2", "ZO ").replaceAll("3", "MEU ");
			Log.d(TAG, String.format("Nombre %s  =>  %s  => %s", nb, converted, shadok));
		}
	}

	public void testDecodet() {
		String[] numbers = new String[] { "GA", //
				"BU", //
				"ZO", //
				"MEU", //
				"ZO ZO ZO", //
				"BU GA ZO BU",//
				"BUGAZOBU" //
		};
		for (String nb : numbers) {
			String base4 = nb.replaceAll("GA", "0").replaceAll("BU", "1").replaceAll("ZO", "2").replaceAll("MEU", "3").replaceAll(" ", "");
			Integer converted = Integer.valueOf(base4, 4);
			// String shadok = converted.replaceAll("0", "G").replaceAll("1",
			// "B").replaceAll("2", "Z").replaceAll("3", "M");

			Log.d(TAG, String.format("Shadok %s  =>  %s  => %s", nb, base4, converted));
		}
	}

}
