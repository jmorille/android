package eu.ttbox.geoping.core.crypto;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.crypto.Cipher;

/**
 * {link http://stackoverflow.com/questions/6069369/rsa-encryption-difference-
 * between-java-and-android}
 * 
 */
public class RsaEncrypter {

    
    /**
     * {link http://www.java2s.com/Code/Android/Security/
     * RSAencryptdecryptfunctionRSAECBPKCS1Padding.htm} generates RSA key pair
     * 
     * @param keySize
     * @param publicExponent
     *            public exponent value (can be RSAKeyGenParameterSpec.F0 or F4)
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidAlgorithmParameterException 
     */
    public static KeyPair generateKey(int keySize, BigInteger publicExponent) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keySize, publicExponent);
        keyGen.initialize(spec);
        KeyPair keyPair = keyGen.genKeyPair();
        return keyPair;

    }

    public static byte[] encrypt(PublicKey pubKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, pubKey);
        byte[] cipherData = cipher.doFinal(data);
        return cipherData;
    }

    public static byte[] decrypt(PrivateKey privateKey, byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // Cipher.getInstance("RSA/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] dectyptedText = cipher.doFinal(data);
        return dectyptedText;
    }

}
