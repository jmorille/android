package eu.ttbox.geoping.encoder.model;


public enum MessageParamEnum {


    // Loc
    PROVIDER('p',  KeyMapName.COL_PROVIDER ), //
    DATE('d',  KeyMapName.COL_TIME ), //
    GEO_E6('g', type( KeyMapName.COL_LATITUDE_E6.dbFieldName, MessageParamTypeEncodingEnum.MULTI), //
    new MessageParamType[] { //
         KeyMapName.COL_LATITUDE_E6 , //
         KeyMapName.COL_LONGITUDE_E6 , //
         KeyMapName.COL_ALTITUDE  //
    }), //
    ACCURACY('a', KeyMapName.COL_ACCURACY), //
    BEARING('b',    KeyMapName.COL_BEARING ), //
    SPEAD('c',     KeyMapName.COL_SPEED ), //
    BATTERY('w',   KeyMapName.COL_BATTERY_LEVEL), //

    // Person
    TIME_IN_S('s', type( "TIME_IN_S", MessageParamTypeEncodingEnum.INT)), //
    PERSON_ID('u',  KeyMapName.COL_PERSON_ID ), //
    // Geo Fence
    GEOFENCE_NAME('e', type(  "GEOFENCE_NAME", MessageParamTypeEncodingEnum.STRING) ), //
    GEOFENCE('f',      type( KeyMapName.GEOFENCE_LATITUDE_E6_.dbFieldName,MessageParamTypeEncodingEnum.MULTI) ,
    new MessageParamType[] { //
         KeyMapName.GEOFENCE_LATITUDE_E6_ , //
         KeyMapName.GEOFENCE_LONGITUDE_E6 , //
         KeyMapName.GEOFENCE_RADIUS , //
         KeyMapName.GEOFENCE_TRANSITION, //
         KeyMapName.GEOFENCE_EXPIRATION_DATE) //
    }), //

    // Spy Event
//    EVT_SIM_PHONE('i', MessageParamTypeEncodingEnum.STRING, "EVT_SIM_PHONE"),
    PHONE_NUMBER('n', type( "PHONE_NUMBER", MessageParamTypeEncodingEnum.STRING_BASE64)), //
    EVT_DATE('t',     type( KeyMapName.COL_EVT_TIME, MessageParamTypeEncodingEnum.DATE)); //

    // ===========================================================
    // Builder
    // ===========================================================

    private static MessageParamType type(String dbColumn, MessageParamTypeEncodingEnum wantedWriteType) {
        return MessageParamType.multiType(dbColumn, wantedWriteType);
    }


    // ===========================================================
    // Constructor
    // ===========================================================
    MessageParamEnum(char fieldName, MessageParamType type ) {
        this(fieldName, type,   null);
    }

    MessageParamEnum(char fieldName, MessageParamType type,   int labelValueResourceId) {
        this(fieldName, type,   null, labelValueResourceId);
    }

    MessageParamEnum(char fieldName, MessageParamType type , MessageParamType[] multiFieldName) {
        this(fieldName, type,  multiFieldName, Integer.MIN_VALUE);
    }
    MessageParamEnum(char fieldName, MessageParamType type,   MessageParamType[] multiFieldName, int labelValueResourceId) {
        this.smsFieldName = fieldName;
        this.type = type;
        this.multiFieldName = multiFieldName;
        this.labelValueResourceId = labelValueResourceId;
    }

    public final char smsFieldName;
    public final MessageParamType type;
    public final MessageParamType[] multiFieldName;
    public final int labelValueResourceId;

    
}
