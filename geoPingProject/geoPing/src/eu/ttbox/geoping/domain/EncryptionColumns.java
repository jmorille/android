package eu.ttbox.geoping.domain;

public interface EncryptionColumns {

	// Encryptin Key
	public static final String COL_ENCRYPTION_PUBKEY = "ENCRYPT_PUBKEY";
	public static final String COL_ENCRYPTION_PRIVKEY = "ENCRYPT_PRIVKEY";
	// Remote
	public static final String COL_ENCRYPTION_REMOTE_PUBKEY = "ENCRYPT_REMOTE_PUBKEY";
	public static final String COL_ENCRYPTION_REMOTE_TIME = "ENCRYPT_REMOTE_TIME";
	public static final String COL_ENCRYPTION_REMOTE_WAY = "ENCRYPT_REMOTE_WAY";

	public static final String[] ALL_COLS_ENCRYPTION = new String[] { //
	COL_ENCRYPTION_PUBKEY, COL_ENCRYPTION_PRIVKEY //
			, COL_ENCRYPTION_REMOTE_PUBKEY, COL_ENCRYPTION_REMOTE_TIME, COL_ENCRYPTION_REMOTE_WAY //
	};
}
