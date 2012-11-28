package eu.ttbox.geoping.crypto.keygen;


import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

/**
 * A KeyGenerator that uses {@link SecureRandom} to generate byte array-based keys.
 * <p>
 * No specific provider is used for the {@code SecureRandom}, so the platform default
 * will be used.
 *
 * @author Keith Donald
 */
final class SecureRandomBytesKeyGenerator implements BytesKeyGenerator {

    private final SecureRandom random;

    private final int keyLength;

    /**
     * Creates a secure random key generator using the defaults.
     */
    public SecureRandomBytesKeyGenerator() {
        this(DEFAULT_KEY_LENGTH);
    }

    /**
     * Creates a secure random key generator with a custom key length.
     */
    public SecureRandomBytesKeyGenerator(int keyLength) {
        this.random = new SecureRandom();
        this.keyLength = keyLength;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public byte[] generateKey() {
        byte[] bytes = new byte[keyLength];
        random.nextBytes(bytes);
        return bytes;
    }

    private static final int DEFAULT_KEY_LENGTH = 8;

}