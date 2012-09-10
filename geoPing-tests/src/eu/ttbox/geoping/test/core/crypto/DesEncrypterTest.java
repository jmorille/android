package eu.ttbox.geoping.test.core.crypto;

import javax.crypto.SecretKey;

import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.core.crypto.DesEncrypter;

public class DesEncrypterTest  extends AndroidTestCase {

	public static final String TAG = "DesEncrypterTest";
 
	
	public String getMessage() {
		return "Test message to encrypt";
	}
	
	public void testEncrypt() throws Exception {
		 String clearText = getMessage();
		Log.d(TAG, "clearText Size : " + clearText.length()+ " / for msg : "+ clearText);
// encrypt
		 SecretKey key =  DesEncrypter.generateKey();
		 DesEncrypter encrypter = new DesEncrypter(key);

		String encryped = encrypter.encrypt(clearText) ;
		Log.d(TAG, "encryped Size : " + encryped.length() + " / for msg : "+ encryped);
		
		
	}

}
