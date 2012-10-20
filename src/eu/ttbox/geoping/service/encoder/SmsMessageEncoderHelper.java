package eu.ttbox.geoping.service.encoder;

import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;

public class SmsMessageEncoderHelper {
    public final static String TAG = "SmsMessageEncoderHelper";

    public final static String GEOPING_MSGID = "geoPing?";
    public final static int GEOPING_MSGID_SIZE = GEOPING_MSGID.length();

    // public final static String GEOPING_PROTOCOL_CLEAR = "0";

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
        return encodeSmsMessage(action.smsAction, params, SmsParamEncoderHelper.NUMBER_ENCODER_RADIX);
    }

    public static String encodeSmsMessage(SmsMessageActionEnum action, Bundle params, int radix) {
        return encodeSmsMessage(action.smsAction, params, radix);
    }

    public static String encodeSmsMessage(String action, Bundle params, int radix) {
        StringBuilder sb = new StringBuilder(AppConstants.SMS_MAX_SIZE);
        sb.append(action);

        if (params != null && !params.isEmpty()) {
            sb.append(PARAM_BEGIN);
            SmsParamEncoderHelper.encodeMessage(params, sb, radix);
            sb.append(PARAM_END);
        } else {
            sb.append(ACTION_END);
        }
        // String encryptedMsg = encryptSmsMsg(sb.toString());
        sb.insert(0, GEOPING_MSGID);
        return sb.toString();
    }

    public static GeoPingMessage decodeSmsMessage(String phone, String encryped) {
        return decodeSmsMessage(phone, encryped, SmsParamEncoderHelper.NUMBER_ENCODER_RADIX);
    }

    public static GeoPingMessage decodeSmsMessage(String phone, String encryped, int radix) {
        GeoPingMessage result = null;
        if (encryped.startsWith(GEOPING_MSGID)) {
            int startIdx = GEOPING_MSGID_SIZE;
            String encodedMsg = encryped;
            // Decodes
            int encodedMsgSize = encodedMsg.length();

            // Params
            int idxActEnd = encodedMsg.indexOf(ACTION_END, startIdx);
            int idxParamBegin = encodedMsg.indexOf(PARAM_BEGIN, startIdx);
            // --- Manage Combinaison of End action
            // ----------------------------------------
            boolean isIdxActionEnd = idxActEnd > -1;
            boolean isIdxParamBegin = idxParamBegin > -1;
            // Log.d(TAG,
            // String.format("Message isIdxActionEnd=%s / isIdxParamBegin=%s : %s",
            // isIdxActionEnd, isIdxParamBegin, encryped ));
            if (isIdxParamBegin && isIdxActionEnd) {
                if (idxParamBegin < idxActEnd) {
                    isIdxActionEnd = isIdxParamBegin;
                    idxActEnd = idxParamBegin;
                } else {
                    // After an action, it sould be a MultiAction Message => Not
                    // manage Now
                    isIdxParamBegin = false;
                }
            } else if (isIdxParamBegin) {
                isIdxActionEnd = isIdxParamBegin;
                idxActEnd = idxParamBegin;
            } else if (!isIdxParamBegin && !isIdxActionEnd) {
                idxActEnd = encodedMsgSize;
                isIdxActionEnd = true;
            }
            // Log.d(TAG,
            // String.format("Message isIdxActionEnd=%s / isIdxParamBegin=%s : %s",
            // isIdxActionEnd, isIdxParamBegin, encryped ));
            // --- Extract End Action
            // ----------------------------------------
            if (isIdxActionEnd) {
                Bundle params = null;
                String smsAction = encodedMsg.substring(startIdx, idxActEnd);
                // Log.d(TAG, String.format("Message action=[%s] : %s",
                // smsAction, encryped ));
                SmsMessageActionEnum action = SmsMessageActionEnum.getBySmsCode(smsAction);
                if (action == null) {
                    return null;
                }
                // Check Param
                int idxParamEnd = encodedMsg.indexOf(PARAM_END, idxActEnd);
                boolean isIdxParamEnd = idxParamEnd > -1;
                if (isIdxParamBegin && isIdxParamEnd) {
                    String encodedParams = encodedMsg.substring(idxParamBegin + 1, idxParamEnd);
                    params = SmsParamEncoderHelper.decodeMessageAsMap(encodedParams, null, radix);
                    // Log.d(TAG,String.format("Sms Decoded Message : params = [%s]",encodedParams));
                }
                if (isIdxParamEnd) {
                    startIdx = idxParamEnd;
                }
                // Create Result
                result = new GeoPingMessage(phone, action, params);
                // --- Compute if next Message
                // ----------------------------------------
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
