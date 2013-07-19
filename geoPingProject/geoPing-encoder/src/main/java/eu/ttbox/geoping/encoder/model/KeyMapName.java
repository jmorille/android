package eu.ttbox.geoping.encoder.model;


public class KeyMapName {

    public static final MessageParamType COL_PROVIDER = type(  "PROVIDER",  MessageParamTypeEncodingEnum.GPS_PROVIDER);
    public static final MessageParamType COL_TIME = type(  "TIME", MessageParamTypeEncodingEnum.DATE);

    public static final MessageParamType COL_PERSON_ID = type(  "PERSON_ID", MessageParamTypeEncodingEnum.LONG);

    public static final MessageParamType COL_LATITUDE_E6 = type(  "LAT_E6", MessageParamTypeEncodingEnum.INT);
    public static final MessageParamType COL_LONGITUDE_E6 = type(  "LNG_E6", MessageParamTypeEncodingEnum.INT);
    public static final MessageParamType COL_ACCURACY = type(  "ACCURACY", MessageParamTypeEncodingEnum.INT);
    public static final MessageParamType COL_ALTITUDE = type(  "ALT", MessageParamTypeEncodingEnum.INT);
    public static final MessageParamType COL_BEARING = type(  "BEARING", MessageParamTypeEncodingEnum.INT);
    public static final MessageParamType COL_SPEED = type(  "SPEED", MessageParamTypeEncodingEnum.INT);

    public static final MessageParamType COL_BATTERY_LEVEL = type(  "BATTERY_LEVEL",MessageParamTypeEncodingEnum.INT);

    public static final MessageParamType GEOFENCE_LATITUDE_E6_ = type(  "GEOFENCE_LATITUDE_E6",MessageParamTypeEncodingEnum.INT);
    public static final MessageParamType GEOFENCE_LONGITUDE_E6 = type(  "GEOFENCE_LONGITUDE_E6",MessageParamTypeEncodingEnum.INT);

    public static final MessageParamType GEOFENCE_RADIUS = type(  "GEOFENCE_RADIUS",MessageParamTypeEncodingEnum.INT);
    public static final MessageParamType GEOFENCE_TRANSITION = type(  "GEOFENCE_TRANSITION", MessageParamTypeEncodingEnum.INT);
    public static final MessageParamType GEOFENCE_EXPIRATION_DATE = type(  "GEOFENCE_EXPIRATION_DATE", MessageParamTypeEncodingEnum.DATE);


    public static final MessageParamType COL_EVT_TIME = type(  "EVT_TIME";

    // ===========================================================
    // Builder
    // ===========================================================

    private static MessageParamType type(String dbColumn, MessageParamTypeEncodingEnum wantedWriteType) {
        return MessageParamType.multiType(dbColumn, wantedWriteType);
    }

    
}
