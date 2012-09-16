package eu.ttbox.geoping.service.encoder;

import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;


public enum SmsMessageLocEnum {

    MSGKEY_PROVIDER('p', SmsMessageTypeEnum.GPS_PROVIDER, GeoTrackColumns.COL_PROVIDER), //
    MSGKEY_TIME('t', SmsMessageTypeEnum.LONG, GeoTrackColumns.COL_TIME), //
    MSGKEY_LATITUDE_E6('x', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_LATITUDE_E6), //
    MSGKEY_LONGITUDE_E6('y', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_LONGITUDE_E6), //
    MSGKEY_ALTITUDE('h', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_ALTITUDE), //
    MSGKEY_ACCURACY('a', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_ACCURACY), //
    MSGKEY_BEARING('b', SmsMessageTypeEnum.INT,   GeoTrackColumns.COL_BEARING), //
    MSGKEY_SPEAD('s', SmsMessageTypeEnum.INT, GeoTrackColumns.COL_SPEED);

    SmsMessageLocEnum(char fieldName, SmsMessageTypeEnum type, String dbFieldName) {
        this.fieldName = fieldName;
        this.type = type;
        this.dbFieldName = dbFieldName;
    }

    public final char fieldName;
    public final SmsMessageTypeEnum type;
    public final String dbFieldName;

}
