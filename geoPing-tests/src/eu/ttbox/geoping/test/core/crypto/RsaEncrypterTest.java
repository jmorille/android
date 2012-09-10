package eu.ttbox.geoping.test.core.crypto;

import java.security.KeyPair;
import java.security.spec.RSAKeyGenParameterSpec;

import android.location.Location;
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.core.crypto.RsaEncrypter;
import eu.ttbox.geoping.domain.GeoTrackSmsMsg;
import eu.ttbox.geoping.service.SmsMsgActionHelper;
import eu.ttbox.geoping.service.SmsMsgEncryptHelper;

public class RsaEncrypterTest extends AndroidTestCase {

    public static final String TAG = "RsaEncrypterTest";

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
        GeoTrackSmsMsg geoTrackMsg = SmsMsgActionHelper.geoLocMessage(loc);
        String msg = SmsMsgEncryptHelper.encodeSmsMessage(geoTrackMsg);
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
        KeyPair keyPair = RsaEncrypter.generateKey(2048,RSAKeyGenParameterSpec.F4); 
        assertNotNull(keyPair); 
        
        // Encrypt 
        String encrypted =  RsaEncrypter.encrypt(keyPair.getPublic(),clearText);
        Log.d(TAG, "DES encryped Size : " + encrypted.length() + " / for msg : " + encrypted);
        // Decrypt 
        String clearResp = RsaEncrypter.decrypt(keyPair.getPrivate(), encrypted);
        Log.d(TAG, "DES response Clear Size : " + clearResp.length() + " / for msg : " + clearResp);
        assertEquals(clearText, clearResp);
    }
}
