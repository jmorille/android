package eu.ttbox.geoping.core.crypto;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;
import android.util.Log;

/**
 * {link http://exampledepot.com/egs/javax.crypto/DesString.html}
 * 
 * @author jmorille
 * 
 */
public class DesEncrypter {

    private static final String TAG = "DesEncrypter";

    Cipher ecipher;
    Cipher dcipher;

    public static SecretKey getAESKey() {
        byte[] keyBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        return key;
    }

    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        SecretKey key = KeyGenerator.getInstance("DES").generateKey();
        return key;
    }

    public DesEncrypter(SecretKey key) {
        try {
            ecipher = Cipher.getInstance("DES");
            dcipher = Cipher.getInstance("DES");
            ecipher.init(Cipher.ENCRYPT_MODE, key);
            dcipher.init(Cipher.DECRYPT_MODE, key);

        } catch (javax.crypto.NoSuchPaddingException e) {
        } catch (java.security.NoSuchAlgorithmException e) {
        } catch (java.security.InvalidKeyException e) {
        }
    }

    public String encrypt(String str) {
        try {
            // Encode the string into bytes using utf-8
            byte[] utf8 = str.getBytes("UTF8");
            // Encrypt
            byte[] enc = ecipher.doFinal(utf8);

            // Encode bytes to base64 to get a string
            // return new String(enc,"UTF8");
            return Base64.encodeToString(enc, Base64.NO_WRAP);
        } catch (javax.crypto.BadPaddingException e) {
            Log.e(TAG, "BadPaddingException : " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "IllegalBlockSizeException : " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException : " + e.getMessage());
        }
        return null;
    }

    public String decrypt(String str) {
        try {
            // Decode base64 to get bytes
            byte[] dec = Base64.decode(str, Base64.NO_WRAP);
            // byte[] dec =str.getBytes();

            // Decrypt
            byte[] utf8 = dcipher.doFinal(dec);

            // Decode using utf-8
            return new String(utf8, "UTF8");
        } catch (javax.crypto.BadPaddingException e) {
            Log.e(TAG, "BadPaddingException : " + e.getMessage());
        } catch (IllegalBlockSizeException e) {
            Log.e(TAG, "IllegalBlockSizeException : " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException : " + e.getMessage());
        }
        return null;
    }

}
