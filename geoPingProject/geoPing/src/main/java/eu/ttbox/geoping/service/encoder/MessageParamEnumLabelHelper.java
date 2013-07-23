package eu.ttbox.geoping.service.encoder;


import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;

import java.util.HashMap;
import java.util.Locale;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.encoder.model.MessageParamEnum;

public class MessageParamEnumLabelHelper {

    private static final HashMap<MessageParamEnum, LabelHoder> byParam;


    // ===========================================================
    // Initializer
    // ===========================================================


    static {
        // Init Label
        LabelHoder[] holders = new LabelHoder[]{
                new LabelHolderSingleInt(MessageParamEnum.BATTERY, R.string.battery_percent)  //
                ,new LabelHolderDate(MessageParamEnum.EVT_DATE, R.string.geotrack_time_dateformat)  //
                ,new LabelHolderDate(MessageParamEnum.DATE, R.string.geotrack_time_dateformat)  //
                ,new LabelHolderLatLngE6(MessageParamEnum.GEO_E6, R.string.battery_percent)  //

        };
        // Construct Map
        HashMap<MessageParamEnum, LabelHoder> abyMessageActionEnum = new HashMap<MessageParamEnum, LabelHoder>(holders.length);
        for (LabelHoder holder : holders) {
            final MessageParamEnum key = holder.param;
            if (abyMessageActionEnum.containsKey(key)) {
                throw new IllegalArgumentException(String.format("Duplicated MessageParamEnumLabelHelper Map Key %s", key));
            }
            abyMessageActionEnum.put(key, holder);
        }
        byParam = abyMessageActionEnum;
    }

    // ===========================================================
    // Accessor
    // ===========================================================

    public static LabelHoder getLabelHolder(Context context, MessageParamEnum key) {
        LabelHoder labelHolder = byParam.get(key);
        return labelHolder;
    }

    public static String getString(Context context, MessageParamEnum key, Bundle extras){
        LabelHoder labelHolder = byParam.get(key);
        String result = null;
        if (labelHolder !=null) {
            result = labelHolder.getString(context, extras);
        }
        return result;
    }

    public static String getString(Context context, MessageParamEnum key, int  value){
        LabelHoder labelHolder = byParam.get(key);
        String result = null;
        if (labelHolder !=null) {
            result = labelHolder.getString(context, value);
        }
        return result;
    }


    public static String getDateAsString(Context context,  long dateInms){
        String result   = DateUtils.formatDateRange(context, dateInms, dateInms,
                    DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                            DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
        return result;
    }


    // ===========================================================
    // Label  Holder
    // ===========================================================


    public static abstract class LabelHoder {
        public final MessageParamEnum param;
        public final int labelResourceId;

        private LabelHoder(MessageParamEnum param, int labelResourceId) {
            this.param = param;
            this.labelResourceId = labelResourceId;
        }

        public String getString(Context context, Object... values) {
            String result = context.getString(labelResourceId, values);
            return result;
        }

        public abstract String getString(Context context, Bundle bundle);
    }

    // ===========================================================
    // Label  Holder : Single Int
    // ===========================================================


    private static class LabelHolderSingleInt extends LabelHoder {

        private LabelHolderSingleInt(MessageParamEnum param, int labelResourceId) {
            super(param, labelResourceId);
        }

        public String getString(Context context, Bundle bundle) {
            String result = null;
            final String key = param.type.dbFieldName;
            if (bundle.containsKey(key)) {
                final int value = bundle.getInt(key);
                result = context.getString(labelResourceId, value);
            }
            return result;
        }

    }

    // ===========================================================
    // Label  Holder : Date
    // ===========================================================

    private static class LabelHolderDate extends LabelHoder {

        private LabelHolderDate(MessageParamEnum param, int labelResourceId) {
            super(param, labelResourceId);
        }

        public String getString(Context context, Bundle bundle) {
            String result = null;
            final String key = param.type.dbFieldName;
            if (bundle.containsKey(key)) {
                final long value = bundle.getLong(key);
               // result = context.getString(labelResourceId, value);
                result =  DateUtils.formatDateRange(context, value, value,
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
            }
            return result;
        }

        @Override
        public String getString(Context context, Object... values) {
            String result = null;
            if (values !=null && values.length==1) {
                long value= (Long)values[0];
                result =  DateUtils.formatDateRange(context, value, value,
                        DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE |
                                DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_YEAR);
            }
            return result;
        }

    }

    // ===========================================================
    // Label  Holder : Lat Lng E6
    // ===========================================================

    private static class LabelHolderLatLngE6 extends LabelHoder {

        private LabelHolderLatLngE6(MessageParamEnum param, int labelResourceId) {
            super(param, labelResourceId);
        }

        public String getString(Context context, Bundle bundle) {
            String result = null;
            final String keyLat = param.multiFieldName[0].dbFieldName;
            final String keyLng = param.multiFieldName[1].dbFieldName;
            if (bundle.containsKey(keyLat) && bundle.containsKey(keyLng)) {
                final int latE6 = bundle.getInt(keyLat);
                final int lngE6 = bundle.getInt(keyLng);
                final double lat = latE6 / AppConstants.E6;
                final double lng = lngE6 / AppConstants.E6;
                result = String.format(Locale.US, "(%.6f, %.6f)", lat, lng);
               // result = context.getString(labelResourceId, lat, lng);
            }
            return result;
        }

    }


}
