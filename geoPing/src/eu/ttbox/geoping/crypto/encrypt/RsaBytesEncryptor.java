package eu.ttbox.geoping.crypto.encrypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAKeyGenParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;

// http://www.bouncycastle.org/java.html
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
			RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keySize, publicExponent);
			keyGen.initialize(spec);
			KeyPair keyPair = keyGen.genKeyPair();
			return keyPair;
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalArgumentException("Unable to initialize due to invalid secret key", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new IllegalStateException("Unable to initialize due to invalid decryption parameter spec", e);
		}

	}

	public static final String RSA_ALGORITHM = "RSA/ECB/PKCS1Padding"; // "RSA";
																		// // //
																		// "RSA/NONE/NoPadding";
																		// //;

	private String algorithm = RSA_ALGORITHM;
	private String provider = "BC";

	private PublicKey pubKey;
	private PrivateKey privateKey;

	public RsaBytesEncryptor() {
		this(RSA_ALGORITHM, RsaBytesEncryptor.generateKey(128, RSAKeyGenParameterSpec.F4));
	}

	public RsaBytesEncryptor(String algorithm, KeyPair keyPair) {
		this(algorithm, keyPair.getPublic(), keyPair.getPrivate());
	}

	public RsaBytesEncryptor(String algorithm, PublicKey pubKey, PrivateKey privateKey) {
		super();
		this.algorithm = algorithm;
		this.pubKey = pubKey;
		this.privateKey = privateKey;

	}

	public PublicKey getPubKey() {
		return pubKey;
	}

	public void setPubKey(PublicKey pubKey) {
		this.pubKey = pubKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	// http://nyal.developpez.com/tutoriel/java/bouncycastle/
	// 5.2. Chiffrement
	// par clé asymétrique (format CMS/PKCS#7)
	@Override
	public byte[] encrypt(byte[] byteArray) {
		Cipher cipher = newCipher(algorithm, provider);
		CipherUtils.initCipher(cipher, Cipher.ENCRYPT_MODE, pubKey, null);

		int blockSize = cipher.getBlockSize();
		int outputSize = cipher.getOutputSize(byteArray.length);
		int leavedSize = byteArray.length % blockSize;
		int blocksSize = leavedSize != 0 ? byteArray.length / blockSize + 1 : byteArray.length / blockSize;
		byte[] raw = new byte[outputSize * blocksSize];
		int i = 0;
		while (byteArray.length - i * blockSize > 0) {
			if (byteArray.length - i * blockSize > blockSize)
				doFinal(cipher, byteArray, i * blockSize, blockSize, raw, i * outputSize);
			else
				doFinal(cipher, byteArray, i * blockSize, byteArray.length - i * blockSize, raw, i * outputSize);
			i++;
		}

		// byte[] cipherData = CipherUtils.doFinal(cipher, byteArray);
		return raw;
	}

	@Override
	public byte[] decrypt(byte[] encryptedByteArray) {
		Cipher cipher = newCipher(algorithm, provider);
		CipherUtils.initCipher(cipher, Cipher.DECRYPT_MODE, privateKey, null);
		int blockSize = cipher.getBlockSize();
		ByteArrayOutputStream bout = new ByteArrayOutputStream(64);
		int j = 0;
		try {
			while (encryptedByteArray.length - j * blockSize > 0) {
				bout.write(doFinal(cipher, encryptedByteArray, j * blockSize, blockSize));
				j++;
			}
		} catch (IOException e) {
			throw new IllegalStateException("Unable to decrypt Cipher due to IO exception", e);
		}
		byte[] cipherData = bout.toByteArray();
		// byte[] cipherData = CipherUtils.doFinal(cipher, encryptedByteArray);
		return cipherData;
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

}
