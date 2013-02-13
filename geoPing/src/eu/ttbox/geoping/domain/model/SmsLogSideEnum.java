package eu.ttbox.geoping.domain.model;

import android.support.v4.util.SparseArrayCompat;

public enum SmsLogSideEnum {
    SLAVE(0), //
    MASTER(1);

    // ===========================================================
    // Constructor
    // ===========================================================

    
    SmsLogSideEnum(int dbCode) {
        this.dbCode = dbCode;
    }

    private final int dbCode;

    // ===========================================================
    // Static
    // ===========================================================

    private static final SparseArrayCompat<SmsLogSideEnum> byDbCodes;
    
    static { 
        // Init map
        byDbCodes = new  SparseArrayCompat<SmsLogSideEnum>(SmsLogSideEnum.values().length);
        for (SmsLogSideEnum value : SmsLogSideEnum.values()) {
            byDbCodes.put(value.getDbCode(), value);
        }
    }
    
    // ===========================================================
    // Accessors
    // ===========================================================


    public static SmsLogSideEnum getByDbCode(int code) {
        return byDbCodes.get(code);
    }
    
    public int getDbCode() {
        return dbCode;
    }

}
