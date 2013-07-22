package eu.ttbox.geoping.encoder.model;


import java.util.HashMap;

import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.params.IParamEncoder;
import eu.ttbox.geoping.encoder.params.MessageParamField;
import eu.ttbox.geoping.encoder.params.ParamTypeEncoding;

public enum MessageParamEnum implements IParamEncoder {


    // Loc
    PROVIDER('p', MessageParamField.LOC_PROVIDER), //

    DATE('d', MessageParamField.LOC_TIME), //
    GEO_E6('g', MessageParamField.LOC_LATITUDE_E6, //
            MessageParamField.LOC_LONGITUDE_E6, //
            MessageParamField.LOC_ALTITUDE  //
    ), //
    ACCURACY('a', MessageParamField.LOC_ACCURACY), //
    BEARING('b', MessageParamField.LOC_BEARING), //
    SPEAD('c', MessageParamField.LOC_SPEED), //
    BATTERY('w', MessageParamField.BATTERY_LEVEL), //

    // Person
    TIME_IN_S('s', MessageParamField.TIME_IN_S), //
    PERSON_ID('u', MessageParamField.COL_PERSON_ID), //
    // Geo Fence
    GEOFENCE_NAME('e', MessageParamField.GEOFENCE_NAME), //
    GEOFENCE('f', MessageParamField.GEOFENCE_LATITUDE_E6_, //
            MessageParamField.GEOFENCE_LONGITUDE_E6, //
            MessageParamField.GEOFENCE_RADIUS, //
            MessageParamField.GEOFENCE_TRANSITION, //
            MessageParamField.GEOFENCE_EXPIRATION_DATE  //
    ), //
    // Spy Event
    //    EVT_SIM_PHONE('i', ParamTypeEncoding.STRING, "EVT_SIM_PHONE"),
    PHONE_NUMBER('n', MessageParamField.PHONE_NUMBER), //
    EVT_DATE('t', MessageParamField.COL_EVT_TIME); //

    // ===========================================================
    // Conversion Init
    // ===========================================================
    static HashMap<Character, MessageParamEnum> bySmsFieldNames;
    static HashMap<String, MessageParamEnum> byDbFieldNames;
    static HashMap<String, MessageParamEnum> byEnumNames;
    static HashMap<String, MessageParamEnum> byIgnoreDbFieldName;

    static {
        MessageParamEnum[] values = MessageParamEnum.values();
        HashMap<Character, MessageParamEnum> fields = new HashMap<Character, MessageParamEnum>(values.length);
        HashMap<String, MessageParamEnum> dbColNames = new HashMap<String, MessageParamEnum>(values.length);
        HashMap<String, MessageParamEnum> enumNames = new HashMap<String, MessageParamEnum>(values.length);
        HashMap<String, MessageParamEnum> ignoreMultiFieldName = new HashMap<String, MessageParamEnum>();
        for (MessageParamEnum field : values) {
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
            if (ParamTypeEncoding.MULTI.equals(field.type.wantedWriteType)) {
                int multiFieldNameSize = field.multiFieldName.length;
                for (int i = 1; i < multiFieldNameSize; i++) {
                    MessageParamField ignoreMultiCol = field.multiFieldName[i];
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

    // Instance
    public final char smsFieldName;
    public final MessageParamField type;
    public final MessageParamField[] multiFieldName;

    // ===========================================================
    // Constructor
    // ===========================================================

    MessageParamEnum(char smsFieldName, MessageParamField... fields) {
        this.smsFieldName = smsFieldName;
        this.multiFieldName = fields;
        if (fields.length == 1) {
            type = fields[0];
        } else {
            type = MessageParamField.multiType(fields);
        }
    }

    // ===========================================================
    // Static Accessor
    // ===========================================================

    public boolean writeTo(EncoderAdapter src,  StringBuilder dest ) {
        return type.writeTo(src, dest, this.type, this.smsFieldName);
    }

    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamField field, char smsFieldName  ) {
        return type.writeTo(src, dest, field, smsFieldName);
    }

    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamField field, char smsFieldName, boolean isSmsFieldName ) {
        return type.writeTo(src, dest, field, smsFieldName, isSmsFieldName);
    }

    @Override
    public int readTo(DecoderAdapter dest, String encoded, MessageParamField field ){
        return readTo(dest, encoded, field);
    }



    // ===========================================================
    // Accessor
    // ===========================================================
    public static MessageParamEnum getByEnumName(String enumName) {
        return byEnumNames.get(enumName);
    }

    public static MessageParamEnum getByDbFieldName(String fieldName) {
        return byDbFieldNames.get(fieldName);
    }

    public static MessageParamEnum getBySmsFieldName(char smsFieldName) {
        return bySmsFieldNames.get(smsFieldName);
    }

    public static MessageParamEnum getByIgnoreDbFieldName(String fieldName) {
        return byIgnoreDbFieldName.get(fieldName);
    }

    public static boolean isIgnoreMultiField(String fieldName) {
        return byIgnoreDbFieldName.containsKey(fieldName);
    }


}
