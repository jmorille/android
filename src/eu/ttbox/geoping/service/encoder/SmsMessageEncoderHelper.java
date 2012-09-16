package eu.ttbox.geoping.service.encoder;

import eu.ttbox.geoping.core.AppConstants;

public class SmsMessageEncoderHelper {

    public final static String GEOPING_MSG_ID = "geoPing?";
    public final static String GEOPING_PROTOCOL_CLEAR = "0";

    // Action
    public static String ACTION_END = "!"; // new String(
                                           // Character.toChars(34));

    public final static String ACTION_GEO_PING = "WRY";
    public final static String ACTION_GEO_LOC = "LOC";

    
    // Constante

    // ===========================================================
    // Encoder
    // ===========================================================

    public static String encodeSmsMessage(GeoPingMessage msg) {
        StringBuilder sb = new StringBuilder(AppConstants.SMS_MAX_SIZE); 
        sb.append(msg.action);
        sb.append(ACTION_END);
        if (msg.params != null && !msg.params.isEmpty()) {
//            sb.append(msg.body);
        }
//        String encryptedMsg = encryptSmsMsg(sb.toString());
        sb.insert(0, GEOPING_MSG_ID);
        return sb.toString();
    }

    // ===========================================================
    // Decoder
    // ===========================================================

    // ===========================================================
    // Other
    // ===========================================================

}
