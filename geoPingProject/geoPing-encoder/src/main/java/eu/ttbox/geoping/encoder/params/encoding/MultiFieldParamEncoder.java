package eu.ttbox.geoping.encoder.params.encoding;

import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.params.IParamEncoder;
import eu.ttbox.geoping.encoder.params.MessageParamField;

public class MultiFieldParamEncoder implements IParamEncoder {

    public static final char MULTI_FIELD_SEP = ';';


    // ===========================================================
    //   Encoder Accessor
    // ===========================================================

    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamField field, char smsFieldName  ) {
        return writeTo(src, dest, field, smsFieldName, true);
    }


    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamField field, char smsFieldName, boolean isSmsFieldName ) {
        boolean isWrite = false;

        // Test first value is Present
        if (src.containsKey(field.dbFieldName)) {
            // Write Field Key
            if (isSmsFieldName) {
                dest.append(smsFieldName);
            }
            // Write Field Values
            MessageParamField[]  multiFields = field.multiFields;
            boolean isLastFieldWrite = true;
            int writeCount = 0;
            boolean isNotFirst = false;
            for (MessageParamField multiField : multiFields) {
               // System.out.println("Multi field write : " +  "  => multiField : " + multiField + " // " + isLastFieldWrite);
                // Write Sep if
                if (!isLastFieldWrite) {
                    dest.append(MULTI_FIELD_SEP);
                }
                // Write the field
                isLastFieldWrite = multiField.writeTo(src, dest, multiField, MULTI_FIELD_SEP, isNotFirst);
                isNotFirst = true;
                if (isLastFieldWrite) {
                    writeCount++;
                }
             }
            // Compute suppess of last seprator
            if (!isLastFieldWrite) {
                int lastIdx = dest.length()-1;
                while (dest.charAt(lastIdx)== MULTI_FIELD_SEP) {
                    dest.deleteCharAt(lastIdx);
                    lastIdx = dest.length()-1;
                }
            }
            // Compute Result
            isWrite = writeCount>0;
        }
        return isWrite;
    }



    // ===========================================================
    //   Decoder Accessor
    // ===========================================================


    public  int readTo(DecoderAdapter dest, String encoded, MessageParamField field ) {
        int start = 0;
        MessageParamField[] multiFields = field.multiFields;
        int colDataSize = multiFields.length;
        boolean isLast = false;
        for (int i = 0; i < colDataSize; i++) {
            MessageParamField multiField = multiFields[i];
            int idx = encoded.indexOf(MULTI_FIELD_SEP, start);
            if (idx == -1) {
                idx = encoded.length();
                isLast = true;
            }
            if (idx != -1) {
                String s = encoded.substring(start, idx);
                // Log.d(TAG, String.format("Read Multi Field(%s) %s : %s", i,
                // colData, s));
                if (s != null && s.length() > 0) {
                    multiField.readTo(dest, s, multiField);
                }
                start = idx + 1;
            }
            if (isLast) {
                return i;
            }
        }
        return 0;
    }

}