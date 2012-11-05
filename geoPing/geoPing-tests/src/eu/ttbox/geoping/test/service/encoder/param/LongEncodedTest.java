package eu.ttbox.geoping.test.service.encoder.param;

import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.service.encoder.params.IntegerEncoded;
import eu.ttbox.geoping.service.encoder.params.LongEncoded;

public class LongEncodedTest extends AndroidTestCase {

    public static final String TAG = "LongEncodedTest";

    public static long[] VALUES_TESTED = new long[] { //
    -1, -2, -3, -7, -42, -73, 0, 1, 2, 3, 7, 42, 73, //
             Long.MIN_VALUE + 1, Long.MIN_VALUE + 2, Long.MAX_VALUE - 1, Long.MAX_VALUE - 2//
            , Long.MIN_VALUE, Long.MAX_VALUE //
    };

    public void testEncode() {
        int radix = IntegerEncoded.MAX_RADIX;
        for (long i = 0; i < 2000; i++) {
            doEncodeDecodeTest(i, radix);
        }
        // Int
        for (long i : IntegerEncodedTest.VALUES_TESTED) {
            doEncodeDecodeTest(i, radix);
        } 
        // Long
        for (long i : VALUES_TESTED) {
            doEncodeDecodeTest(i, radix);
        }
    }

    private void doEncodeDecodeTest(long i, int radix) {
        int fullSize = String.valueOf(i).length();
        String encoded = LongEncoded.toString(i, radix);
        long decoded = LongEncoded.parseLong(encoded, radix);
        assertEquals(i, decoded);
        Log.d(TAG, String.format("Encoded Message (%s chars /%s) : %s for Long value %s", encoded.length(), fullSize, encoded, i));
    }

}
