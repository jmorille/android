package eu.ttbox.geoping.crypto.keygen;

/**
 * A generator for unique byte array-based keys.
 * @author Keith Donald
 */
public interface BytesKeyGenerator {

    /**
     * Get the length, in bytes, of keys created by this generator.
     * Most unique keys are at least 8 bytes in length.
     */
    int getKeyLength();

    /**
     * Generate a new key.
     */
    byte[] generateKey();

}