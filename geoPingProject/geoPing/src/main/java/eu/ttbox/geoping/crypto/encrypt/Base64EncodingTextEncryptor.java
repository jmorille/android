package eu.ttbox.geoping.crypto.encrypt;


import eu.ttbox.geoping.crypto.codec.Base64;
import eu.ttbox.geoping.crypto.codec.Utf8;
import eu.ttbox.geoping.encoder.crypto.TextEncryptor;

/**
 * Delegates to an {@link BytesEncryptor} to encrypt text strings.
 * Raw text strings are UTF-8 encoded before being passed to the encryptor.
 * Encrypted strings are returned base64-encoded.
 * @author Keith Donald
 */
public final class Base64EncodingTextEncryptor implements TextEncryptor {

    public final BytesEncryptor encryptor;

    public Base64EncodingTextEncryptor(BytesEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public String encrypt(String text) {
        return new String(Base64.encode(encryptor.encrypt(Utf8.encode(text))));
    }

    public String decrypt(String encryptedText) {
        return Utf8.decode(encryptor.decrypt(Base64.decode(encryptedText.getBytes())));
    }

}