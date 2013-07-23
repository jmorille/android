package eu.ttbox.geoping.domain.smslog;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import eu.ttbox.geoping.domain.model.SmsLog;
import eu.ttbox.geoping.domain.model.SmsLogSideEnum;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;
import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.encoder.model.MessageParamEnum;
import eu.ttbox.geoping.encoder.params.ParamEncoderHelper;
import eu.ttbox.geoping.service.encoder.adpater.BundleEncoderAdapter;

public class SmsLogHelper {

    public static final String TAG = "SmsLogHelper";
    public boolean isNotInit = true;
    public int idIdx = -1;
    public int timeIdx = -1;
    public int actionIdx = -1;
    public int messageIdx = -1;
    public int messageParamIdx = -1;
    public int phoneIdx = -1;
    public int phoneMinMatchIdx = -1;
    public int smsLogTypeIdx = -1;
    public int smsLogSideIdx = -1;
    public int requestIdIdx = -1;
    public int sendAckTimeInMsIdx = -1;
    public int sendDeliveryAckTimeInMsIdx = -1;

    public static ContentValues getContentValues(SmsLog vo) {
        ContentValues initialValues = new ContentValues();
        if (vo.id > -1) {
            initialValues.put(SmsLogColumns.COL_ID, Long.valueOf(vo.id));
        }
        initialValues.put(SmsLogColumns.COL_TIME, vo.time);
        initialValues.put(SmsLogColumns.COL_PHONE, vo.phone);
        initialValues.put(SmsLogColumns.COL_ACTION, vo.action.getDbCode());
        initialValues.put(SmsLogColumns.COL_MESSAGE, vo.message);
        initialValues.put(SmsLogColumns.COL_MESSAGE_PARAMS, vo.messageParams);
        initialValues.put(SmsLogColumns.COL_SMSLOG_TYPE, vo.smsLogType.getCode());
        initialValues.put(SmsLogColumns.COL_SMS_SIDE, vo.side.getDbCode());
        initialValues.put(SmsLogColumns.COL_REQUEST_ID, vo.requestId);

        return initialValues;
    }

    public static ContentValues getContentValues(SmsLogSideEnum side, SmsLogTypeEnum type, BundleEncoderAdapter geoMessage) {
        return getContentValues(side, type, geoMessage.getPhone(), geoMessage.getAction(), geoMessage.getMap(), null);
    }

    /**
     * Used for logging sms Message in db
     */
    public static ContentValues getContentValues(SmsLogSideEnum side, SmsLogTypeEnum type, String phone, MessageActionEnum action, Bundle params, String messageResult) {
        ContentValues values = new ContentValues();
        values.put(SmsLogColumns.COL_TIME, System.currentTimeMillis());
        values.put(SmsLogColumns.COL_PHONE, phone);
        values.put(SmsLogColumns.COL_ACTION, action.getDbCode());
        values.put(SmsLogColumns.COL_SMSLOG_TYPE, type.getCode());
        values.put(SmsLogColumns.COL_SMS_SIDE, side.getDbCode());
        values.put(SmsLogColumns.COL_MESSAGE, messageResult);

        if (params != null && !params.isEmpty()) {
            // Test Values SMS Log values
            if (params.containsKey(SmsLogColumns.COL_REQUEST_ID)) {
                String colVal = params.getString(SmsLogColumns.COL_REQUEST_ID);
                values.put(SmsLogColumns.COL_REQUEST_ID, colVal);
            }
            String paramString = convertAsJsonString(params);
            if (paramString != null) {
                values.put(SmsLogColumns.COL_MESSAGE_PARAMS, paramString);
            }
        }
        return values;
    }

    private static String convertAsJsonString(Bundle extras) {
        String result = null;
        Log.d(TAG, "convertAsJsonString : " + extras);
        BundleEncoderAdapter src = new BundleEncoderAdapter(null, extras);
        StringBuilder dest = new StringBuilder();
        boolean isNotFirst = false;
        for (String key : extras.keySet()) {
            MessageParamEnum fieldEnum = MessageParamEnum.getByDbFieldName(key);
            if (fieldEnum != null) {
                switch (fieldEnum) {
                    case PERSON_ID:
                        // Ignore this Field
                        break;
                    default:
                        isNotFirst = ParamEncoderHelper.addFieldSep(dest, isNotFirst);
                        fieldEnum.writeTo(src, dest);
                        break;
                }
            }
        }
        result = dest.toString();
        return result;
    }


    // ===========================================================
    // Data Accessor
    // ===========================================================

