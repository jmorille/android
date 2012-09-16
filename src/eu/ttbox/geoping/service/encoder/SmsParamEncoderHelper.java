package eu.ttbox.geoping.service.encoder;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.domain.GeoTrack;

public class SmsParamEncoderHelper {

    private static final int NUMBER_ENCODER_RADIX = 36;

    public static final String TAG = "SmsMessageEncoderHelper";

    public static final char FIELD_SEP = ',';
    public static final char MSG_BEGIN = '(';
    public static final char MSG_END = '(';

    // ===========================================================
    // Location Provider Encoder
    // ===========================================================

    private static HashMap<String, String> locProviderEncoder;
    private static HashMap<String, String> locProviderDecoder;

    private static String[][] locProviders = new String[][] { //
    { "gps", "g" }, //
            { "network", "n" }, //
            { "passive", "p" } //
    };

    static {
        HashMap<String, String> encoder = new HashMap<String, String>(3);
        HashMap<String, String> decoder = new HashMap<String, String>(3);

        for (String[] provider : locProviders) {
            encoder.put(provider[0], provider[1]);
            decoder.put(provider[1], provider[0]);
        }
        locProviderEncoder = encoder;
        locProviderDecoder = decoder;
    }

    private static String decodeToGpsProvider(String value) {
        String result = locProviderDecoder.get(value);
        return result == null ? value : result;
    }

    private static String encodeToGpsProvider(String value) {
        String result = locProviderEncoder.get(value);
        return result == null ? value : result;
    }

    // ===========================================================
    // Enum Map Ref
    // ===========================================================

    static HashMap<Character, SmsMessageLocEnum> byFieldNames = buildbyFieldNames();

    private static HashMap<Character, SmsMessageLocEnum> buildbyFieldNames() {
        SmsMessageLocEnum[] values = SmsMessageLocEnum.values();
        HashMap<Character, SmsMessageLocEnum> fields = new HashMap<Character, SmsMessageLocEnum>(values.length);
        for (SmsMessageLocEnum field : values) {
            char key = field.fieldName;
            if (fields.containsKey(key)) {
                throw new IllegalArgumentException(String.format("Duplicated Key %s", key));
            }
            fields.put(key, field);
        }
        return fields;
    }

    public static SmsMessageLocEnum getByFieldName(char fieldName) {
        return byFieldNames.get(fieldName);
    }

    // ===========================================================
    // Tools 
    // ===========================================================
    private static StringBuilder writeTo(StringBuilder sb, SmsMessageLocEnum field, String value) {
        return writeTo(sb, field, value, true);
    }

    private static StringBuilder writeTo(StringBuilder sb, SmsMessageLocEnum field, String value, boolean addSep) {
        if (addSep) {
            sb.append(FIELD_SEP);
        }
        sb.append(field.fieldName);
        if (SmsMessageTypeEnum.GPS_PROVIDER == field.type) {
            sb.append(encodeToGpsProvider(value));
        } else {
            sb.append(value);
        }
        return sb;
    }

    private static String readToString(SmsMessageLocEnum field, String value) {
        if (SmsMessageTypeEnum.GPS_PROVIDER == field.type) {
            return decodeToGpsProvider(value);
        } else {
            return value;
        }
    }

    private static StringBuilder writeTo(StringBuilder sb, SmsMessageLocEnum field, int value) {
        sb.append(FIELD_SEP);
        String valueString = Integer.toString(value, NUMBER_ENCODER_RADIX);
        sb.append(field.fieldName).append(valueString);
        return sb;

    }

    private static StringBuilder writeTo(StringBuilder sb, SmsMessageLocEnum field, long value) {
        sb.append(FIELD_SEP);
        String valueString = Long.toString(value, NUMBER_ENCODER_RADIX);
        sb.append(field.fieldName).append(valueString);
        return sb;
    }

