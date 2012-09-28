package eu.ttbox.geoping.service.encoder;

import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;

public class SmsMessageEncoderHelper {
    public final static String TAG = "SmsMessageEncoderHelper";

    public final static String GEOPING_MSG_ID = "geoPing?";
    public final static String GEOPING_PROTOCOL_CLEAR = "0";

    // Action
    public static String ACTION_END = "!"; // new String(
                                           // Character.toChars(34));

    public final static String ACTION_GEO_PING = "WRY";
    public final static String ACTION_GEO_LOC = "LOC";
    public final static String ACTION_GEO_PAIRING = "PAQ";
    public final static String ACTION_GEO_PAIRING_RESPONSE = "PAR";

    public static final char PARAM_BEGIN = '(';
    public static final char PARAM_END = ')';

    // Constante

    // ===========================================================
    // Encoder
    // ===========================================================

    // @Deprecated
    // public static String encodeSmsMessage(GeoPingMessage msg) {
    // return encodeSmsMessage(msg.action, msg.params);
    // }

    public static String encodeSmsMessage(SmsMessageActionEnum action, Bundle params) {
        return encodeSmsMessage(action.smsAction, params);
    }
    
    public static String encodeSmsMessage(String action, Bundle params) {
        StringBuilder sb = new StringBuilder(AppConstants.SMS_MAX_SIZE);
        sb.append(action);
        sb.append(ACTION_END);
        if (params != null && !params.isEmpty()) {
            sb.append(PARAM_BEGIN);
            SmsParamEncoderHelper.encodeMessage(params, sb);
            sb.append(PARAM_END);
        }
        // String encryptedMsg = encryptSmsMsg(sb.toString());
        sb.insert(0, GEOPING_MSG_ID);
        return sb.toString();
    }

    public static GeoPingMessage decodeSmsMessage(String phone, String encryped) {
         GeoPingMessage result = null;
        if (encryped.startsWith(GEOPING_MSG_ID)) {
            String clearMsg = encryped;
            // Decodes
            int clearMsgSize = clearMsg.length();
            int idxActEnd = clearMsg.indexOf(ACTION_END);
            if (idxActEnd > 0) {
                Bundle params = null;
                String smsMessage = clearMsg.substring(GEOPING_MSG_ID.length(), idxActEnd);
                SmsMessageActionEnum action = SmsMessageActionEnum.getBySmsCode(smsMessage);
                // Check Param
                int idxParamBegin = clearMsg.indexOf(PARAM_BEGIN, idxActEnd);
                int idxParamEnd = clearMsg.indexOf(PARAM_END, idxActEnd);
                if (idxParamBegin > -1 && idxParamEnd > -1) {
                    String encodedParams = clearMsg.substring(idxParamBegin + 1, idxParamEnd);
                    params = SmsParamEncoderHelper.decodeMessageAsMap(encodedParams);
                    Log.d(TAG, String.format("Sms Decoded Message : params = [%s]", encodedParams));
                    // result.params = clearMsg.substring(idxActEnd + 1,
                    // clearMsgSize);
                }
                // Create Result
                result = new GeoPingMessage(phone, action, params);
            }
        }
        return result;
    }

    // ===========================================================
    // Decoder
    // ===========================================================

    // ===========================================================
    // Other
    // ===========================================================

}
