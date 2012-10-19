package eu.ttbox.geoping.test.service.encoder.param;

import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.service.encoder.params.IntegerEncoded;
import eu.ttbox.geoping.service.encoder.params.LongEncoded;

public class LongEncodedTest  extends AndroidTestCase {

    public static final String TAG = "LongEncodedTest";
    

    public void testEncode() {
        int radix =  IntegerEncoded.MAX_RADIX;
        for (long i=0 ; i <2000;  i++) {
            doEncodeDecodeTest(i, radix);
         }
        long[] values = new long[] {
          Long.MAX_VALUE, Long.MIN_VALUE      
        };
        for (long i : values) {
            doEncodeDecodeTest(i, radix);
         }
    }
    
    private void doEncodeDecodeTest(long i,  int radix) {
        int fullSize = String.valueOf(i).length();
        String encoded=  LongEncoded.toString(i,radix);
        long decoded = LongEncoded.parseLong(encoded, radix);
        assertEquals(i, decoded);
        Log.d(TAG, String.format("Encoded Message (%s chars /%s) : %s for Long value %s", encoded.length(),fullSize, encoded, i));
    }

}
