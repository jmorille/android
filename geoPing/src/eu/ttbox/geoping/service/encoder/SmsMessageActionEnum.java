package eu.ttbox.geoping.service.encoder;

import java.util.HashMap;

import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.service.master.GeoPingMasterService;
import eu.ttbox.geoping.service.slave.GeoPingSlaveService;

public enum SmsMessageActionEnum {

    // Slave
    GEOPING_REQUEST(SmsMessageEncoderHelper.ACTION_GEO_PING, Intents.ACTION_SMS_GEOPING_REQUEST_HANDLER, GeoPingSlaveService.class), //
    ACTION_GEO_PAIRING(SmsMessageEncoderHelper.ACTION_GEO_PAIRING, Intents.ACTION_SMS_PAIRING_RESQUEST, GeoPingSlaveService.class), //
   
    // Master
    ACTION_GEO_LOC(SmsMessageEncoderHelper.ACTION_GEO_LOC, Intents.ACTION_SMS_GEOPING_RESPONSE_HANDLER, GeoPingMasterService.class), //
    ACTION_GEO_PAIRING_RESPONSE(SmsMessageEncoderHelper.ACTION_GEO_PAIRING_RESPONSE, Intents.ACTION_SMS_PAIRING_RESPONSE, GeoPingMasterService.class), //
    // Receive Notif
    ACTION_SPY_EVENT(SmsMessageEncoderHelper.ACTION_SPY_EVENT, Intents.ACTION_SMS_EVT_RESPONSE, GeoPingMasterService.class);

    // ===========================================================
    // Constructor
    // ===========================================================

    private SmsMessageActionEnum(String smsAction, String intentAction, Class<?> cls) {
        this.intentAction = intentAction;
        this.smsAction = smsAction;
        this.serviceClass = cls;
    }

    public final String intentAction;
    public final String smsAction;
    public final Class<?> serviceClass;

    // ===========================================================
    // Conversion Init
    // ===========================================================

    static HashMap<String, SmsMessageActionEnum> bySmsCodeNames;
    static HashMap<String, SmsMessageActionEnum> byIntentNames;

    static {
        SmsMessageActionEnum[] values = SmsMessageActionEnum.values();
        HashMap<String, SmsMessageActionEnum> smsCodes = new HashMap<String, SmsMessageActionEnum>(values.length);
        HashMap<String, SmsMessageActionEnum> intentNames = new HashMap<String, SmsMessageActionEnum>(values.length);
        for (SmsMessageActionEnum field : values) {
            // Sms Code
            addAndCheckUnique(smsCodes, field, field.smsAction);
            // intent name
            addAndCheckUnique(intentNames, field, field.intentAction);
        }
        // Affect
        bySmsCodeNames = smsCodes;
        byIntentNames = intentNames;
    }

    private static void addAndCheckUnique(HashMap<String, SmsMessageActionEnum> map, SmsMessageActionEnum field, String key) {
        if (map.containsKey(key)) {
            throw new IllegalArgumentException(String.format("Duplicated Key %s", key));
        }
        map.put(key, field);
    }

    // ===========================================================
    // Conversion Accessor
    // ===========================================================

    public static SmsMessageActionEnum getByIntentName(String fieldName) {
        if (fieldName==null) {
            return null;
        }
        return byIntentNames.get(fieldName);
    }

    public static SmsMessageActionEnum getBySmsCode(String fieldName) {
        if (fieldName==null) {
            return null;
        }
        return bySmsCodeNames.get(fieldName);
    }
    
    public static SmsMessageActionEnum getByCode(int code) {
    	return SmsMessageActionEnum.values()[code];
    }

	public int getCode() {
		return ordinal();
	}


}
