package eu.ttbox.geoping.service.encoder;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase;

public enum SmsMessageLocEnum {

    // Loc
    PROVIDER('p', SmsMessageTypeEnum.GPS_PROVIDER, GeoTrackColumns.COL_PROVIDER), //
    DATE('d', SmsMessageTypeEnum.DATE, GeoTrackColumns.COL_TIME), //
    GEO_E6('g', SmsMessageTypeEnum.MULTI, GeoTrackColumns.COL_LATITUDE_E6, new String[] { GeoTrackColumns.COL_LATITUDE_E6, GeoTrackColumns.COL_LONGITUDE_E6, GeoTrackColumns.COL_ALTITUDE }), //
    ACCURACY('a', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_ACCURACY), //
    BEARING('b', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_BEARING), //
    SPEAD('c', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_SPEED), //
    BATTERY('w', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_BATTERY_LEVEL, R.string.battery_percent), //

    // Person
    TIME_IN_S('s', SmsMessageTypeEnum.INT, "TIME_IN_S"), //
    PERSON_ID('u', SmsMessageTypeEnum.LONG, GeoTrackColumns.COL_PERSON_ID), //
    // Geo Fence
    GEOFENCE_NAME('e', SmsMessageTypeEnum.STRING, "GEOFENCE_NAME" ), //
    GEOFENCE('f', SmsMessageTypeEnum.MULTI, GeoFenceDatabase.GeoFenceColumns.COL_LATITUDE_E6 , new String[] { GeoFenceDatabase.GeoFenceColumns.COL_LATITUDE_E6, GeoFenceDatabase.GeoFenceColumns.COL_LONGITUDE_E6, GeoFenceDatabase.GeoFenceColumns.COL_RADIUS, GeoFenceDatabase.GeoFenceColumns.COL_TRANSITION, GeoFenceDatabase.GeoFenceColumns.COL_EXPIRATION_DATE  }), //

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

    SmsMessageLocEnum(char fieldName, SmsMessageTypeEnum type, String dbFieldName, int labelValueResourceId) {
        this(fieldName, type, dbFieldName, null, labelValueResourceId);
    }

    SmsMessageLocEnum(char fieldName, SmsMessageTypeEnum type, String dbFieldName, String[] multiFieldName) {
        this(fieldName, type, dbFieldName, multiFieldName, Integer.MIN_VALUE);
    }
    SmsMessageLocEnum(char fieldName, SmsMessageTypeEnum type, String dbFieldName, String[] multiFieldName, int labelValueResourceId) {
        this.smsFieldName = fieldName;
        this.type = type;
        this.dbFieldName = dbFieldName;
        this.multiFieldName = multiFieldName;
        this.labelValueResourceId = labelValueResourceId;
    }

    public final char smsFieldName;
    public final SmsMessageTypeEnum type;
    public final String dbFieldName;
    public final String[] multiFieldName;
    public final int labelValueResourceId;
    // ===========================================================
    // Conversion Init
    // ===========================================================

    static HashMap<Character, SmsMessageLocEnum> bySmsFieldNames;
    static HashMap<String, SmsMessageLocEnum> byDbFieldNames;
    static HashMap<String, SmsMessageLocEnum> byEnumNames;
    static  HashMap<String, SmsMessageLocEnum>  byIgnoreDbFieldName ;

    static {
        SmsMessageLocEnum[] values = SmsMessageLocEnum.values();
        HashMap<Character, SmsMessageLocEnum> fields = new HashMap<Character, SmsMessageLocEnum>(values.length);
        HashMap<String, SmsMessageLocEnum> dbColNames = new HashMap<String, SmsMessageLocEnum>(values.length);
        HashMap<String, SmsMessageLocEnum> enumNames = new HashMap<String, SmsMessageLocEnum>(values.length);
        HashMap<String, SmsMessageLocEnum>   ignoreMultiFieldName = new HashMap<String, SmsMessageLocEnum>();
        for (SmsMessageLocEnum field : values) {
            // Enum name
            enumNames.put(field.name(), field);
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
                    if (ignoreMultiFieldName.containsKey(ignoreMultiCol)) {
                        throw new IllegalArgumentException(String.format("Duplicated Ignore Multifield : %s", ignoreMultiCol));
                    }
                    ignoreMultiFieldName.put(ignoreMultiCol, field);
                }
            }
        }
        // Affect
        byEnumNames = enumNames;
        bySmsFieldNames = fields;
        byDbFieldNames = dbColNames;
        byIgnoreDbFieldName = ignoreMultiFieldName;
    }
    // ===========================================================
    // ContentValue Writer / Reader
    // ===========================================================

    public boolean isToContentValues(ContentValues extras ) {
        if (extras==null) {
            return false;
        }
        return extras.containsKey(dbFieldName);
    }

    public String readString(ContentValues params) {
        String result = null;
        if (params != null && params.containsKey(dbFieldName)) {
            result = params.getAsString(dbFieldName);
        }
        return result;
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
    // Label Ressource
    // ===========================================================

    public boolean hasLabelValueResourceId() {
        if (this.equals(EVT_DATE) || this.equals(DATE)) {
            return true;
        } else {
            return this.labelValueResourceId != Integer.MIN_VALUE;
        }
    }

    public String getLabelValueResourceId(Context context, long value) {
        if (this.equals(EVT_DATE) || this.equals(DATE)) {
            String smsTypeTime = DateUtils.formatDateRange(context, value, value,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                            DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
            return  smsTypeTime;
        } else   if (hasLabelValueResourceId()) {
            String labelValue =  context.getString( labelValueResourceId, value);
            return labelValue;
        } else {
            return String.valueOf(value);
        }
    }
    public String getLabelValueResourceId(Context context, String value) {
        if (this.equals(EVT_DATE) || this.equals(DATE)) {
            Long longValue = Long.valueOf(value);
            String smsTypeTime = DateUtils.formatDateRange(context, longValue, longValue,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                            DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
            return  value;
        } else   if (hasLabelValueResourceId()) {
            String labelValue =  context.getString( labelValueResourceId, value);
            return labelValue;
        } else {
            return value;
        }
    }

    // ===========================================================
    // Conversion Accessor
    // ===========================================================
    public static SmsMessageLocEnum getByEnumName(String fieldName) {
        return byEnumNames.get(fieldName);
    }

    public static SmsMessageLocEnum getBySmsFieldName(char fieldName) {
        return bySmsFieldNames.get(fieldName);
    }

    public static SmsMessageLocEnum getByDbFieldName(String fieldName) {
        return byDbFieldNames.get(fieldName);
    }

    public static SmsMessageLocEnum getByIgnoreDbFieldName(String fieldName) {
        return byIgnoreDbFieldName.get(fieldName);
    }

    public static boolean isIgnoreMultiField(String fieldName) {
       return byIgnoreDbFieldName.containsKey(fieldName);
    }


}
