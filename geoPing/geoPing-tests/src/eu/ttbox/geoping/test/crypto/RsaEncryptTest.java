package eu.ttbox.geoping.test.crypto;
 
import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.crypto.encrypt.RsaBytesEncryptor;
import eu.ttbox.geoping.crypto.encrypt.TextEncryptor;
import eu.ttbox.geoping.crypto.encrypt.HexEncodingTextEncryptor;

public class RsaEncryptTest extends AndroidTestCase {

	public static final String TAG = "RsaEncryptTest";

	public TextEncryptor getService() {
		RsaBytesEncryptor bytesEncryptor = new RsaBytesEncryptor();
		HexEncodingTextEncryptor textEncryptor = new HexEncodingTextEncryptor(
				bytesEncryptor);
		return textEncryptor;
	}

	public void testEncrypt() throws Exception {
		TextEncryptor textEncyptor = getService();
		String msg = "Test message to encrypt";
		String encrypted = textEncyptor.encrypt(msg);
		Log.d(TAG, "RSA encryped Size : " + encrypted.length() + " / for msg : " + encrypted);
		String decrypted = textEncyptor.decrypt(encrypted);
		Log.d(TAG, "RSA decrypted Size : " + decrypted.length() + " / for msg : " + decrypted);
		assertEquals(msg, decrypted);

	}

}
