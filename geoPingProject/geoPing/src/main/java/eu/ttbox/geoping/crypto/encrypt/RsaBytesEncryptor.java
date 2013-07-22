package eu.ttbox.geoping.crypto.encrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

import eu.ttbox.geoping.crypto.codec.Base64;
import eu.ttbox.geoping.encoder.crypto.TextEncryptor;


// http://www.bouncycastle.org/java.html
public class RsaBytesEncryptor implements BytesEncryptor {

    public static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding"; // "RSA";
                                                                       // // //
                                                                       // "RSA/NONE/NoPadding";
                                                                       // //;

    private String algorithm = RSA_ALGORITHM;
    private String provider = "BC";

    private PublicKey publicKey;
    private PrivateKey privateKey;

    // ===========================================================
    // Constructors
    // ===========================================================

    public RsaBytesEncryptor() {
        this(RSA_ALGORITHM, RsaBytesEncryptor.generateKey(128, RSAKeyGenParameterSpec.F4));
    }

    public RsaBytesEncryptor(String algorithm, KeyPair keyPair) {
        this(algorithm, keyPair.getPublic(), keyPair.getPrivate());
    }

    public RsaBytesEncryptor(String algorithm, PublicKey pubKey, PrivateKey privateKey) {
        super();
        this.algorithm = algorithm;
        this.publicKey = pubKey;
        this.privateKey = privateKey;

    }

    // ===========================================================
    // Accessors
    // ===========================================================

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public String getPublicKeyAsString(TextEncryptor textEncryptor) {
        String result = null;
        if (publicKey!=null) {
            result = convertToString(publicKey, textEncryptor);
        }
        return result;
    }
    
    public void setPublicKey(PublicKey pubKey) {
        this.publicKey = pubKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public String getPrivateKeyAsString(TextEncryptor textEncryptor) {
        String result = null;
        if (privateKey!=null) {
            result = convertToString(privateKey, textEncryptor);
        }
        return result;
    }
    
    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    
    // ===========================================================
    // Encryptor
    // ===========================================================

    
    // http://nyal.developpez.com/tutoriel/java/bouncycastle/
    // 5.2. Chiffrement
    // par clé asymétrique (format CMS/PKCS#7)
    @Override
    public byte[] encrypt(byte[] byteArray) {
        Cipher cipher = CipherUtils.newCipher(algorithm, provider);
        CipherUtils.initCipher(cipher, Cipher.ENCRYPT_MODE, publicKey, null);

        int blockSize = cipher.getBlockSize();
        int outputSize = cipher.getOutputSize(byteArray.length);
        int leavedSize = byteArray.length % blockSize;
        int blocksSize = leavedSize != 0 ? byteArray.length / blockSize + 1 : byteArray.length / blockSize;
        byte[] raw = new byte[outputSize * blocksSize];
        int i = 0;
        while (byteArray.length - i * blockSize > 0) {
            if (byteArray.length - i * blockSize > blockSize)
                CipherUtils.doFinal(cipher, byteArray, i * blockSize, blockSize, raw, i * outputSize);
            else
                CipherUtils.doFinal(cipher, byteArray, i * blockSize, byteArray.length - i * blockSize, raw, i * outputSize);
            i++;
        }

        // byte[] cipherData = CipherUtils.doFinal(cipher, byteArray);
        return raw;
    }

    @Override
    public byte[] decrypt(byte[] encryptedByteArray) {
        Cipher cipher = CipherUtils.newCipher(algorithm, provider);
        CipherUtils.initCipher(cipher, Cipher.DECRYPT_MODE, privateKey, null);
        int blockSize = cipher.getBlockSize();
        ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
        int j = 0;
        try {
            while (encryptedByteArray.length - j * blockSize > 0) {
                bout.write(CipherUtils.doFinal(cipher, encryptedByteArray, j * blockSize, blockSize));
                j++;
            }
        } catch (IOException e) {
            throw new IllegalStateException("Unable to decrypt Cipher due to IO exception", e);
        }
        byte[] cipherData = bout.toByteArray();
        // byte[] cipherData = CipherUtils.doFinal(cipher, encryptedByteArray);
        return cipherData;
    }

    // ===========================================================
    // Statics
    // ===========================================================

    public static KeyPair generateKey(int keySize) {
        return generateKey(keySize, RSAKeyGenParameterSpec.F4);
    }

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
    public static KeyPair generateKey(int keySize, BigInteger publicExponent) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keySize, publicExponent);
            SecureRandom random = new SecureRandom();
            keyGen.initialize(spec, random);
            KeyPair keyPair = keyGen.genKeyPair();
            return keyPair;
            // } catch (NoSuchProviderException e) {
            // throw new
            // IllegalStateException("Not a valid encryption provider", e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unable to initialize due to invalid secret key", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Unable to initialize due to invalid decryption parameter spec", e);
        }
    }

    public static String convertToString(Key secretKey, TextEncryptor textEncryptor) { 
        String encodedAsString =  convertByte2String(secretKey.getEncoded());
        String encrypted = textEncryptor.encrypt(encodedAsString);
        return encrypted;
    }

    private static String convertByte2String(  byte[] encoded) {
        //      String encodedAsString = android.util.Base64.encodeToString(encoded, android.util.Base64.DEFAULT);
//        String encodedAsString =  new String(Hex.encode(encoded)); 
      String encodedAsString =  new String(Base64.encode(encoded)); 
        return encodedAsString;
    }

    private static  byte[]  convertString2Byte(String encoded) {
//        byte[] keyBytes = android.util.Base64.decode(encoded, android.util.Base64.DEFAULT);
//        byte[] keyBytes = Hex.decode(encoded);
        byte[] keyBytes = Base64.decode(encoded.getBytes());
        return keyBytes;
    }
    
     public static PublicKey convertToPublicKey(String encryptedKey, TextEncryptor textEncryptor) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            String decryptedKey = textEncryptor.decrypt(encryptedKey);
            byte[] pubBytes = convertString2Byte(decryptedKey);
            PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(pubBytes));
            return pubKey;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unable to initialize due to invalid secret key", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to initialize due to invalid decryption parameter spec", e);
        }
    }

    public static PrivateKey convertToPrivateKey(String encryptedKey, TextEncryptor textEncryptor) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            String decryptedKey = textEncryptor.decrypt(encryptedKey);
            byte[] privBytes =  convertString2Byte(decryptedKey);
            PrivateKey privKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
            return privKey;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unable to initialize due to invalid secret key", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("Unable to initialize due to invalid decryption parameter spec", e);
        } 
    }
    
    
}
