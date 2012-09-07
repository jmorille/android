package eu.ttbox.smstraker.service;

import android.util.Log;
import eu.ttbox.smstraker.core.crypto.SimpleCrypto;
import eu.ttbox.smstraker.domain.GeoTrackSmsMsg;

public class SmsMsgEncryptHelper {

    public final static String TAG = "SmsMsgEncryptHelper";

    // Action
    public static String ACTION_END = "!";

    public final static String ACTION_GEO_PING = "GEO_PING" + ACTION_END;
    public final static String ACTION_GEO_LOC = "GEO_LOC" + ACTION_END;

    // Constante
    public final static String MSGID = "smsTracker#";
    public final static String SEED = "pass";

    // Config
    public final static boolean isMsgEncrypted = false;

    public static GeoTrackSmsMsg decodeSmsMessage(String smsNumber, String encrypted) {
        GeoTrackSmsMsg result = null;
        String clearMsg = decryptSmsMsg(encrypted);
        if (clearMsg != null) {
            int clearMsgSize = clearMsg.length();
            int idxActEnd = clearMsg.indexOf(ACTION_END);
            if (idxActEnd > 0) {
                result = new GeoTrackSmsMsg();
                result.smsNumber = smsNumber;
                result.action = clearMsg.substring(0, idxActEnd);
                // Check Param
                if (clearMsgSize > idxActEnd) {
                    result.body = clearMsg.substring(idxActEnd + 1, clearMsgSize);
                }
            }
        }
        return result;
    }

    public static String encodeSmsMessage(GeoTrackSmsMsg msg) {
        StringBuilder sb = new StringBuilder(255);
        sb.append(msg.action);
        if (msg.body != null) {
            sb.append(msg.body);
        }
        String encryptedMsg = encryptSmsMsg(sb.toString());
        return encryptedMsg;
    }

    private static String encryptSmsMsg(String msg) {
        StringBuilder sb = new StringBuilder(255);
        sb.append(MSGID);
        sb.append(encrypt(msg));
        return sb.toString();
    }

    private static String decryptSmsMsg(String msg) {
        if (msg.startsWith(MSGID)) {
            String result = msg.substring(MSGID.length(), msg.length());
            String body = decrypt(result);
            return body;
        }
        return null;
    }

    private static String encrypt(String cleartext) {
        String body = cleartext;
        if (isMsgEncrypted) {
            try {
                body = SimpleCrypto.encrypt(SEED, cleartext);
            } catch (Exception e) {
                Log.e(TAG, "Encrypt error : " + e.getMessage());
                body = null;
            }
        }
        return body;
    }

    private static String decrypt(String encrypted) {
        String body = encrypted;
        if (isMsgEncrypted) {
            try {
                body = SimpleCrypto.decrypt(SEED, encrypted);
            } catch (Exception e) {
                Log.e(TAG, "Decrypt error : " + e.getMessage());
                body = null;
            }
        }
        return body;
    }

}
