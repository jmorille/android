package eu.ttbox.geoping.crypto.encrypt;


import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
 

/**
 * Static helper for working with the Cipher API.
 * @author Keith Donald
 */
class CipherUtils {

    /**
     * Generates a SecretKey.
     */
    public static SecretKey newSecretKey(String algorithm, String password) {
        return newSecretKey(algorithm, new PBEKeySpec(password.toCharArray()));
    }

    /**
     * Generates a SecretKey.
     */
    public static SecretKey newSecretKey(String algorithm, PBEKeySpec keySpec) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(algorithm);
            return factory.generateSecret(keySpec);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Not a valid encryption algorithm", e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Not a valid secret key", e);
        }
    }

    /**
     * Constructs a new Cipher.
     */
    public static Cipher newCipher(String algorithm) {
        try {
            return Cipher.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Not a valid encryption algorithm", e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException("Should not happen", e);
        }
    }
    

    /**
     * Constructs a new Cipher.
     */
    public static Cipher newCipher(String algorithm, String provider) {
        try {
            return Cipher.getInstance(algorithm, provider);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Not a valid encryption algorithm", e);
        } catch (NoSuchPaddingException e) {
            throw new IllegalStateException("Should not happen", e);
        } catch (NoSuchProviderException e) {
            throw new IllegalStateException("Not a valid encryption provider", e);
        } 
    }

    

    /**
     * Initializes the Cipher for use.
     */
    public static <T extends AlgorithmParameterSpec> T getParameterSpec(Cipher cipher, Class<T> parameterSpecClass) {
        try {
            return cipher.getParameters().getParameterSpec(parameterSpecClass);
        } catch (InvalidParameterSpecException e) {
            throw new IllegalArgumentException("Unable to access parameter", e);
        }
    }

    /**
     * Initializes the Cipher for use.
     */
    public static void initCipher(Cipher cipher, int mode, Key secretKey) {
        initCipher(cipher, mode, secretKey, null);
    }

    /**
     * Initializes the Cipher for use.
     */
    public static void initCipher(Cipher cipher, int mode, Key secretKey, byte[] salt, int iterationCount) {
        initCipher(cipher, mode, secretKey, new PBEParameterSpec(salt, iterationCount));
    }

    /**
     * Initializes the Cipher for use.
     */
    public static void initCipher(Cipher cipher, int mode, Key secretKey, AlgorithmParameterSpec parameterSpec) {
        try {
            if (parameterSpec != null) {
                cipher.init(mode, secretKey, parameterSpec);
            } else {
                cipher.init(mode, secretKey);
            }
        } catch (InvalidKeyException e) {
            throw new IllegalArgumentException("Unable to initialize due to invalid secret key", e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new IllegalStateException("Unable to initialize due to invalid decryption parameter spec", e);
        }
    }

    /**
     * Invokes the Cipher to perform encryption or decryption (depending on the initialized mode).
     */
    public static byte[] doFinal(Cipher cipher, byte[] input) {
        try {
            return cipher.doFinal(input);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException("Unable to invoke Cipher due to illegal block size", e);
        } catch (BadPaddingException e) {
            throw new IllegalStateException("Unable to invoke Cipher due to bad padding", e);
        }
    }

    /**
     * Invokes the Cipher to perform encryption or decryption (depending on the
     * initialized mode).
     */
    public static byte[] doFinal(Cipher cipher, byte[] input, int inputOffSet, int inputLen) {
        try {
            return cipher.doFinal(input, inputOffSet, inputLen);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException("Unable to invoke Cipher due to illegal block size", e);
        } catch (BadPaddingException e) {
            throw new IllegalStateException("Unable to invoke Cipher due to bad padding", e);
        }
    }

    /**
     * Invokes the Cipher to perform encryption or decryption (depending on the
     * initialized mode).
     */
    public static int doFinal(Cipher cipher, byte[] input, int inputOffSet, int inputLen, byte[] ouput, int outputOffSet) {
        try {
            return cipher.doFinal(input, inputOffSet, inputLen, ouput, outputOffSet);
        } catch (IllegalBlockSizeException e) {
            throw new IllegalStateException("Unable to invoke Cipher due to illegal block size", e);
        } catch (BadPaddingException e) {
            throw new IllegalStateException("Unable to invoke Cipher due to bad padding", e);
        } catch (ShortBufferException e) {
            throw new IllegalStateException("Short Buffer Exception", e);
        }
    }

    private CipherUtils() {
    }

}