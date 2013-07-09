package eu.ttbox.geoping.crypto.encrypt;

/**
 * Service interface for symmetric encryption of text strings.
 *
 * @author Keith Donald
 */
public interface TextEncryptor {

    /**
     * Encrypt the raw text string.
     */
    String encrypt(String text);

    /**
     * Decrypt the encrypted text string.
     */
    String decrypt(String encryptedText);

}
