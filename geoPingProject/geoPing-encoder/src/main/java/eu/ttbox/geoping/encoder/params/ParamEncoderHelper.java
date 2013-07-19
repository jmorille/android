package eu.ttbox.geoping.encoder.params;

import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.model.MessageParamEnum;
import eu.ttbox.geoping.encoder.params.helper.IntegerEncoded;

public class ParamEncoderHelper {

    public static final int NUMBER_ENCODER_RADIX = IntegerEncoded.MAX_RADIX;

    public static final char FIELD_SEP = ',';
    public static final char FIELD_MULTIDATA_SEP = ';';

    public static final int SMS_MAX_SIZE_7BITS = 160;
    public static final int SMS_MAX_SIZE_8BITS = 140;
    public static final int SMS_MAX_BITS_ARRAY_SIZE =  1120;


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
                MessageParamField type = fieldEnum.type;
                IParamEncoder typeEncoding = null; //(IParamEncoder)type.wantedWriteType ;
              //  typeEncoding.writeTo(extras, dest, type, keyValue);

            }
        }
        return sb;
    }

    // ===========================================================
    //   Decoder
    // ===========================================================

    public static DecoderAdapter decodeMessageAsMap(DecoderAdapter dest, String encoded ) {
        int encodedSize = encoded.length();
        int startIdx = 0;
        int sepIdx = 0;
        while ((sepIdx = encoded.indexOf(FIELD_SEP, startIdx)) > -1) {
            // Consume param
            readSmsMessageLocEnum(dest, startIdx, sepIdx, encoded );
            // Next Loop
            startIdx = sepIdx + 1;
        }
        // Last Loop
        sepIdx = encodedSize;
        readSmsMessageLocEnum(dest, startIdx, sepIdx, encoded );
        return dest;
    }


    private static void readSmsMessageLocEnum(DecoderAdapter result, int startIdx, int sepIdx, String encoded ) {
        char key = encoded.charAt(startIdx);
        MessageParamEnum fieldEnum = MessageParamEnum.getBySmsFieldName(key);
        if (fieldEnum != null) {
            String valueEncoded = encoded.substring(startIdx + 1, sepIdx);
            MessageParamField field = fieldEnum.type;
            field.wantedWriteType.readTo(result, valueEncoded, field);
        }

    }

}
