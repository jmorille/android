package eu.ttbox.geoping.test.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;

import android.test.AndroidTestCase;
import android.util.Log;
import eu.ttbox.geoping.crypto.encrypt.Base64EncodingTextEncryptor;
import eu.ttbox.geoping.crypto.encrypt.RsaBytesEncryptor;
import eu.ttbox.geoping.crypto.encrypt.TextEncryptor;

public class RsaEncryptTest extends AndroidTestCase {

    public static final String TAG = "RsaEncryptTest";

    public RsaBytesEncryptor getBytesEncryptor() {
        RsaBytesEncryptor bytesEncryptor = new RsaBytesEncryptor();
        PublicKey pubKey = bytesEncryptor.getPubKey();
        PrivateKey privateKey = bytesEncryptor.getPrivateKey();
        Log.d(TAG, "PublicKey : " + pubKey);
        Log.d(TAG, "PrivateKey : " + privateKey);
        return bytesEncryptor;
    }

    public TextEncryptor getService() {
        RsaBytesEncryptor bytesEncryptor = getBytesEncryptor(); 
        Base64EncodingTextEncryptor textEncryptor = new Base64EncodingTextEncryptor(bytesEncryptor); 
        return textEncryptor;
    }

    // public void testByteEncrypt() throws Exception {
    // RsaBytesEncryptor bytesEncryptor = getBytesEncryptor();
    // // String msg = "Test message to encrypt";
    // String msgString =
    // "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque nunc nisl, varius commodo gravida id, tincidunt eget risus. Nunc interdum hendrerit laoreet. In et lacus ac velit luctus sollicitudin. ";
    // byte[] msg = Utf8.encode(msgString);
    // byte[] encrypted = bytesEncryptor.encrypt(msg);
    // Log.d(TAG, "RSA encryped Size : " + encrypted.length + " / for msg : " +
    // encrypted);
    // byte[] decrypted = bytesEncryptor.decrypt(encrypted);
    // Log.d(TAG, "RSA decrypted Size : " + decrypted.length + " / for msg : " +
    // decrypted);
    // assertEquals(msg, decrypted);
    //
    // }

    public void testEncrypt() throws Exception {
        TextEncryptor textEncyptor = getService();
        String msg = "LOC(d-mB9r,g3iZPk;9ROI;20,ak,c21,pg,b49)";
        // String msg =
        // "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Quisque nunc nisl, varius commodo gravida id, tincidunt eget risus. Nunc interdum hendrerit laoreet. In et lacus ac velit luctus sollicitudin. ";
        // String msg =
        // "Integer ornare dignissim sem ut interdum. In placerat, lacus in malesuada semper, nisl ante rhoncus mi, ut mattis elit leo vitae orci. Proin tristique euismod ornare. Donec tincidunt, elit eget accumsan convallis, elit massa porta magna, sed convallis quam nisi dignissim arcu. Sed non sapien risus. Cras sit amet faucibus quam. Donec pulvinar tellus vel erat consectetur imperdiet. In vitae nulla non ante ornare aliquam at eu ipsum. Duis auctor auctor gravida. Aliquam erat volutpat. Phasellus dapibus dapibus elit, bibendum tempus felis laoreet sit amet. Fusce consectetur euismod dui, ut malesuada eros volutpat sed. Nullam eu metus erat, tristique sollicitudin nulla.";
        String encrypted = textEncyptor.encrypt(msg);
        Log.d(TAG, "RSA encryped Size : " + encrypted.length() + " / for msg : " + encrypted);
        String decrypted = textEncyptor.decrypt(encrypted);
        Log.d(TAG, "RSA decrypted Size : " + decrypted.length() + " / for msg : " + decrypted);
        assertEquals(msg, decrypted);

    }

}
