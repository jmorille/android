package eu.ttbox.geoping.encoder.model;


import eu.ttbox.geoping.encoder.params.MessageParamField;

public enum MessageParamEnum {


    // Loc
    PROVIDER('p', MessageParamField.COL_PROVIDER), //

    DATE('d', MessageParamField.COL_TIME), //
    GEO_E6('g',     MessageParamField.COL_LATITUDE_E6, //
                    MessageParamField.COL_LONGITUDE_E6, //
                    MessageParamField.COL_ALTITUDE  //
            ), //
    ACCURACY('a', MessageParamField.COL_ACCURACY), //
    BEARING('b', MessageParamField.COL_BEARING), //
    SPEAD('c', MessageParamField.COL_SPEED), //
    BATTERY('w', MessageParamField.COL_BATTERY_LEVEL), //

    // Person
    TIME_IN_S('s', MessageParamField.TIME_IN_S), //
    PERSON_ID('u', MessageParamField.COL_PERSON_ID), //
    // Geo Fence
    GEOFENCE_NAME('e', MessageParamField.GEOFENCE_NAME), //
    GEOFENCE('f',   MessageParamField.GEOFENCE_LATITUDE_E6_, //
                    MessageParamField.GEOFENCE_LONGITUDE_E6, //
                    MessageParamField.GEOFENCE_RADIUS, //
                    MessageParamField.GEOFENCE_TRANSITION, //
                    MessageParamField.GEOFENCE_EXPIRATION_DATE  //
           ), //
    // Spy Event
    //    EVT_SIM_PHONE('i', ParamTypeEncoding.STRING, "EVT_SIM_PHONE"),
    PHONE_NUMBER('n', MessageParamField.PHONE_NUMBER), //
    EVT_DATE('t', MessageParamField.COL_EVT_TIME); //

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
    // Accessor
    // ===========================================================


    public static MessageParamEnum getByDbFieldName(String key) {
        //FIXME
        return null;
    }

    public static MessageParamEnum getBySmsFieldName(char key) {
        //FIXME
        return null;
    }
}
