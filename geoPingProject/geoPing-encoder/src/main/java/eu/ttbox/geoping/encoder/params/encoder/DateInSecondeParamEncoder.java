package eu.ttbox.geoping.encoder.params.encoder;


import java.util.Calendar;

import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.params.helper.LongEncoded;
import eu.ttbox.geoping.encoder.params.IParamEncoder;
import eu.ttbox.geoping.encoder.model.MessageParamType;

public class DateInSecondeParamEncoder implements IParamEncoder {

    public static final long DATE_ZERO = getZeroDate();

    private static long getZeroDate() {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, 2012);
        cal.set(Calendar.MONTH, Calendar.DECEMBER);
        cal.set(Calendar.DAY_OF_MONTH, 22);
        // Midnight
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long timeAtMidnight = cal.getTimeInMillis();
        return timeAtMidnight;
    }


    // ===========================================================
    //   Encoder - Decoder Accessor
    // ===========================================================

    @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName  ) {
        return writeTo(src, dest, field, smsFieldName, true);
    }

        @Override
    public boolean writeTo(EncoderAdapter src,  StringBuilder dest, MessageParamType field, char smsFieldName, boolean isSmsFieldName ) {
        boolean isWrite = false;
        Long value =  (Long) src.get(field.dbFieldName) ;
        if (value!= null) {
            long dateValue = (value - DATE_ZERO) / 1000;
            String valueString = LongEncoded.toString(dateValue, LongEncoded.MAX_RADIX);
            if (isSmsFieldName) {
                dest.append( smsFieldName);
            }
            dest.append(valueString);
            isWrite = true;
        }
        return isWrite;
    }



    private  long readToDate(MessageParamType field, String value, int radix) {
        long result = LongEncoded.parseLong(value, radix);
        long dateAsLong = (result * 1000) + DATE_ZERO;
        return dateAsLong;
    }



}
