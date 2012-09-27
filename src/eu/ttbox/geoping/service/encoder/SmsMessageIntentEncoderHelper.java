package eu.ttbox.geoping.service.encoder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.core.Intents;

public class SmsMessageIntentEncoderHelper {

    public static final String TAG = "SmsMessageIntentEncoderHelper";


    // ===========================================================
    // Encoder
    // ===========================================================
    public static String encodeSmsMessage(SmsMessageActionEnum action, Bundle params) { 
        String encrypedMsg = SmsMessageEncoderHelper.encodeSmsMessage(action.smsAction, params);
        return encrypedMsg;
    }

    
    // ===========================================================
    // Decoder
    // ===========================================================

    
    public static Intent decodeSmsMessage(Context context, String phone, String encryped) {
        Intent intent = null;
        GeoPingMessage clearMsg = SmsMessageEncoderHelper.decodeSmsMessage(phone, encryped);
        SmsMessageActionEnum intentAction =  clearMsg.action;
        if (intentAction!=null) {
            intent = convertForIntentGeoPingMessage(context, clearMsg, intentAction);
        } 
        return intent;
    }
    
    
    public static Intent convertForIntentGeoPingMessage(Context context, GeoPingMessage msg, SmsMessageActionEnum intentAction) {
        Log.d(TAG, String.format("Create Intent from %s", msg));
        Intent intent = new Intent(context, intentAction.serviceClass) //
                .setAction(intentAction.intentAction);//
        if (msg.params != null && !msg.params.isEmpty()) {
            intent.putExtra(Intents.EXTRA_SMS_PARAMS, msg.params);
        }
        intent.putExtra(Intents.EXTRA_SMS_ACTION, msg.action);
        intent.putExtra(Intents.EXTRA_SMS_PHONE, msg.phone); //
        return intent;
    }
}
