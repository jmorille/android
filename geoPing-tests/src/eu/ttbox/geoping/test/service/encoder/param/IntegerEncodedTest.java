package eu.ttbox.geoping.test.service.encoder.param;

import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.service.encoder.params.IntegerEncoded;

public class IntegerEncodedTest extends AndroidTestCase {

    public static final String TAG = "IntegerEncodedTest";

    public static int[] VALUES_TESTED = new int[] { //
    -1, -2, -3, -7, -42, -73, 0, 1, 2, 3, 7, 42, 73, //
             Integer.MIN_VALUE + 1, Integer.MIN_VALUE + 2, Integer.MAX_VALUE - 1, Integer.MAX_VALUE - 2//
            , Integer.MIN_VALUE, Integer.MAX_VALUE //
    };

    public void testAlphabet() {
        int radix = IntegerEncoded.MAX_RADIX;
        int i = 0;
        for (char c : IntegerEncoded.ALPHABET) {
            String str = String.valueOf(c);
            int decoded = IntegerEncoded.parseInt(str, radix);
            assertEquals(i++, decoded);
        }
    }

    public void testEncodeMaxRadix() {
        int radix = IntegerEncoded.MAX_RADIX;
        for (int i = 0; i < 2000; i++) {
            doEncodeDecodeTest(i, radix);
        }
        for (int i : VALUES_TESTED) {
            doEncodeDecodeTest(i, radix);
        }
    }

    public void testEncodeMulTiRadix() {
        for (int radix : new int[] { 4, 5, 10, 36, IntegerEncoded.MAX_RADIX }) {
            for (int i = 0; i < 2000; i++) {
                doEncodeDecodeTest(i, radix, false);
            }
            for (int i : VALUES_TESTED) {
                doEncodeDecodeTest(i, radix, false);
            }
        }
    }

    private void doEncodeDecodeTest(int i, int radix) {
        doEncodeDecodeTest(i, radix, true);
    }

    private void doEncodeDecodeTest(int i, int radix, boolean printIt) {
        int fullSize = String.valueOf(i).length();
        String encoded = IntegerEncoded.toString(i, radix);
        int decoded = IntegerEncoded.parseInt(encoded, radix);
        assertEquals(i, decoded);
        Log.d(TAG, String.format("Encoded Message (%s chars /%s) : %s for Int value %s", encoded.length(), fullSize, encoded, i));
    }
}
