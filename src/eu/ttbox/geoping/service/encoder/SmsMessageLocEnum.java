package eu.ttbox.geoping.service.encoder;

import java.util.HashMap;

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
        this.smsFieldName = fieldName;
        this.type = type;
        this.dbFieldName = dbFieldName;
    }

    public final char smsFieldName;
    public final SmsMessageTypeEnum type;
    public final String dbFieldName;

    // ===========================================================
    // Conversion Sms Code
    // ===========================================================

    static HashMap<Character, SmsMessageLocEnum> bySmsFieldNames ;
    static HashMap<String, SmsMessageLocEnum> byDbFieldNames ;

      static {
        SmsMessageLocEnum[] values = SmsMessageLocEnum.values();
        HashMap<Character, SmsMessageLocEnum> fields = new HashMap<Character, SmsMessageLocEnum>(values.length);
        HashMap<String, SmsMessageLocEnum> dbColNames = new HashMap<String, SmsMessageLocEnum>(values.length);
        for (SmsMessageLocEnum field : values) {
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
        }
        // Affect
        bySmsFieldNames =fields;
     }
    

 
    // ===========================================================
    // Conversion Db Col Name
    // ===========================================================

      public static SmsMessageLocEnum getBySmsFieldName(char fieldName) {
          return bySmsFieldNames.get(fieldName);
      }

      public static SmsMessageLocEnum getByDbFieldName(String fieldName) {
          return byDbFieldNames.get(fieldName);
      }

    
}