    public SmsLogHelper initWrapper(Cursor cursor) {
        idIdx = cursor.getColumnIndex(SmsLogColumns.COL_ID);
        timeIdx = cursor.getColumnIndex(SmsLogColumns.COL_TIME);
        actionIdx = cursor.getColumnIndex(SmsLogColumns.COL_ACTION);
        phoneIdx = cursor.getColumnIndex(SmsLogColumns.COL_PHONE);
        phoneMinMatchIdx = cursor.getColumnIndex(SmsLogColumns.COL_PHONE_MIN_MATCH);
        smsLogTypeIdx = cursor.getColumnIndex(SmsLogColumns.COL_SMSLOG_TYPE);
        messageIdx = cursor.getColumnIndex(SmsLogColumns.COL_MESSAGE);
        messageParamIdx = cursor.getColumnIndex(SmsLogColumns.COL_MESSAGE_PARAMS);
        smsLogSideIdx = cursor.getColumnIndex(SmsLogColumns.COL_SMS_SIDE);
        requestIdIdx = cursor.getColumnIndex(SmsLogColumns.COL_REQUEST_ID);

        sendAckTimeInMsIdx = cursor.getColumnIndex(SmsLogColumns.COL_MSG_ACK_SEND_TIME_MS);
        sendDeliveryAckTimeInMsIdx = cursor.getColumnIndex(SmsLogColumns.COL_MSG_ACK_DELIVERY_TIME_MS);

        isNotInit = false;
        return this;
    }

    public SmsLog getEntity(Cursor cursor) {
        if (isNotInit) {
            initWrapper(cursor);
        }
        SmsLog user = new SmsLog();
        user.setId(idIdx > -1 ? cursor.getLong(idIdx) : -1);
        user.setTime(timeIdx > -1 ? cursor.getLong(timeIdx) : SmsLog.UNSET_TIME);
        user.setAction(actionIdx > -1 ? getSmsMessageActionEnum(cursor) : null);
        user.setPhone(phoneIdx > -1 ? cursor.getString(phoneIdx) : null);
        user.setSmsLogType(smsLogTypeIdx > -1 ? getSmsLogType(cursor) : null);
        user.setMessage(messageIdx > -1 ? cursor.getString(messageIdx) : null);
        user.setMessageParams(messageParamIdx > -1 ? cursor.getString(messageParamIdx) : null);
        user.setSide(smsLogSideIdx > -1 ? getSmsLogSideEnum(cursor) : null);
        user.setRequestId(requestIdIdx > -1 ? cursor.getString(requestIdIdx) : null);
        return user;
    }

    private SmsLogHelper setTextWithIdx(TextView view, Cursor cursor, int idx) {
        view.setText(cursor.getString(idx));
        return this;
    }

    public SmsLogHelper setTextSmsLogId(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, idIdx);
    }

    public String getSmsLogIdAsString(Cursor cursor) {
        return cursor.getString(idIdx);
    }

    public long getSmsLogId(Cursor cursor) {
        return cursor.getLong(idIdx);
    }

    public String getSmsLogPhone(Cursor cursor) {
        return cursor.getString(phoneIdx);
    }

    public SmsLogTypeEnum getSmsLogType(Cursor cursor) {
        return SmsLogTypeEnum.getByCode(cursor.getInt(smsLogTypeIdx));
    }

    public MessageActionEnum getSmsMessageActionEnum(Cursor cursor) {
        String actionValue = cursor.getString(actionIdx);
        return MessageActionEnum.getByDbCode(actionValue);
    }

    public String getSmsMessageActionString(Cursor cursor) {
        String actionValue = cursor.getString(actionIdx);
        return actionValue;
    }

    // ===========================================================
    // Field Setter
    // ===========================================================

    public SmsLogSideEnum getSmsLogSideEnum(Cursor cursor) {
        int key = cursor.getInt(smsLogSideIdx);
        return SmsLogSideEnum.getByDbCode(key);
    }

    public long getSendAckTimeInMs(Cursor cursor) {
        return cursor.getLong(sendAckTimeInMsIdx);
    }

    public long getSendDeliveryAckTimeInMs(Cursor cursor) {
        return cursor.getLong(sendDeliveryAckTimeInMsIdx);
    }

    public String getMessage(Cursor cursor) {
        return cursor.getString(messageIdx);
    }

    //

    public String getMessageParams(Cursor cursor) {
        return cursor.getString(messageParamIdx);
    }

    public SmsLogHelper setTextSmsLogAction(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, actionIdx);
    }

    public SmsLogHelper setTextSmsLogMessage(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, messageIdx);
    }

    public SmsLogHelper setTextSmsLogPhone(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, phoneIdx);
    }

    public SmsLogHelper setTextSmsLogTime(TextView view, Cursor cursor) {
        return setTextWithIdx(view, cursor, timeIdx);
    }

    public long getSmsLogTime(Cursor cursor) {
        return cursor.getLong(timeIdx);
    }


}
