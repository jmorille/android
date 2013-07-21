package eu.ttbox.geoping.encoder;



import java.util.ArrayList;
import java.util.List;

import eu.ttbox.geoping.encoder.adapter.DecoderAdapter;
import eu.ttbox.geoping.encoder.adapter.EncoderAdapter;
import eu.ttbox.geoping.encoder.crypto.TextEncryptor;
import eu.ttbox.geoping.encoder.model.MessageActionEnum;
import eu.ttbox.geoping.encoder.params.ParamEncoderHelper;

public class SmsEncoderHelper {

    // Message Hearder
    public final static String GEOPING_MSGID = "geoPing?";
    public final static String GEOPING_ENCRYPT_MSGID = "ge0Ping?";
    public final static int GEOPING_MSGID_SIZE = GEOPING_MSGID.length();
    public final static int GEOPING_ENCRYPT_MSGID_SIZE = GEOPING_ENCRYPT_MSGID.length();

    // Message Param
    public static String ACTION_END = "!";
    public static final char PARAM_BEGIN = '(';
    public static final char PARAM_END = ')';


    // ===========================================================
    // Encoder
    // ===========================================================


    public static String encodeSmsMessage(MessageActionEnum action, EncoderAdapter params, TextEncryptor textEncryptor) {
        StringBuilder sb = new StringBuilder(ParamEncoderHelper.SMS_MAX_SIZE_7BITS);
        sb.append(action.smsAction);
        if (params != null && !params.isEmpty()) {
            sb.append(PARAM_BEGIN);
            ParamEncoderHelper.encodeMessage(params, sb );
            sb.append(PARAM_END);
        } else {
            sb.append(ACTION_END);
        }
        if (textEncryptor == null) {
            sb.insert(0, GEOPING_MSGID);
        } else {
            String encryptedMsg = textEncryptor.encrypt(sb.toString());
            sb.delete(0, sb.length());
            sb.append(GEOPING_ENCRYPT_MSGID);
            sb.append(encryptedMsg);
        }
        return sb.toString();
    }

    // ===========================================================
    // Decoder
    // ===========================================================


    public static boolean isGeoPingEncodedSmsMessageEncrypted(String encryped) {
        return encryped.startsWith(GEOPING_ENCRYPT_MSGID);
    }
    public static boolean isGeoPingEncodedSmsMessageObsuscated(String encryped) {
        return encryped.startsWith(GEOPING_MSGID);
    }

    public static DecoderAdapter[] decodeSmsMessage(DecoderAdapter dest, String phone, String encryped, int radix, TextEncryptor textEncryptor) {
        DecoderAdapter[] result = null;
        String decodedBody = null;
        // What is my body
        if (isGeoPingEncodedSmsMessageObsuscated(encryped)) {
            decodedBody = encryped.substring(GEOPING_MSGID_SIZE, encryped.length());
        } else if (isGeoPingEncodedSmsMessageEncrypted(encryped)) {
            String encrypedBody = encryped.substring(GEOPING_ENCRYPT_MSGID_SIZE, encryped.length());
            decodedBody = textEncryptor.decrypt(encrypedBody);
        }
        // As Message
        if (decodedBody != null) {
            int startIdx = 0;
            DecoderAdapter resultLast;
            MessageActionEnum actionLast = null;
            DecoderAdapter resultUnit = dest;
            List<DecoderAdapter> multiMessage = new ArrayList<DecoderAdapter>();
           // Log.d(TAG, String.format("Decode SmsMessage : %s", decodedBody));
            while ((startIdx = decodeSmsMessageBody(resultUnit, phone, decodedBody, startIdx, actionLast)) >= 0) {
                if (resultLast != null) {
                    startIdx = resultLast.nextStartIdx;
                    if (result == null) {
                        result = resultLast;
                    } else {
                        // Multi-actions
                        result.addMultiMessage(resultLast);
                    }
                    actionLast = resultLast.action;
                }
            }
        }
        return result;
    }

    private static final int NO_MESSAGE_TO_DECODE = -1;

    private static int decodeSmsMessageBody(DecoderAdapter dest, final String phone, final String encodedMsg, final int pStartIdx, MessageActionEnum actionLast) {
        int nextStartIdx =  -1;
        int startIdx = pStartIdx;
        //Log.d(TAG, String.format("Decode SmsMessage Body (startIdx=%s) : %s", startIdx, encodedMsg));

        // --- Manage Combinaison of End action
        // ----------------------------------------
        int idxActEnd = encodedMsg.indexOf(ACTION_END, startIdx);
        int idxParamBegin = encodedMsg.indexOf(PARAM_BEGIN, startIdx);
        boolean isIdxActionEnd = idxActEnd > -1;
        boolean isIdxParamBegin = idxParamBegin > -1;
       // Log.d(TAG, String.format("Message isIdxActionEnd=%s / isIdxParamBegin=%s : %s", isIdxActionEnd, isIdxParamBegin, encodedMsg));
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
            int encodedMsgSize = encodedMsg.length();
            if (startIdx < encodedMsgSize) {
                idxActEnd = encodedMsgSize;
                isIdxActionEnd = true;
               // Log.d(TAG, String.format("Define idxActEnd at Full Size Message (Full size %s chars) : %s", encodedMsgSize, encodedMsg));
            }
        }
        // Log.d(TAG,String.format("Message isIdxActionEnd=%s / isIdxParamBegin=%s : %s",isIdxActionEnd,isIdxParamBegin,encodedMsg));

        // --- Extract End Action
        // ----------------------------------------
        if (isIdxActionEnd) {
            String encodedParams = null;
            String smsAction = encodedMsg.substring(startIdx, idxActEnd);
            // Log.d(TAG, String.format("Message action=[%s] : %s", smsAction, encodedMsg));
            MessageActionEnum action = MessageActionEnum.getBySmsCode(smsAction);
            if (action == null) {
                if (actionLast != null && smsAction.trim().length() < 1) {
                    action = actionLast;
                } else {
                    // Not an geoPing Message
                    return NO_MESSAGE_TO_DECODE;
                }
            }
            // Check Param
            int idxParamEnd = encodedMsg.indexOf(PARAM_END, idxActEnd);
            boolean isIdxParamEnd = idxParamEnd > -1;
            if (isIdxParamBegin && isIdxParamEnd) {
                encodedParams = encodedMsg.substring(idxParamBegin + 1, idxParamEnd);
                ParamEncoderHelper.decodeMessageAsMap(dest, encodedParams );
                // Log.d(TAG,String.format("Sms Decoded Message : params = [%s]",encodedParams));
                // --- Prepare Compute if next Message
                startIdx = idxParamEnd + 1;
               // Log.d(TAG, String.format("Define With ParamEnd startIdx : %s", smsAction, encodedMsg));
            } else {
                // --- Prepare Compute if next Message
                startIdx = idxActEnd + 1;
                //Log.d(TAG, String.format("Define No ParamEnd startIdx : %s", smsAction, encodedMsg));
            }
            // Create Result
            dest.setAction(action);
            dest.setPhone(phone);


            // --- Prepare Compute if next Message
            // ----------------------------------------
            nextStartIdx = startIdx;
            return nextStartIdx;
        }
        return NO_MESSAGE_TO_DECODE;
    }

}
