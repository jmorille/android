package eu.ttbox.geoping.domain.model;

import android.content.ContentValues;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;

public enum PairingAuthorizeTypeEnum {
    
    AUTHORIZE_REQUEST, //
    AUTHORIZE_ALWAYS, //
    AUTHORIZE_NEVER; //
    
    public int getCode() {
        return ordinal();
    }
    
    public static PairingAuthorizeTypeEnum getByCode(int code) {
        if (code<0) {
            return null;
        }
        return PairingAuthorizeTypeEnum.values()[code];
    }

    
    public ContentValues writeTo(ContentValues values) {
        ContentValues val = values != null ? values : new ContentValues();
        val.put(PairingColumns.COL_AUTHORIZE_TYPE, getCode());
        return val;
    }
    
}
