package eu.ttbox.geoping.test.core.crypto;

import javax.crypto.SecretKey;

import android.location.Location;
import android.os.Bundle;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.core.crypto.DesEncrypter;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.service.encoder.SmsMessageActionEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageIntentEncoderHelper;

public class DesEncrypterTest extends AndroidTestCase {

    public static final String TAG = "DesEncrypterTest";

    public String getMessage() {
        return "Test message to encrypt";
    }

    public String getMessageLoc() {
        String provider = "network";
        Location loc = new Location(provider);
        loc.setTime(System.currentTimeMillis());
        loc.setLatitude(43.15854941164189446d);
        loc.setLongitude(25.218546646446d);
        loc.setAccuracy(120.258446418974f);
        loc.setAltitude(124.6546533464d);
        loc.setBearing(257.16416464646446464646413f);
        loc.setSpeed(125.1464646464468946444646f);
        // Convertion 2 String
        GeoTrack geotrack = new GeoTrack(null, loc);
        Bundle params = GeoTrackHelper.getBundleValues(geotrack);
        String msg = SmsMessageIntentEncoderHelper.encodeSmsMessage(SmsMessageActionEnum.ACTION_GEO_LOC, params);

        return msg;
    }
    
    

    public void testEncrypt() throws Exception {
        String clearText = getMessage();
        doEncryptDecryptTest(clearText);
     }



    public void testEncryptMessageLoc() throws Exception {
        String clearText = getMessageLoc();
        doEncryptDecryptTest(clearText);
    }
    
    private void doEncryptDecryptTest(String clearText) throws Exception {
        Log.d(TAG, "clearText Size : " + clearText.length() + " / for msg : " + clearText);
        // encrypt
        SecretKey key = DesEncrypter.generateKey(); 
        assertNotNull(key); 
        DesEncrypter encrypter = new DesEncrypter(key);
        // Encrypt 
        String encrypted =  encrypter.encrypt(clearText);
        Log.d(TAG, "DES encryped Size : " + encrypted.length() + " / for msg : " + encrypted);
        // Decrypt 
        String clearResp = encrypter.decrypt(encrypted);
        Log.d(TAG, "DES response Clear Size : " + clearResp.length() + " / for msg : " + clearResp);
        assertEquals(clearText, clearResp);
    }
}
