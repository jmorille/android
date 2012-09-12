package eu.ttbox.geoping.service;

import android.util.Log;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.crypto.SimpleCrypto;
import eu.ttbox.geoping.domain.GeoTrackSmsMsg;

/**
 * GSM Alphabet
 * <ul>
 * <li>{link http://www.dreamfabric.com/sms/default_alphabet.html}</li>
 * <li>{link http://en.wikipedia.org/wiki/GSM_03.38}</li>
 * </ul>
 * 
 */
public class SmsMsgEncryptHelper {

	public final static String TAG = "SmsMsgEncryptHelper";

	// Action
	public static String ACTION_END = "!"; // new String(
											// Character.toChars(34));

	public final static String ACTION_GEO_PING = "WRY";
	public final static String ACTION_GEO_LOC = "LOC";

	// Constante
	public final static String MSGID = "geoPing?";
	public final static String SEED = "pass";

	// Config
	public final static boolean isMsgEncrypted = false;

	public static GeoTrackSmsMsg decodeSmsMessage(String smsNumber,
			String encrypted) {
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
				if (clearMsgSize > idxActEnd+1) {
					result.body = clearMsg.substring(idxActEnd + 1,
							clearMsgSize);
				}
			}
		}
		return result;
	}

	public static String encodeSmsMessage(GeoTrackSmsMsg msg) {
		StringBuilder sb = new StringBuilder(AppConstants.SMS_MAX_SIZE);
		sb.append(msg.action);
		sb.append(ACTION_END);
		if (msg.body != null) {
			sb.append(msg.body);
		}
		String encryptedMsg = encryptSmsMsg(sb.toString());
		return encryptedMsg;
	}

	private static String encryptSmsMsg(String msg) {
		StringBuilder sb = new StringBuilder(AppConstants.SMS_MAX_SIZE);
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
