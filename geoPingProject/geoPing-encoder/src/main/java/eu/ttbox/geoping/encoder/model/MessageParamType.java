package eu.ttbox.geoping.encoder.model;

import eu.ttbox.geoping.encoder.params.IParamEncoder;

public class MessageParamType {


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


    public static final MessageParamType COL_EVT_TIME = type(  "EVT_TIME", MessageParamTypeEncodingEnum.DATE);

    public static final MessageParamType TIME_IN_S = type( "TIME_IN_S", MessageParamTypeEncodingEnum.INT);

    public static final MessageParamType GEOFENCE_NAME = type(  "GEOFENCE_NAME", MessageParamTypeEncodingEnum.STRING);
    public static final MessageParamType  PHONE_NUMBER = type("PHONE_NUMBER",MessageParamTypeEncodingEnum.STRING_BASE64);


    // ===========================================================
    // Constructor
    // ===========================================================

    public final String dbFieldName;
    public final IParamEncoder wantedWriteType;
    public final MessageParamType[] multiFields;

    private MessageParamType(String dbColumn, IParamEncoder wantedWriteType,  MessageParamType[] multiFields) {
        this.dbFieldName = dbColumn;
        this.wantedWriteType = wantedWriteType;
        this.multiFields = multiFields;
    }


    // ===========================================================
    // Builder
    // ===========================================================


    public static MessageParamType type(String dbColumn, IParamEncoder wantedWriteType) {
        return new MessageParamType(dbColumn, wantedWriteType, null);
    }

    public static MessageParamType multiType(MessageParamType[] fields) {
        return new MessageParamType(fields[0].dbFieldName , MessageParamTypeEncodingEnum.MULTI, fields);
    }

}