    private static Integer readToInt(SmsMessageLocEnum field, String value) {
        Integer result = Integer.valueOf(value, NUMBER_ENCODER_RADIX);
        return result;
    }

    private static Long readToLong(SmsMessageLocEnum field, String value) {
        Long result = Long.valueOf(value, NUMBER_ENCODER_RADIX);
        return result;
    }

    // ===========================================================
    // GeoTrack Decoder
    // ===========================================================
    
    public static HashMap<String, Object> decodeMessageAsMap(String encoded ) {
        return decodeMessageAsMap(encoded, null);
    }
    
    public static HashMap<String, Object> decodeMessageAsMap(String encoded, HashMap<String, Object> dest) {
        HashMap<String, Object> result = dest != null ? dest : new HashMap<String, Object>();
        String[] splitMsg = encoded.split(String.valueOf(FIELD_SEP));
        for (String field : splitMsg) {
            char key = field.charAt(0);
            SmsMessageLocEnum fieldEnum = getByFieldName(key);
            if (fieldEnum != null) {
                String valueEncoded = field.substring(1, field.length());
                switch (fieldEnum.type) {
                case GPS_PROVIDER:
                case STRING:
                    result.put(fieldEnum.dbFieldName, readToString(fieldEnum, valueEncoded));
                    break;
                case INT:
                    result.put(fieldEnum.dbFieldName, readToInt(fieldEnum, valueEncoded));
                    break;
                case LONG:
                    result.put(fieldEnum.dbFieldName, readToLong(fieldEnum, valueEncoded));
                    break;
                default:
                    break;
                }
            } else {
                Log.d(TAG, String.format("Not found convertion Field ofr key(%s) : %s", key, field));
            }
        }
        return result;
    }

    // ===========================================================
    // GeoTrack Encoder
    // ===========================================================
    

    
    public static StringBuilder encodeMessage(GeoTrack geoTrack) {
        return encodeMessage(geoTrack, null);
    }

    public static StringBuilder encodeMessage(Bundle extras, StringBuilder dest) {
        StringBuilder sb = dest != null ? dest : new StringBuilder(AppConstants.SMS_MAX_SIZE);
        
    }
    public static StringBuilder encodeMessage(GeoTrack geoTrack, StringBuilder dest) {
        StringBuilder sb = dest != null ? dest : new StringBuilder(AppConstants.SMS_MAX_SIZE);
        // sb.append(MSG_BEGIN);
        writeTo(sb, SmsMessageLocEnum.MSGKEY_PROVIDER, geoTrack.getProvider(), false);
        writeTo(sb, SmsMessageLocEnum.MSGKEY_TIME, geoTrack.getTime());

        // Lat Lng
        int latE6 = (int) (geoTrack.getLatitude() * AppConstants.E6);
        int lngE6 = (int) (geoTrack.getLongitude() * AppConstants.E6);
        writeTo(sb, SmsMessageLocEnum.MSGKEY_LATITUDE_E6, latE6);
        writeTo(sb, SmsMessageLocEnum.MSGKEY_LONGITUDE_E6, lngE6);
        writeTo(sb, SmsMessageLocEnum.MSGKEY_ACCURACY, (int) geoTrack.getAccuracy());

        // altitude
        if (geoTrack.hasAltitude()) {
            int alt = (int) geoTrack.getAltitude();
            writeTo(sb, SmsMessageLocEnum.MSGKEY_ALTITUDE, alt);
        }
        if (geoTrack.hasBearing()) {
            int bearing = (int) geoTrack.getBearing();
            writeTo(sb, SmsMessageLocEnum.MSGKEY_BEARING, bearing);
        }
        if (geoTrack.hasSpeed()) {
            int speed = (int) geoTrack.getSpeed();
            writeTo(sb, SmsMessageLocEnum.MSGKEY_SPEAD, speed);
        }
        // sb.append(MSG_END);
        return sb;
    }

    // ===========================================================
    // Other
    // ===========================================================

}
