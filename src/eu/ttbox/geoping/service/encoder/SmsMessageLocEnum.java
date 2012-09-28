package eu.ttbox.geoping.service.encoder;

import java.util.HashMap;

import android.os.Bundle;

import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;

public enum SmsMessageLocEnum {

	// Loc
    MSGKEY_PROVIDER('p', SmsMessageTypeEnum.GPS_PROVIDER, GeoTrackColumns.COL_PROVIDER), //
    MSGKEY_TIME('t', SmsMessageTypeEnum.LONG, GeoTrackColumns.COL_TIME), //
    MSGKEY_LATITUDE_E6('x', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_LATITUDE_E6), //
    MSGKEY_LONGITUDE_E6('y', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_LONGITUDE_E6), //
    MSGKEY_ALTITUDE('z', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_ALTITUDE), //
    MSGKEY_ACCURACY('a', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_ACCURACY), //
    MSGKEY_BEARING('b', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_BEARING), //
    MSGKEY_SPEAD('c', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_SPEED),//
    
    // Person
    MSGKEY_PERSON_ID('u', SmsMessageTypeEnum.LONG, GeoTrackColumns.COL_PERSON_ID); //

    // ===========================================================
    // Constructor
    // ===========================================================

     
    SmsMessageLocEnum(char fieldName, SmsMessageTypeEnum type, String dbFieldName) {
        this.smsFieldName = fieldName;
        this.type = type;
        this.dbFieldName = dbFieldName;
    }

    public final char smsFieldName;
    public final SmsMessageTypeEnum type;
    public final String dbFieldName;

    // ===========================================================
    // Conversion Init
    // ===========================================================

    static HashMap<Character, SmsMessageLocEnum> bySmsFieldNames;
    static HashMap<String, SmsMessageLocEnum> byDbFieldNames;

    static {
        SmsMessageLocEnum[] values = SmsMessageLocEnum.values();
        HashMap<Character, SmsMessageLocEnum> fields = new HashMap<Character, SmsMessageLocEnum>(values.length);
        HashMap<String, SmsMessageLocEnum> dbColNames = new HashMap<String, SmsMessageLocEnum>(values.length);
        for (SmsMessageLocEnum field : values) {
            // Sms Code
            char key = field.smsFieldName;
            if (fields.containsKey(key)) {
                throw new IllegalArgumentException(String.format("Duplicated Key %s", key));
            }
            fields.put(key, field);
            // Db Col name
            String colName = field.dbFieldName;
            if (dbColNames.containsKey(colName)) {
                throw new IllegalArgumentException(String.format("Duplicated DbColName %s", key));
            }
            dbColNames.put(colName, field);
        }
        // Affect
        bySmsFieldNames = fields;
        byDbFieldNames = dbColNames;
    }

    // ===========================================================
    // Writer / Reader
    // ===========================================================
	public Bundle writeToBundle(Bundle extras, long value) {
		Bundle params = extras==null ? new Bundle() : extras;
		params.putLong(dbFieldName, value);
		return params;
	}
	public Bundle writeToBundle(Bundle extras, int value) {
		Bundle params = extras==null ? new Bundle() : extras;
		params.putInt(dbFieldName, value);
		return params;
	}
	public Bundle writeToBundle(Bundle extras, String value) {
		Bundle params = extras==null ? new Bundle() : extras;
		params.putString(dbFieldName, value);
		return params;
	}

	public long readLong(Bundle params, long defaultValue) {
		long result  = defaultValue;
		if (params!=null && params.containsKey(dbFieldName)) {
			result = params.getLong(dbFieldName, defaultValue);
		}
		return result;
	}
	public int readInt(Bundle params, int defaultValue) {
		int result  = defaultValue;
		if (params!=null && params.containsKey(dbFieldName)) {
			result = params.getInt(dbFieldName, defaultValue);
		}
		return result;
	}
	
	public String readString(Bundle params ) {
		String result  = null;
		if (params!=null && params.containsKey(dbFieldName)) {
			result = params.getString(dbFieldName);
		}
		return result;
	}
    
    
    // ===========================================================
    // Conversion Accessor
    // ===========================================================

    public static SmsMessageLocEnum getBySmsFieldName(char fieldName) {
        return bySmsFieldNames.get(fieldName);
    }

    public static SmsMessageLocEnum getByDbFieldName(String fieldName) {
        return byDbFieldNames.get(fieldName);
    }



}
