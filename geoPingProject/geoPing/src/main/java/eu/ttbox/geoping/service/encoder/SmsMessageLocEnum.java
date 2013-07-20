package eu.ttbox.geoping.service.encoder;

import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase;
import eu.ttbox.geoping.service.encoder.params.SmsType;

/**
 * @deprecated Use MessageParamEnum
 */
public enum SmsMessageLocEnum {

    // Loc
    PROVIDER('p', type(GeoTrackColumns.COL_PROVIDER,  SmsMessageTypeEnum.GPS_PROVIDER)), //
    DATE('d', type(GeoTrackColumns.COL_TIME, SmsMessageTypeEnum.DATE)), //
    GEO_E6('g', type( GeoTrackColumns.COL_LATITUDE_E6, SmsMessageTypeEnum.MULTI), //
            new SmsType[] { //
                    type(GeoTrackColumns.COL_LATITUDE_E6, SmsMessageTypeEnum.INT), //
                    type(GeoTrackColumns.COL_LONGITUDE_E6, SmsMessageTypeEnum.INT), //
                    type(GeoTrackColumns.COL_ALTITUDE, SmsMessageTypeEnum.INT) //
            }), //
    ACCURACY('a', type( GeoTrackColumns.COL_ACCURACY,SmsMessageTypeEnum.INT)), //
    BEARING('b',  type( GeoTrackColumns.COL_BEARING,SmsMessageTypeEnum.INT)), //
    SPEAD('c',    type( GeoTrackColumns.COL_SPEED,SmsMessageTypeEnum.INT)), //
    BATTERY('w',  type( GeoTrackColumns.COL_BATTERY_LEVEL,SmsMessageTypeEnum.INT), R.string.battery_percent), //

    // Person
    TIME_IN_S('s', type( "TIME_IN_S", SmsMessageTypeEnum.INT)), //
    PERSON_ID('u', type( GeoTrackColumns.COL_PERSON_ID, SmsMessageTypeEnum.LONG)), //
    // Geo Fence
    GEOFENCE_NAME('e', type(  "GEOFENCE_NAME", SmsMessageTypeEnum.STRING) ), //
    GEOFENCE('f',      type( GeoFenceDatabase.GeoFenceColumns.COL_LATITUDE_E6,SmsMessageTypeEnum.MULTI) ,
            new SmsType[] { //
                    type(GeoFenceDatabase.GeoFenceColumns.COL_LATITUDE_E6, SmsMessageTypeEnum.INT), //
                    type(GeoFenceDatabase.GeoFenceColumns.COL_LONGITUDE_E6, SmsMessageTypeEnum.INT), //
                    type(GeoFenceDatabase.GeoFenceColumns.COL_RADIUS, SmsMessageTypeEnum.INT), //
                    type(GeoFenceDatabase.GeoFenceColumns.COL_TRANSITION, SmsMessageTypeEnum.INT), //
                    type(GeoFenceDatabase.GeoFenceColumns.COL_EXPIRATION_DATE, SmsMessageTypeEnum.DATE) //
            }), //

    // Spy Event
//    EVT_SIM_PHONE('i', SmsMessageTypeEnum.STRING, "EVT_SIM_PHONE"),
    PHONE_NUMBER('n', type( "PHONE_NUMBER", SmsMessageTypeEnum.STRING_BASE64)), //
    EVT_DATE('t',     type( GeoTrackColumns.COL_EVT_TIME, SmsMessageTypeEnum.DATE)); //

    // ===========================================================
    // Builder
    // ===========================================================

    private static SmsType type(String dbColumn, SmsMessageTypeEnum wantedWriteType) {
         return SmsType.multiType(dbColumn, wantedWriteType);
    }


    // ===========================================================
    // Constructor
    // ===========================================================
    SmsMessageLocEnum(char fieldName, SmsType type ) {
        this(fieldName, type,   null);
     }

    SmsMessageLocEnum(char fieldName, SmsType type,   int labelValueResourceId) {
        this(fieldName, type,   null, labelValueResourceId);
    }

    SmsMessageLocEnum(char fieldName, SmsType type , SmsType[] multiFieldName) {
        this(fieldName, type,  multiFieldName, Integer.MIN_VALUE);
    }
    SmsMessageLocEnum(char fieldName, SmsType type,   SmsType[] multiFieldName, int labelValueResourceId) {
        this.smsFieldName = fieldName;
        this.type = type;
        this.multiFieldName = multiFieldName;
        this.labelValueResourceId = labelValueResourceId;
    }

    public final char smsFieldName;
    public final SmsType type;
    public final SmsType[] multiFieldName;
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
            String colName = field.type.dbFieldName;
            if (dbColNames.containsKey(colName)) {
                throw new IllegalArgumentException(String.format("Duplicated DbColName %s", key));
            }
            dbColNames.put(colName, field);
            // Multi Field
            if (SmsMessageTypeEnum.MULTI.equals(field.type)) {
                int multiFieldNameSize = field.multiFieldName.length;
                for (int i =1; i< multiFieldNameSize; i++) {
                    SmsType ignoreMultiCol = field.multiFieldName[i];
                    if (ignoreMultiFieldName.containsKey(ignoreMultiCol)) {
                        throw new IllegalArgumentException(String.format("Duplicated Ignore Multifield : %s", ignoreMultiCol));
                    }
                    ignoreMultiFieldName.put(ignoreMultiCol.dbFieldName, field);
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
        return extras.containsKey(type.dbFieldName);
    }

    public String readString(ContentValues params) {
        String result = null;
        if (params != null && params.containsKey(type.dbFieldName)) {
            result = params.getAsString(type.dbFieldName);
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
        return extras.containsKey(type.dbFieldName);
    }
    public Bundle writeToBundle(Bundle extras, long value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putLong(type.dbFieldName, value);
        return params;
    }

    public Bundle writeToBundle(Bundle extras, int value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putInt(type.dbFieldName, value);
        return params;
    }

    public Bundle writeToBundle(Bundle extras, int[] value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putIntArray(type.dbFieldName, value);
        return params;
    }

    public Bundle writeToBundle(Bundle extras, String value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putString(type.dbFieldName, value);
        return params;
    }
     

    public long readLong(Bundle params, long defaultValue) {
        long result = defaultValue;
        if (params != null && params.containsKey(type.dbFieldName)) {
            result = params.getLong(type.dbFieldName, defaultValue);
        }
        return result;
    }

    public int readInt(Bundle params, int defaultValue) {
        int result = defaultValue;
        if (params != null && params.containsKey(type.dbFieldName)) {
            result = params.getInt(type.dbFieldName, defaultValue);
        }
        return result;
    }

    public String readString(Bundle params) {
        String result = null;
        if (params != null && params.containsKey(type.dbFieldName)) {
            result = params.getString(type.dbFieldName);
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
