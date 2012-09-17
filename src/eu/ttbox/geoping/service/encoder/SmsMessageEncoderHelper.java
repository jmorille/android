package eu.ttbox.geoping.service.encoder;

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

    public static final char PARAM_BEGIN = '(';
    public static final char PARAM_END = ')';

    // Constante

    // ===========================================================
    // Encoder
    // ===========================================================

    public static String encodeSmsMessage(GeoPingMessage msg) {
        StringBuilder sb = new StringBuilder(AppConstants.SMS_MAX_SIZE);
        sb.append(msg.action);
        sb.append(ACTION_END);
        if (msg.params != null && !msg.params.isEmpty()) {
            sb.append(PARAM_BEGIN);
            SmsParamEncoderHelper.encodeMessage(msg.params, sb);
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
                result = new GeoPingMessage();
                result.phone = phone;
                result.action = clearMsg.substring(GEOPING_MSG_ID.length(), idxActEnd);
                // Check Param
                int idxParamBegin = clearMsg.indexOf(PARAM_BEGIN, idxActEnd);
                int idxParamEnd = clearMsg.indexOf(PARAM_END, idxActEnd);
                if (idxParamBegin > -1 && idxParamEnd > -1) {
                    String encodedParams = clearMsg.substring(idxParamBegin + 1, idxParamEnd);
                    result.params = SmsParamEncoderHelper.decodeMessageAsMap(encodedParams);
                    Log.d(TAG, String.format("Sms Decoded Message : params = [%s]", encodedParams));
                    // result.params = clearMsg.substring(idxActEnd + 1,
                    // clearMsgSize);
                }
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
