package eu.ttbox.geoping.service.encoder.helper;

import java.util.HashMap;

import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.PhoneNumberUtils;
import eu.ttbox.geoping.service.billing.util.Base64DecoderException;
import eu.ttbox.geoping.service.encoder.SmsMessageLocEnum;
import eu.ttbox.geoping.service.encoder.SmsMessageTypeEnum;
import eu.ttbox.geoping.service.encoder.params.IntegerEncoded;
import eu.ttbox.geoping.service.encoder.params.LongEncoded;

public class SmsParamEncoderHelper {

    public static final int NUMBER_ENCODER_RADIX = IntegerEncoded.MAX_RADIX;
    // public static final int NUMBER_ENCODER_RADIX = 36;

    public static final String TAG = "SmsParamEncoderHelper";

    public static final char FIELD_SEP = ',';
    public static final char FIELD_MULTIDATA_SEP = ';';

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
    // Tools
    // ===========================================================

    private static StringBuilder writeTo(StringBuilder sb, SmsMessageLocEnum field, String value) {
        sb.append(field.smsFieldName);
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

    private static StringBuilder writeToBase64String(StringBuilder sb, SmsMessageLocEnum field, String value) {
        String toEncodeVal = value;
        if (SmsMessageLocEnum.PHONE_NUMBER == field) {
            toEncodeVal = PhoneNumberUtils.normalizeNumber(toEncodeVal);
        }
        String preparePhoneNumber = eu.ttbox.geoping.service.billing.util.Base64.encodeWebSafe(toEncodeVal.getBytes(), false);
        return writeTo(sb, field, preparePhoneNumber);
    }

    private static String readToBase64String(SmsMessageLocEnum field, String value) {
        String encodeVal = readToString(field, value);
        if (encodeVal != null) {
            try {
                encodeVal = new String(eu.ttbox.geoping.service.billing.util.Base64.decodeWebSafe(encodeVal));
            } catch (Base64DecoderException e) {
                Log.e(TAG, "Base64DecoderException : " + e.getMessage(), e);
                encodeVal = null;
            }
        }
        return encodeVal;
    }

    private static StringBuilder writeTo(StringBuilder sb, SmsMessageLocEnum field, int value, int radix) {
        String valueString = IntegerEncoded.toString(value, radix);
        sb.append(field.smsFieldName).append(valueString);
        return sb;
    }

    private static StringBuilder writeTo(StringBuilder sb, SmsMessageLocEnum field, int[] values, int radix) {
        boolean isNotFirst = false;
        for (int value : values) {
            String valueEncoded = IntegerEncoded.toString(value, radix);
            if (isNotFirst) {
                sb.append(FIELD_MULTIDATA_SEP);
            } else {
                sb.append(field.smsFieldName);
                isNotFirst = true;
            }
            sb.append(valueEncoded);
        }
        return sb;
    }

    private static StringBuilder writeTo(StringBuilder sb, SmsMessageLocEnum field, long value, int radix) {
        String valueString = LongEncoded.toString(value, radix);
        sb.append(field.smsFieldName).append(valueString);
        return sb;
    }

    private static StringBuilder writeToDate(StringBuilder sb, SmsMessageLocEnum field, long value, int radix) {
        long dateValue = (value - AppConstants.DATE_ZERO) / 1000;
        String valueString = LongEncoded.toString(dateValue, radix);
        sb.append(field.smsFieldName).append(valueString);
        return sb;
    }

    private static int readToInt(SmsMessageLocEnum field, String value, int radix) {
        int result = IntegerEncoded.parseInt(value, radix);
        return result;
    }

    private static long readToLong(SmsMessageLocEnum field, String value, int radix) {
        Long result = LongEncoded.parseLong(value, radix);
        return result;
    }

    private static long readToDate(SmsMessageLocEnum field, String value, int radix) {
        long result = LongEncoded.parseLong(value, radix);
        long dateAsLong = (result * 1000) + AppConstants.DATE_ZERO;
        return dateAsLong;
    }

    public static boolean writeToMultiInt(StringBuilder sb, SmsMessageLocEnum field, Bundle values, String[] colDatas, int radix) {
        boolean isNotFirst = false;
        int colDataSize = colDatas.length;
        for (int i = 0; i < colDataSize; i++) {
            String colData = colDatas[i];
            if (values.containsKey(colData)) {
                // Encode
                int value = values.getInt(colData);
                String valueEncoded = IntegerEncoded.toString(value, radix);
                // Write
                if (isNotFirst) {
                    sb.append(FIELD_MULTIDATA_SEP);
                } else if (i == 0) {
                    sb.append(field.smsFieldName);
                    isNotFirst = true;
                } else if (!isNotFirst) {
                    // first is not Lat => Ignore alls
                    Log.w(TAG, String.format("Ignore Multis Datas : %s (because Not int good order os %s)", colData, colDatas));
                    return false;
                }
                sb.append(valueEncoded);
            }
        }
        return isNotFirst;
    }

    private static int readToMultiInt(Bundle extras, String value, String[] colDatas, int radix) {
        int start = 0;
        int colDataSize = colDatas.length;
        boolean isLast = false;
        for (int i = 0; i < colDataSize; i++) {
            String colData = colDatas[i];
            int idx = value.indexOf(FIELD_MULTIDATA_SEP, start);
            if (idx == -1) {
                idx = value.length();
                isLast = true;
            }
            if (idx != -1) {
                String s = value.substring(start, idx);
                // Log.d(TAG, String.format("Read Multi Field(%s) %s : %s", i,
                // colData, s));
                if (s != null && s.length() > 0) {
                    int unit = IntegerEncoded.parseInt(s, radix);
                    extras.putInt(colData, unit);
                }
                start = idx + 1;
            }
            if (isLast) {
                return i;
            }
        }
        return 0;
    }

    // ===========================================================
    // GeoTrack Decoder
    // ===========================================================

    public static Bundle decodeMessageAsMap(String encoded) {
        return decodeMessageAsMap(encoded, null);
    }

    public static Bundle decodeMessageAsMap(String encoded, int radix) {
        return decodeMessageAsMap(encoded, null, radix);
    }

    public static Bundle decodeMessageAsMap(String encoded, Bundle dest) {
        return decodeMessageAsMap(encoded, dest, NUMBER_ENCODER_RADIX);
    }

    // public static Bundle decodeMessageAsMapOld(String encoded, Bundle dest,
    // int radix) {
    // Bundle result = dest != null ? dest : new Bundle();
    // String[] splitMsg = encoded.split(String.valueOf(FIELD_SEP));
    // for (String field : splitMsg) {
    // char key = field.charAt(0);
    // SmsMessageLocEnum fieldEnum = SmsMessageLocEnum.getBySmsFieldName(key);
    // if (fieldEnum != null) {
    // String valueEncoded = field.substring(1, field.length());
    // switch (fieldEnum.type) {
    // case GPS_PROVIDER:
    // // Same as String
    // case STRING:
    // result.putString(fieldEnum.dbFieldName, readToString(fieldEnum,
    // valueEncoded));
    // break;
    // case INT:
    // result.putInt(fieldEnum.dbFieldName, readToInt(fieldEnum, valueEncoded,
    // radix));
    // break;
    // case DATE:
    // result.putLong(fieldEnum.dbFieldName, readToDate(fieldEnum, valueEncoded,
    // radix));
    // break;
    // case LONG:
    // result.putLong(fieldEnum.dbFieldName, readToLong(fieldEnum, valueEncoded,
    // radix));
    // break;
    // case MULTI:
    // readToMultiInt(result, valueEncoded, fieldEnum.multiFieldName, radix);
    // break;
    // default:
    // break;
    // }
    // } else {
    // Log.d(TAG, String.format("Not found convertion Field for key(%s) : %s",
    // key, field));
    // }
    // }
    // return result;
    // }

    public static Bundle decodeMessageAsMap(String encoded, Bundle dest, int radix) {
        Bundle result = dest != null ? dest : new Bundle();
        int encodedSize = encoded.length();
        int startIdx = 0;
        int sepIdx = 0;
        while ((sepIdx = encoded.indexOf(FIELD_SEP, startIdx)) > -1) {
            // Consume param
            readSmsMessageLocEnum(result, startIdx, sepIdx, encoded, radix);
            // Next Loop
            startIdx = sepIdx + 1;
        }
        // Last Loop
        sepIdx = encodedSize;
        readSmsMessageLocEnum(result, startIdx, sepIdx, encoded, radix);
        return result;
    }

    private static void readSmsMessageLocEnum(Bundle result, int startIdx, int sepIdx, String encoded, int radix) {
        char key = encoded.charAt(startIdx);
        SmsMessageLocEnum fieldEnum = SmsMessageLocEnum.getBySmsFieldName(key);
        if (fieldEnum != null) {
            String valueEncoded = encoded.substring(startIdx + 1, sepIdx);
            switch (fieldEnum.type) {
            case GPS_PROVIDER:
                // Same as String
            case STRING:
                result.putString(fieldEnum.dbFieldName, readToString(fieldEnum, valueEncoded));
                break;
            case STRING_BASE64:
                result.putString(fieldEnum.dbFieldName, readToBase64String(fieldEnum, valueEncoded));
                break;
            case INT:
                result.putInt(fieldEnum.dbFieldName, readToInt(fieldEnum, valueEncoded, radix));
                break;
            case DATE:
                result.putLong(fieldEnum.dbFieldName, readToDate(fieldEnum, valueEncoded, radix));
                break;
            case LONG:
                result.putLong(fieldEnum.dbFieldName, readToLong(fieldEnum, valueEncoded, radix));
                break;
            case MULTI:
                readToMultiInt(result, valueEncoded, fieldEnum.multiFieldName, radix);
                break;
            default:
                break;
            }
        }

    }

    // ===========================================================
    // GeoTrack Encoder
    // ===========================================================

    public static StringBuilder encodeMessage(Bundle extras, StringBuilder dest) {
        return encodeMessage(extras, dest, NUMBER_ENCODER_RADIX);
    }

    private static boolean addFieldSep(StringBuilder sb, boolean isNotFirst) {
        if (isNotFirst) {
            sb.append(FIELD_SEP);
        } else {
            return true;
        }
        return isNotFirst;
    }

    public static StringBuilder encodeMessage(Bundle extras, StringBuilder dest, int radix) {
        StringBuilder sb = dest != null ? dest : new StringBuilder(AppConstants.SMS_MAX_SIZE_7BITS);
        boolean isNotFirst = false;
        // Single Field
        for (String key : extras.keySet()) {
            // Check Null Values
            Object keyValue = extras.get(key);
            if (keyValue == null) {
                Log.w(TAG, "Ignore encode Key[" + key + "] : for Null Value");
                continue;
            }
            // Specific Field
            SmsMessageLocEnum fieldEnum = SmsMessageLocEnum.getByDbFieldName(key);
            if (fieldEnum != null) {
                // Add Separator
                isNotFirst = addFieldSep(sb, isNotFirst);
                // Add Field
                switch (fieldEnum.type) {
                case GPS_PROVIDER:
                case STRING:
                    writeTo(sb, fieldEnum, (String) keyValue);
                    break;
                case STRING_BASE64:
                    writeToBase64String(sb, fieldEnum, (String) keyValue);
                    break;
                case INT:
                    writeTo(sb, fieldEnum, extras.getInt(key), radix);
                    break;
                case DATE:
                    writeToDate(sb, fieldEnum, extras.getLong(key), radix);
                    break;
                case LONG:
                    writeTo(sb, fieldEnum, extras.getLong(key), radix);
                    break;
                case MULTI:
                    writeToMultiInt(sb, fieldEnum, extras, fieldEnum.multiFieldName, radix);
                    break;
                default:
                    break;
                }
            } else {
                if (!SmsMessageLocEnum.isIgnoreMultiField(key)) {
                    Log.w(TAG, String.format("Ignore encode field [%s] : No convertion found for value [%s]", key, extras.get(key)));
                }
            }

        }
        return sb;
    }
    // public static StringBuilder encodeMessage(GeoTrack geoTrack) {
    // return encodeMessage(geoTrack, null);
    // }
    //
    // public static StringBuilder encodeMessage(GeoTrack geoTrack,
    // StringBuilder dest) {
    // StringBuilder sb = dest != null ? dest : new
    // StringBuilder(AppConstants.SMS_MAX_SIZE);
    // // sb.append(MSG_BEGIN);
    // writeTo(sb, SmsMessageLocEnum.MSGKEY_PROVIDER, geoTrack.getProvider(),
    // false);
    // writeTo(sb, SmsMessageLocEnum.MSGKEY_TIME, geoTrack.getTime(), true);
    //
    // // Lat Lng
    // int latE6 = (int) (geoTrack.getLatitude() * AppConstants.E6);
    // int lngE6 = (int) (geoTrack.getLongitude() * AppConstants.E6);
    // writeTo(sb, SmsMessageLocEnum.MSGKEY_LATITUDE_E6, latE6, true);
    // writeTo(sb, SmsMessageLocEnum.MSGKEY_LONGITUDE_E6, lngE6, true);
    // writeTo(sb, SmsMessageLocEnum.MSGKEY_ACCURACY, (int)
    // geoTrack.getAccuracy(), true);
    //
    // // altitude
    // if (geoTrack.hasAltitude()) {
    // int alt = (int) geoTrack.getAltitude();
    // writeTo(sb, SmsMessageLocEnum.MSGKEY_ALTITUDE, alt, true);
    // }
    // if (geoTrack.hasBearing()) {
    // int bearing = (int) geoTrack.getBearing();
    // writeTo(sb, SmsMessageLocEnum.MSGKEY_BEARING, bearing, true);
    // }
    // if (geoTrack.hasSpeed()) {
    // int speed = (int) geoTrack.getSpeed();
    // writeTo(sb, SmsMessageLocEnum.MSGKEY_SPEAD, speed, true);
    // }
    // // sb.append(MSG_END);
    // return sb;
    // }

    // ===========================================================
    // Other
    // ===========================================================

}
