package eu.ttbox.geoping.encoder;



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


}
