package eu.ttbox.geoping.domain;

public interface EncryptionColumns {

    // Encryptin Key
    public static final String COL_ENCRYPTION_PUBKEY = "ENCRYPTION_PUBKEY";
    public static final String COL_ENCRYPTION_PRIVKEY = "COL_ENCRYPTION_PRIVKEY";
    public static final String COL_ENCRYPTION_REMOTE_PUBKEY = "COL_ENCRYPTION_REMOTE_PUBKEY";
    
    public static final String[] ALL_COLS_ENCRYPTION = new String[] {COL_ENCRYPTION_PUBKEY, COL_ENCRYPTION_PRIVKEY, COL_ENCRYPTION_REMOTE_PUBKEY};
}
