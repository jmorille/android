package eu.ttbox.geoping.encoder.params.encoder;

import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.model.MessageParamType;
import eu.ttbox.geoping.encoder.params.IParamEncoder;

public class MultiFieldParamEncoder implements IParamEncoder {

    public static final char MULTI_FIELD_SEP = ';';


    // ===========================================================
    //   Encoder Accessor
    // ===========================================================

    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName  ) {
        return writeTo(src, dest, field, smsFieldName, true);
    }


    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName, boolean isSmsFieldName ) {
        boolean isWrite = false;
        // Test first value is Present
        if (src.containsKey(field.dbFieldName)) {
            MessageParamType[]  multiFields = field.multiFields;
            boolean isLastFieldWrite = true;
            int writeCount = 0;
            for (MessageParamType multiField : multiFields) {
                // Write Sep if
                if (!isLastFieldWrite) {
                    dest.append(MULTI_FIELD_SEP);
                }
                // Write the field
                isLastFieldWrite = multiField.wantedWriteType.writeTo(src, dest, field, MULTI_FIELD_SEP);
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


    public  int readTo(DecoderAdapter dest, String value, MessageParamType field ) {
        int start = 0;
        MessageParamType[] multiFields = field.multiFields;
        int colDataSize = multiFields.length;
        boolean isLast = false;
        for (int i = 0; i < colDataSize; i++) {
            MessageParamType colData = multiFields[i];
            int idx = value.indexOf(MULTI_FIELD_SEP, start);
            if (idx == -1) {
                idx = value.length();
                isLast = true;
            }
            if (idx != -1) {
                String s = value.substring(start, idx);
                // Log.d(TAG, String.format("Read Multi Field(%s) %s : %s", i,
                // colData, s));
                if (s != null && s.length() > 0) {
                    int unit = colData.readTo(dest, s, colData);
                    extras.putInt(colData.dbFieldName, unit);
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