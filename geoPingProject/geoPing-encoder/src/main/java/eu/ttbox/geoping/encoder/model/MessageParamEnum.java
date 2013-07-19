package eu.ttbox.geoping.encoder.model;


public enum MessageParamEnum {


    // Loc
    PROVIDER('p', MessageParamType.COL_PROVIDER), //

    DATE('d', MessageParamType.COL_TIME), //
    GEO_E6('g',     MessageParamType.COL_LATITUDE_E6, //
                    MessageParamType.COL_LONGITUDE_E6, //
                    MessageParamType.COL_ALTITUDE  //
            ), //
    ACCURACY('a', MessageParamType.COL_ACCURACY), //
    BEARING('b', MessageParamType.COL_BEARING), //
    SPEAD('c', MessageParamType.COL_SPEED), //
    BATTERY('w', MessageParamType.COL_BATTERY_LEVEL), //

    // Person
    TIME_IN_S('s', MessageParamType.TIME_IN_S), //
    PERSON_ID('u', MessageParamType.COL_PERSON_ID), //
    // Geo Fence
    GEOFENCE_NAME('e', MessageParamType.GEOFENCE_NAME), //
    GEOFENCE('f',   MessageParamType.GEOFENCE_LATITUDE_E6_, //
                    MessageParamType.GEOFENCE_LONGITUDE_E6, //
                    MessageParamType.GEOFENCE_RADIUS, //
                    MessageParamType.GEOFENCE_TRANSITION, //
                    MessageParamType.GEOFENCE_EXPIRATION_DATE  //
           ), //
    // Spy Event
    //    EVT_SIM_PHONE('i', MessageParamTypeEncodingEnum.STRING, "EVT_SIM_PHONE"),
    PHONE_NUMBER('n', MessageParamType.PHONE_NUMBER), //
    EVT_DATE('t', MessageParamType.COL_EVT_TIME); //
    public final char smsFieldName;
    public final MessageParamType type;
    public final MessageParamType[] multiFieldName;

    // ===========================================================
    // Constructor
    // ===========================================================

    MessageParamEnum(char smsFieldName, MessageParamType... fields) {
        this.smsFieldName = smsFieldName;
        this.multiFieldName = fields;
        if (fields.length == 1) {
            type = fields[0];
        } else {
            type = MessageParamType.multiType(fields);
        }
    }

    // ===========================================================
    // Accessor
    // ===========================================================


    public static MessageParamEnum getByDbFieldName(String key) {
        return null;
    }
}
