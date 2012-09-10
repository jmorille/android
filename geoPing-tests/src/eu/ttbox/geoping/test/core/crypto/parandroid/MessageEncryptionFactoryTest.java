package eu.ttbox.geoping.test.core.crypto.parandroid;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.spec.DHParameterSpec;

import android.location.Location;
import android.test.AndroidTestCase;
import android.util.Base64;
import android.util.Log;
import eu.ttbox.geoping.core.crypto.parandroid.MessageEncryption;
import eu.ttbox.geoping.core.crypto.parandroid.MessageEncryptionFactory;
import eu.ttbox.geoping.domain.GeoTrackSmsMsg;
import eu.ttbox.geoping.service.SmsMsgActionHelper;
import eu.ttbox.geoping.service.SmsMsgEncryptHelper;

public class MessageEncryptionFactoryTest extends AndroidTestCase {

    public static final String TAG = "MessageEncryptionFactoryTest";

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

    public KeyPair generateKeyPair() throws Exception {
        // Generating keys for alice
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(MessageEncryptionFactory.KEY_EXCHANGE_PROTOCOL);
        DHParameterSpec dhSpec = new DHParameterSpec(MessageEncryptionFactory.P, MessageEncryptionFactory.G);
        keyGen.initialize(dhSpec);
        KeyPair keyPair = keyGen.generateKeyPair();
        return keyPair;
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
        KeyPair keyPair = generateKeyPair(); // MessageEncryptionFactory.generateKeyPair(getContext());
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        assertNotNull(privateKey);
        assertNotNull(publicKey);
        // Encrypt
        byte[] encrypedByte = MessageEncryption.encrypt(getContext(), privateKey, publicKey, clearText);
        String encrypted = Base64.encodeToString(encrypedByte, Base64.DEFAULT);
        Log.d(TAG, "Rsa encryped Size : " + encrypted.length() + " / for msg : " + encrypted);
        // Decrypt
        byte[] encrypedRespByte = Base64.decode(encrypted, Base64.DEFAULT);
        String clearResp = MessageEncryption.decrypt(getContext(), privateKey, publicKey, encrypedRespByte);
        Log.d(TAG, "Rsa response Clear Size : " + clearResp.length() + " / for msg : " + clearResp);
        assertEquals(clearText, clearResp);
    }

}
