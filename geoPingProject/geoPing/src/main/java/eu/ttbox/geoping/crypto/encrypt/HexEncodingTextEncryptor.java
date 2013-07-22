package eu.ttbox.geoping.crypto.encrypt;


import eu.ttbox.geoping.crypto.codec.Hex;
import eu.ttbox.geoping.crypto.codec.Utf8;
import eu.ttbox.geoping.encoder.crypto.TextEncryptor;

/**
 * Delegates to an {@link BytesEncryptor} to encrypt text strings.
 * Raw text strings are UTF-8 encoded before being passed to the encryptor.
 * Encrypted strings are returned hex-encoded.
 * @author Keith Donald
 */
public final class HexEncodingTextEncryptor implements TextEncryptor {

    public final BytesEncryptor encryptor;

    public HexEncodingTextEncryptor(BytesEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    public String encrypt(String text) {
        return new String(Hex.encode(encryptor.encrypt(Utf8.encode(text))));
    }

    public String decrypt(String encryptedText) {
        return Utf8.decode(encryptor.decrypt(Hex.decode(encryptedText)));
    }

}