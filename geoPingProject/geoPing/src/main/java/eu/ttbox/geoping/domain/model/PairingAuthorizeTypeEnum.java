package eu.ttbox.geoping.domain.model;

import android.content.ContentValues;

import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;

public enum PairingAuthorizeTypeEnum {
    
    AUTHORIZE_REQUEST, //
    AUTHORIZE_ALWAYS, //
    AUTHORIZE_NEVER; //
    
    public static final PairingAuthorizeTypeEnum DEFAULT = PairingAuthorizeTypeEnum.AUTHORIZE_REQUEST;
    
    public int getCode() {
        return ordinal();
    }
    
    public static PairingAuthorizeTypeEnum getByCode(int code) {
        PairingAuthorizeTypeEnum[] valCodes =  PairingAuthorizeTypeEnum.values();
        if (code<0 || code>=valCodes.length) {
            return null;
        }
        return valCodes[code];
    }

    
    public ContentValues writeTo(ContentValues values) {
        ContentValues val = values != null ? values : new ContentValues();
        val.put(PairingColumns.COL_AUTHORIZE_TYPE, getCode());
        return val;
    }
    
}
