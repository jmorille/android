package eu.ttbox.geoping.test.service.encoder.param;

import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.service.encoder.SmsParamEncoderHelper;
import eu.ttbox.geoping.service.encoder.params.GeoPingLocationProviders;
import eu.ttbox.geoping.service.encoder.params.GeoPingLocationProviders.ProviderEnum;
import eu.ttbox.geoping.service.encoder.params.LongEncoded;

public class GeoPingLocationProvidersTest extends AndroidTestCase {

    public static final String TAG = "GeoPingLocationProvidersTest";
    
    
    public void testSetProvider() {
        GeoPingLocationProviders pvd = new GeoPingLocationProviders();
        pvd.set(ProviderEnum.gps );
        pvd.set(ProviderEnum.passive );
        pvd.set(ProviderEnum.network );
//        pvd.set(ProviderEnum.other );

        pvd.set2(ProviderEnum.gps );
//        pvd.set2(ProviderEnum.passive );
//        pvd.set2(ProviderEnum.network );
//        pvd.set2(ProviderEnum.other );

        // Encode
        long pvdAsLong = pvd.getBitSetAsLong();
        String encoded = LongEncoded.toString(pvdAsLong, LongEncoded.MAX_RADIX);
        Log.d(TAG, String.format("Encoded Message (%s chars) : %s for Long value %s / %s", encoded.length(), encoded, pvdAsLong, pvd));
    }

}
