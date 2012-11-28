package eu.ttbox.geoping.crypto.encrypt;

/**
 * Service interface for symmetric data encryption.
 * @author Keith Donald
 */
public interface BytesEncryptor {

    /**
     * Encrypt the byte array.
     */
    byte[] encrypt(byte[] byteArray);

    /**
     * Decrypt the byte array.
     */
    byte[] decrypt(byte[] encryptedByteArray);

}
