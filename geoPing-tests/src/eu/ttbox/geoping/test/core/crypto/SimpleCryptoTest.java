package eu.ttbox.geoping.test.core.crypto;

import eu.ttbox.geoping.core.crypto.SimpleCrypto;
import android.test.AndroidTestCase;
import android.util.Log;

public class SimpleCryptoTest  extends AndroidTestCase {

	public static final String TAG = "SimpleCryptoTest";
	public static final String SEED = "monsel";
	
	public String getMessage() {
		return "Test message to encrypt";
	}
	
	public void testEncrypt() throws Exception {
		String clearText = getMessage();
		Log.d(TAG, "clearText Size : " + clearText.length()+ " / for msg : "+ clearText);
		String encryped = SimpleCrypto.encrypt(SEED, clearText);
		Log.d(TAG, "encryped Size : " + encryped.length() + " / for msg : "+ encryped);
		
		
	}
}
