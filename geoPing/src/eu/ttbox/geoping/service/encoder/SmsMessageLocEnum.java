package eu.ttbox.geoping.service.encoder;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;

public enum SmsMessageLocEnum {

    // Loc
    PROVIDER('p', SmsMessageTypeEnum.GPS_PROVIDER, GeoTrackColumns.COL_PROVIDER), //
    DATE('d', SmsMessageTypeEnum.DATE, GeoTrackColumns.COL_TIME), //
    GEO_E6('g', SmsMessageTypeEnum.MULTI, GeoTrackColumns.COL_LATITUDE_E6, new String[] { GeoTrackColumns.COL_LATITUDE_E6, GeoTrackColumns.COL_LONGITUDE_E6, GeoTrackColumns.COL_ALTITUDE }), //
    ACCURACY('a', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_ACCURACY), //
    BEARING('b', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_BEARING), //
    SPEAD('c', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_SPEED), //
    BATTERY('w', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_BATTERY_LEVEL), //

    // Person
    TIME_IN_S('s', SmsMessageTypeEnum.INT, "TIME_IN_S"), //
    PERSON_ID('u', SmsMessageTypeEnum.LONG, GeoTrackColumns.COL_PERSON_ID), //
    // Geo Fence
    GEOFENCE_NAME('f', SmsMessageTypeEnum.LONG, "GEOFENCE_NAME"), //

    // Spy Event
//    EVT_SIM_PHONE('i', SmsMessageTypeEnum.STRING, "EVT_SIM_PHONE"),
    PHONE_NUMBER('n', SmsMessageTypeEnum.STRING_BASE64, "PHONE_NUMBER"), //
    EVT_DATE('t', SmsMessageTypeEnum.DATE, GeoTrackColumns.COL_EVT_TIME); //
    // ===========================================================
    // Constructor
    // ===========================================================

    SmsMessageLocEnum(char fieldName, SmsMessageTypeEnum type, String dbFieldName) {
        this(fieldName, type, dbFieldName, null);
    }

    SmsMessageLocEnum(char fieldName, SmsMessageTypeEnum type, String dbFieldName, String[] multiFieldName) {
        this.smsFieldName = fieldName;
        this.type = type;
        this.dbFieldName = dbFieldName;
        this.multiFieldName = multiFieldName;
    }

    public final char smsFieldName;
    public final SmsMessageTypeEnum type;
    public final String dbFieldName;
    public final String[] multiFieldName;
    // ===========================================================
    // Conversion Init
    // ===========================================================

    static HashMap<Character, SmsMessageLocEnum> bySmsFieldNames;
    static HashMap<String, SmsMessageLocEnum> byDbFieldNames;
    static ArrayList<String> ignoreDbFieldName ;
    static {
        SmsMessageLocEnum[] values = SmsMessageLocEnum.values();
        HashMap<Character, SmsMessageLocEnum> fields = new HashMap<Character, SmsMessageLocEnum>(values.length);
        HashMap<String, SmsMessageLocEnum> dbColNames = new HashMap<String, SmsMessageLocEnum>(values.length);
        ArrayList<String>   ignoreMultiFieldName = new ArrayList<String>();
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
            // Multi Field
            if (SmsMessageTypeEnum.MULTI.equals(field.type)) {
                int multiFieldNameSize = field.multiFieldName.length;
                for (int i =1; i< multiFieldNameSize; i++) {
                    String ignoreMultiCol = field.multiFieldName[i];
                    if (ignoreMultiFieldName.contains(ignoreMultiCol)) {
                        throw new IllegalArgumentException(String.format("Duplicated Ignore Multifield : %s", ignoreMultiCol));
                    }
                    ignoreMultiFieldName.add(ignoreMultiCol);
                }
            }
        }
        // Affect
        bySmsFieldNames = fields;
        byDbFieldNames = dbColNames;
        ignoreDbFieldName = ignoreMultiFieldName;
    }

    // ===========================================================
    // Writer / Reader
    // ===========================================================
 
    public boolean isToBundle(Bundle extras ) {
        if (extras==null) {
            return false;
        }
        return extras.containsKey(dbFieldName);
    }
    public Bundle writeToBundle(Bundle extras, long value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putLong(dbFieldName, value);
        return params;
    }

    public Bundle writeToBundle(Bundle extras, int value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putInt(dbFieldName, value);
        return params;
    }

    public Bundle writeToBundle(Bundle extras, int[] value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putIntArray(dbFieldName, value);
        return params;
    }

    public Bundle writeToBundle(Bundle extras, String value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putString(dbFieldName, value);
        return params;
    }
     

    public long readLong(Bundle params, long defaultValue) {
        long result = defaultValue;
        if (params != null && params.containsKey(dbFieldName)) {
            result = params.getLong(dbFieldName, defaultValue);
        }
        return result;
    }

    public int readInt(Bundle params, int defaultValue) {
        int result = defaultValue;
        if (params != null && params.containsKey(dbFieldName)) {
            result = params.getInt(dbFieldName, defaultValue);
        }
        return result;
    }

    public String readString(Bundle params) {
        String result = null;
        if (params != null && params.containsKey(dbFieldName)) {
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

    public static boolean isIgnoreMultiField(String fieldName) {
       return ignoreDbFieldName.contains(fieldName);
    }
}
