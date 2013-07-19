package eu.ttbox.geoping.encoder.params;

import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;

public class MessageParamField implements IParamEncoder {


    public static final MessageParamField COL_PROVIDER = type(  "PROVIDER",  ParamTypeEncoding.GPS_PROVIDER);
    public static final MessageParamField COL_TIME = type(  "TIME", ParamTypeEncoding.DATE);

    public static final MessageParamField COL_PERSON_ID = type(  "PERSON_ID", ParamTypeEncoding.LONG);

    public static final MessageParamField COL_LATITUDE_E6 = type(  "LAT_E6", ParamTypeEncoding.INT);
    public static final MessageParamField COL_LONGITUDE_E6 = type(  "LNG_E6", ParamTypeEncoding.INT);
    public static final MessageParamField COL_ACCURACY = type(  "ACCURACY", ParamTypeEncoding.INT);
    public static final MessageParamField COL_ALTITUDE = type(  "ALT", ParamTypeEncoding.INT);
    public static final MessageParamField COL_BEARING = type(  "BEARING", ParamTypeEncoding.INT);
    public static final MessageParamField COL_SPEED = type(  "SPEED", ParamTypeEncoding.INT);

    public static final MessageParamField COL_BATTERY_LEVEL = type(  "BATTERY_LEVEL", ParamTypeEncoding.INT);

    public static final MessageParamField GEOFENCE_LATITUDE_E6_ = type(  "GEOFENCE_LATITUDE_E6", ParamTypeEncoding.INT);
    public static final MessageParamField GEOFENCE_LONGITUDE_E6 = type(  "GEOFENCE_LONGITUDE_E6", ParamTypeEncoding.INT);

    public static final MessageParamField GEOFENCE_RADIUS = type(  "GEOFENCE_RADIUS", ParamTypeEncoding.INT);
    public static final MessageParamField GEOFENCE_TRANSITION = type(  "GEOFENCE_TRANSITION", ParamTypeEncoding.INT);
    public static final MessageParamField GEOFENCE_EXPIRATION_DATE = type(  "GEOFENCE_EXPIRATION_DATE", ParamTypeEncoding.DATE);


    public static final MessageParamField COL_EVT_TIME = type(  "EVT_TIME", ParamTypeEncoding.DATE);

    public static final MessageParamField TIME_IN_S = type( "TIME_IN_S", ParamTypeEncoding.INT);

    public static final MessageParamField GEOFENCE_NAME = type(  "GEOFENCE_NAME", ParamTypeEncoding.STRING);
    public static final MessageParamField PHONE_NUMBER = type("PHONE_NUMBER", ParamTypeEncoding.STRING_BASE64);


    // ===========================================================
    // Constructor
    // ===========================================================

    public final String dbFieldName;
    public final IParamEncoder wantedWriteType;
    public final MessageParamField[] multiFields;

    private MessageParamField(String dbColumn, IParamEncoder wantedWriteType, MessageParamField[] multiFields) {
        this.dbFieldName = dbColumn;
        this.wantedWriteType = wantedWriteType;
        this.multiFields = multiFields;
    }


    // ===========================================================
    // Builder
    // ===========================================================


    public static MessageParamField type(String dbColumn, IParamEncoder wantedWriteType) {
        return new MessageParamField(dbColumn, wantedWriteType, null);
    }

    public static MessageParamField multiType(MessageParamField[] fields) {
        return new MessageParamField(fields[0].dbFieldName , ParamTypeEncoding.MULTI, fields);
    }

      // ===========================================================
    //   Encoder -Decoder Accessor
    // ===========================================================

    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamField field, char smsFieldName  ) {
        return wantedWriteType.writeTo(src, dest, field, smsFieldName);
    }


    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamField field, char smsFieldName, boolean isSmsFieldName ) {
        return wantedWriteType.writeTo(src, dest, field, smsFieldName, isSmsFieldName );
    }

    @Override
    public int readTo(DecoderAdapter dest, String encoded, MessageParamField field ) {
        return wantedWriteType.readTo(  dest,   encoded,   field );
    }


    // ===========================================================
    //  Other
    // ===========================================================

}
