package eu.ttbox.geoping.encoder.params;

import android.os.Bundle;

import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.model.MessageParamEnum;

import eu.ttbox.geoping.encoder.params.helper.IntegerEncoded;
import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.model.MessageParamType;

public class MessageParamEncoderHelper {

    public static final int NUMBER_ENCODER_RADIX = IntegerEncoded.MAX_RADIX;

    public static final char FIELD_SEP = ',';
    public static final char FIELD_MULTIDATA_SEP = ';';

    public static final int SMS_MAX_SIZE_7BITS = 160;
    public static final int SMS_MAX_SIZE_8BITS = 140;
    public static final int SMS_MAX_BITS_ARRAY_SIZE =  1120;


    // ===========================================================
    //   Decoder
    // ===========================================================


    // ===========================================================
    //   Encoder
    // ===========================================================

    public static StringBuilder encodeMessage(EncoderAdapter extras, StringBuilder dest) {
        return encodeMessage(extras, dest, NUMBER_ENCODER_RADIX);
    }


    public static StringBuilder encodeMessage(EncoderAdapter extras, StringBuilder dest, int radix) {
        StringBuilder sb = dest != null ? dest : new StringBuilder( SMS_MAX_SIZE_7BITS);
        boolean isNotFirst = false;
        // Single Field
        for (String key : extras.keySet()) {
            // Check Null Values
            Object keyValue = extras.get(key);
            if (keyValue == null) {
               // Log.w(TAG, "Ignore encode Key[" + key + "] : for Null Value");
                continue;
            }
            // Specific Field
            MessageParamEnum fieldEnum = MessageParamEnum.getByDbFieldName(key);
            if (fieldEnum != null) {
                MessageParamType type = fieldEnum.type;
                IParamEncoder typeEncoding = null; //(IParamEncoder)type.wantedWriteType ;
              //  typeEncoding.writeTo(extras, dest, type, keyValue);

            }
        }
        return sb;
    }

    // ===========================================================
    //   Decoder
    // ===========================================================

    public static Bundle decodeMessageAsMap(String encoded, DecoderAdapter dest, int radix) {
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


    private static void readSmsMessageLocEnum(DecoderAdapter result, int startIdx, int sepIdx, String encoded, int radix) {
        char key = encoded.charAt(startIdx);
        SmsMessageLocEnum fieldEnum = SmsMessageLocEnum.getBySmsFieldName(key);
        if (fieldEnum != null) {
            String valueEncoded = encoded.substring(startIdx + 1, sepIdx);
            MessageParamType smsType = fieldEnum.type;
            switch (smsType.wantedWriteType) {
                case GPS_PROVIDER:
                    // Same as String
                case STRING:
                    result.putString(smsType.dbFieldName, readToString(smsType, valueEncoded));
                    break;
                case STRING_BASE64:
                    result.putString(smsType.dbFieldName, readToBase64String(smsType, valueEncoded));
                    break;
                case INT:
                    result.putInt(smsType.dbFieldName, readToInt(fieldEnum, valueEncoded, radix));
                    break;
                case DATE:
                    result.putLong(smsType.dbFieldName, readToDate(fieldEnum, valueEncoded, radix));
                    break;
                case LONG:
                    result.putLong(smsType.dbFieldName, readToLong(fieldEnum, valueEncoded, radix));
                    break;
                case MULTI:
                    readToMultiInt(result, valueEncoded, fieldEnum.multiFieldName, radix);
                    break;
                default:
                    break;
            }
        }

    }

}
