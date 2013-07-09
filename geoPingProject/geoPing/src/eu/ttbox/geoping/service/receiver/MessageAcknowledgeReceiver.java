package eu.ttbox.geoping.service.receiver;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase.SmsLogColumns;

public class MessageAcknowledgeReceiver extends BroadcastReceiver {

    public static final String ACTION_SEND_ACK = "eu.ttbox.geoping.ACTION_SEND_ACK";
    public static final String ACTION_DELIVERY_ACK = "eu.ttbox.geoping.ACTION_DELIVERY_ACK";
    public static final String EXTRA_ACK_MSG_PART_COUNT = "EXTRA_ACK_MSG_PART_COUNT";
    public static final String EXTRA_ACK_MSG_PART_ID = "EXTRA_ACK_MSG_PART_ID";
    public static final String EXTRA_PDU = "pdu";
    public static final String EXTRA_FORMAT = "format";
    private static final String TAG = "MessageAcknowledgeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Message Acknowledge action : " + action);
//        printExtras(intent.getExtras());

        if (ACTION_SEND_ACK.equals(action)) {
            Uri logUri = intent.getData();
            int[] msgCountPartId = readMessageCountAndPartId(intent);
            Log.d(TAG, "GeoPing message Acknowledge Send : " + logUri);
            long acknowledgeTimeInMs = System.currentTimeMillis();
            saveAcknowledge(context, intent, logUri, SmsLogTypeEnum.SEND_ACK, acknowledgeTimeInMs, msgCountPartId);
        } else if (ACTION_DELIVERY_ACK.equals(action)) {
            Uri logUri = intent.getData();
            int[] msgCountPartId = readMessageCountAndPartId(intent);
            Log.d(TAG, "GeoPing message Acknowledge Delivery : " + logUri);
            long acknowledgeTimeInMs = System.currentTimeMillis();
            if (intent.hasExtra(EXTRA_PDU)) {
                byte[] msgByte = intent.getByteArrayExtra(EXTRA_PDU);
                SmsMessage message = SmsMessage.createFromPdu(msgByte);
                acknowledgeTimeInMs = message.getTimestampMillis();
                Log.d(TAG, String.format("Message Acknowledge Time :  %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS,%1$tL", acknowledgeTimeInMs));
            }
            saveAcknowledge(context, intent, logUri, SmsLogTypeEnum.SEND_DELIVERY_ACK, acknowledgeTimeInMs, msgCountPartId);
        }
    }

    private int[] readMessageCountAndPartId(Intent intent) {
        int countMsg = intent.getIntExtra(EXTRA_ACK_MSG_PART_COUNT, 1);
        int msgId = intent.getIntExtra(EXTRA_ACK_MSG_PART_ID, 0);
        int[] result = new int[]{countMsg, msgId};
        return result;
    }

    private void printExtras(Bundle extras) {
        if (extras != null) {
            for (String key : extras.keySet()) {
                Object value = extras.get(key);
                Log.d(TAG, "Message Acknowledge extras : " + key + " = " + value);
                if (EXTRA_PDU.equals(key)) {
                    SmsMessage message = SmsMessage.createFromPdu((byte[]) value);
                    Log.d(TAG, "Message Acknowledge extras : " + key + " = " + message);
                    Log.d(TAG, "Message Acknowledge extras : " + key + " = MessageBody    : " + message.getMessageBody());
                    Log.d(TAG, "Message Acknowledge extras : " + key + " = Status         : " + message.getStatus());
                    Log.d(TAG, "Message Acknowledge extras : " + key + " = Ori Address    : " + message.getDisplayOriginatingAddress());
                    Log.d(TAG, "Message Acknowledge extras : " + key + " = Pseudo Subject : " + message.getPseudoSubject());
                    Log.d(TAG, "Message Acknowledge extras : " + key + " = Status         : " + message.getStatus());
                    Log.d(TAG, "Message Acknowledge extras : " + key + " = Status On Icc  : " + message.getStatusOnIcc());
                    Log.d(TAG, "Message Acknowledge extras : " + key + " = TimestampMilli : " + message.getTimestampMillis());
                    String timeAsString = String.format("Date %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS,%1$tL", //
                            message.getTimestampMillis());
                    Log.d(TAG, "Message Acknowledge extras : " + key + " = TimestampMilli : " + String.format("Date %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS,%1$tL", //
                            message.getTimestampMillis()));
                    Log.d(TAG, "Message Acknowledge extras : " + key + " = UserData       : " + message.getUserData());

                }
            }
        }
    }

    /**
     * Do the update of the Message Acknowledge.
     *
     * @param context          The context
     * @param logUri           The Log Uri
     * @param ackTypeRequested Send=true and delivery = false
     */
    private void saveAcknowledge(Context context, Intent intent, Uri logUri, SmsLogTypeEnum ackTypeRequested,
                                 long acknowledgeTimeInMs, int[] msgCountPartId) {
        SmsLogTypeEnum ackType = ackTypeRequested;
        String acknowledgeType;
        String acknowledgeIncrement;
        String acknowledgeResultMsg;
        if (SmsLogTypeEnum.SEND_DELIVERY_ACK.equals(ackType)) {
            acknowledgeType = SmsLogColumns.COL_MSG_ACK_DELIVERY_TIME_MS;
            acknowledgeIncrement = SmsLogColumns.COL_MSG_ACK_DELIVERY_MSG_COUNT;
            acknowledgeResultMsg = SmsLogColumns.COL_MSG_ACK_DELIVERY_RESULT_MSG;
        } else {
            acknowledgeType = SmsLogColumns.COL_MSG_ACK_SEND_TIME_MS;
            acknowledgeIncrement = SmsLogColumns.COL_MSG_ACK_SEND_MSG_COUNT;
            acknowledgeResultMsg = SmsLogColumns.COL_MSG_ACK_SEND_RESULT_MSG;
        }
        // Manage Code
        String message = null;
        boolean error = true;
        boolean completeMsg = false;
        // Prepare Db Update
        ContentValues values = new ContentValues();
        // Test Result Code
        int resultCode = getResultCode(); // TODO Really not in the intent ?
        switch (resultCode) {
            case Activity.RESULT_OK: {
                int msgCount = msgCountPartId[0];
                int msgId = msgCountPartId[1];
                message = "Message : " + msgId + '/' + msgCount;
                // Manage the Message Part
                if (msgCount == msgId) {
                    completeMsg = true;
                }
                // Manage Acknowledge increment
                values.put(acknowledgeIncrement, msgId);
                error = false;
            }
            break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                message = "Error.";
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                message = "Error: No service.";
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                message = "Error: Null PDU.";
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                message = "Error: Radio off.";
                break;
        }
        if (error) {
            ackType = SmsLogTypeEnum.SEND_ERROR;
        }
        Log.d(TAG, "### ############################### ### ");
        Log.d(TAG, "### ###  ACTION_SMS_SENT : " + message);
        Log.d(TAG, "### ############################### ### ");
        // Save Acknowledge
        values.put(acknowledgeType, acknowledgeTimeInMs);
        ackType.writeTo(values);
        if (!TextUtils.isEmpty(message)) {
            values.put(acknowledgeResultMsg, message);
        }
        // Save Update
        ContentResolver cr = context.getContentResolver();
        cr.update(logUri, values, null, null);
    }

}
