package eu.ttbox.geoping.service.encoder;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.encoder.SmsEncoderHelper;
import eu.ttbox.geoping.encoder.crypto.TextEncryptor;
import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.encoder.model.MessageParamEnum;
import eu.ttbox.geoping.encoder.params.MessageParamField;
import eu.ttbox.geoping.service.encoder.adpater.BundleEncoderAdapter;
import eu.ttbox.geoping.service.master.GeoPingMasterService;
import eu.ttbox.geoping.service.slave.GeoPingSlaveService;

public class MessageEncoderHelper {

    public static final String TAG = "MessageEncoderHelper";

    // ===========================================================
    // Encoder
    // ===========================================================

    public static String encodeSmsMessage(MessageActionEnum action, Bundle params) {
        BundleEncoderAdapter src = new BundleEncoderAdapter(action, params);
        TextEncryptor textEncryptor = null;
        String encrypedMsg = SmsEncoderHelper.encodeSmsMessage( action, src, textEncryptor);
        return encrypedMsg;
    }


    // ===========================================================
    // Decoder
    // ===========================================================

    public static boolean isGeoPingEncodedSmsMessageEncrypted(String encryped) {
        return SmsEncoderHelper.isGeoPingEncodedSmsMessageEncrypted(encryped);
    }

    public static  List<BundleEncoderAdapter> decodeSmsMessage( String phone, String encryped,  TextEncryptor textEncryptor) {
        BundleEncoderAdapter dest = new BundleEncoderAdapter();
        List<BundleEncoderAdapter> result =   SmsEncoderHelper.decodeSmsMessage(dest,   phone, encryped, textEncryptor);
        return result;
    }

    // ===========================================================
    // GeoPing Service
    // ===========================================================

    public static boolean startServiceGeoPingMessageAsIntent(Context context, List<BundleEncoderAdapter> msgs) {
        boolean isConsume = false;
        if (msgs == null ||  msgs.isEmpty()  || msgs.get(0).getAction() == null) {
            Log.w(TAG, String.format("Ignore for No Action the GeoPingMessage : %s",  msgs.get(0) ));
            return isConsume;
        }
        for (BundleEncoderAdapter msg : msgs) {
            Intent intent = convertSingleGeoPingMessageAsIntent(context, msg);
            if (intent != null) {
                isConsume = true;
                context.startService(intent);
            }
        }
        return isConsume;
    }


    private static Intent convertSingleGeoPingMessageAsIntent(Context context, BundleEncoderAdapter msg) {
        if (msg == null || msg.getAction() == null) {
            Log.w(TAG, String.format("Ignore for No Action the GeoPingMessage : %s", msg));
            return null;
        }
        MessageActionEnum action = msg.getAction();
        Log.d(TAG, String.format("Create Intent from %s", msg));
        // Create Intent
        Intent intent = new Intent(context, getServiceClassForSmsAction(action)) //
                .setAction(action.intentAction);//

        if (!msg.isEmpty()) {
            intent.putExtra(Intents.EXTRA_SMS_PARAMS, msg.getMap());
        }
        intent.putExtra(Intents.EXTRA_SMS_ACTION, action);
        intent.putExtra(Intents.EXTRA_SMS_PHONE, msg.getPhone()); //

        return intent;
    }


    private static Class getServiceClassForSmsAction(MessageActionEnum action) {
        if (action.isConsumeMaster) {
            return GeoPingMasterService.class;
        } else {
            return GeoPingSlaveService.class;
        }
    }


    // ===========================================================
    // Writer / Reader
    // ===========================================================

    public static boolean isToBundle(Bundle extras, MessageParamEnum field ) {
        return isToBundle( extras, field.type );
    }


    public static boolean isToBundle(Bundle extras, MessageParamField type) {
        if (extras==null) {
            return false;
        }
        return extras.containsKey(type.dbFieldName);
    }

    public static Bundle writeToBundle(Bundle extras, MessageParamEnum field, long value) {
        return writeToBundle( extras, field.type,   value);
    }

    public static Bundle writeToBundle(Bundle extras, MessageParamField type, long value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putLong(type.dbFieldName, value);
        return params;
    }

    public static Bundle writeToBundle(Bundle extras, MessageParamEnum field, int value) {
        return writeToBundle( extras, field.type,   value);
    }

    public static Bundle writeToBundle(Bundle extras, MessageParamField type, int value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putInt(type.dbFieldName, value);
        return params;
    }

    public static Bundle writeToBundle(Bundle extras, MessageParamEnum field, int[] value) {
        return writeToBundle( extras, field.type,   value);
    }

    public static Bundle writeToBundle(Bundle extras, MessageParamField type, int[] value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putIntArray(type.dbFieldName, value);
        return params;
    }

    public static Bundle writeToBundle(Bundle extras, MessageParamEnum field, String value) {
        return writeToBundle( extras, field.type,   value);
    }


    public static Bundle writeToBundle(Bundle extras, MessageParamField type, String value) {
        Bundle params = extras == null ? new Bundle() : extras;
        params.putString(type.dbFieldName, value);
        return params;
    }

    public static long readLong(Bundle extras, MessageParamEnum field, long value) {
        return readLong( extras, field.type,   value);
    }


    public static long readLong(Bundle params, MessageParamField type, long defaultValue) {
        long result = defaultValue;
        if (params != null && params.containsKey(type.dbFieldName)) {
            result = params.getLong(type.dbFieldName, defaultValue);
        }
        return result;
    }

    public static int readInt(Bundle extras, MessageParamEnum field, int value) {
        return readInt( extras, field.type,   value);
    }

    public static int readInt(Bundle params, MessageParamField type, int defaultValue) {
        int result = defaultValue;
        if (params != null && params.containsKey(type.dbFieldName)) {
            result = params.getInt(type.dbFieldName, defaultValue);
        }
        return result;
    }

    public static String readString(Bundle extras, MessageParamEnum field ) {
        return readString( extras, field.type);
    }

    public static String readString(Bundle params, MessageParamField type) {
        String result = null;
        if (params != null && params.containsKey(type.dbFieldName)) {
            result = params.getString(type.dbFieldName);
        }
        return result;
    }


}
