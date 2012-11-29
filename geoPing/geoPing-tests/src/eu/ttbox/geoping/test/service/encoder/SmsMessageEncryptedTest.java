package eu.ttbox.geoping.test.service.encoder;

import java.security.PrivateKey;
import java.security.PublicKey;

import eu.ttbox.geoping.crypto.encrypt.Base64EncodingTextEncryptor;
import eu.ttbox.geoping.crypto.encrypt.RsaBytesEncryptor;
import eu.ttbox.geoping.crypto.encrypt.TextEncryptor;
import android.test.AndroidTestCase;
import android.util.Log;

public class SmsMessageEncryptedTest extends SmsMessageEncoderHelperTest  {

    public static final String TAG = "SmsMessageEncryptedTest";
    
    public RsaBytesEncryptor getBytesEncryptor() {
        RsaBytesEncryptor bytesEncryptor = new RsaBytesEncryptor();
        PublicKey pubKey = bytesEncryptor.getPubKey();
        PrivateKey privateKey = bytesEncryptor.getPrivateKey();
        Log.d(TAG, "PublicKey : " + pubKey);
        Log.d(TAG, "PrivateKey : " + privateKey);
        return bytesEncryptor;
    }

    @Override
    public TextEncryptor getEncryptorService() {
        RsaBytesEncryptor bytesEncryptor = getBytesEncryptor(); 
        Base64EncodingTextEncryptor textEncryptor = new Base64EncodingTextEncryptor(bytesEncryptor); 
        return textEncryptor;
    }
    
    
    
    
    
}
