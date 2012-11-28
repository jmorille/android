package eu.ttbox.geoping.crypto.encrypt;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.crypto.Cipher;

public class RsaBytesEncryptor implements BytesEncryptor {

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
			RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keySize,
					publicExponent);
			keyGen.initialize(spec);
			KeyPair keyPair = keyGen.genKeyPair();
			return keyPair;
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(
					"Unable to initialize due to invalid secret key", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalStateException(
					"Unable to initialize due to invalid decryption parameter spec",
					e);
		}

	}

	public static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding";

	private String algorithm = RSA_ALGORITHM;
	private PublicKey pubKey;
	private PrivateKey privateKey;

	public RsaBytesEncryptor() {
		this(RSA_ALGORITHM, RsaBytesEncryptor.generateKey(128,
				RSAKeyGenParameterSpec.F4));
	}

	public RsaBytesEncryptor(String algorithm, KeyPair keyPair) {
		this(algorithm, keyPair.getPublic(), keyPair.getPrivate());
	}

	public RsaBytesEncryptor(String algorithm, PublicKey pubKey,
			PrivateKey privateKey) {
		super();
		this.algorithm = algorithm;
		this.pubKey = pubKey;
		this.privateKey = privateKey;
	}

	@Override
	public byte[] encrypt(byte[] byteArray) {
		Cipher cipher = CipherUtils.newCipher(algorithm);
		initCipher(cipher, Cipher.ENCRYPT_MODE, pubKey, null);
		byte[] cipherData = CipherUtils.doFinal(cipher, byteArray);
		return cipherData;
	}

	@Override
	public byte[] decrypt(byte[] encryptedByteArray) {
		Cipher cipher = CipherUtils.newCipher(algorithm);
		initCipher(cipher, Cipher.DECRYPT_MODE, privateKey, null);
		return null;
	}

	public static void initCipher(Cipher cipher, int mode, Key secretKey,
			AlgorithmParameterSpec parameterSpec) {
		try {
			if (parameterSpec != null) {
				cipher.init(mode, secretKey, parameterSpec);
			} else {
				cipher.init(mode, secretKey);
			}
		} catch (InvalidKeyException e) {
			throw new IllegalArgumentException(
					"Unable to initialize due to invalid secret key", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalStateException(
					"Unable to initialize due to invalid decryption parameter spec",
					e);
		}
	}

}
