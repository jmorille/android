package eu.ttbox.geoping.service.encoder;

import java.util.HashMap;

import android.content.res.Resources;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.service.master.GeoPingMasterService;
import eu.ttbox.geoping.service.slave.GeoPingSlaveService;

public enum SmsMessageActionEnum {

	// Slave
	GEOPING_REQUEST("WRY", Intents.ACTION_SMS_GEOPING_REQUEST_HANDLER, GeoPingSlaveService.class,false, R.string.sms_action_geoping_request), //
	ACTION_GEO_PAIRING("PAQ", Intents.ACTION_SMS_PAIRING_RESQUEST, GeoPingSlaveService.class, false,R.string.sms_action_pairing_request), //

	// Master
	ACTION_GEO_LOC("LOC", Intents.ACTION_SMS_GEOPING_RESPONSE_HANDLER, GeoPingMasterService.class, true,R.string.sms_action_geoping_response), //
	ACTION_GEO_PAIRING_RESPONSE("PAR", Intents.ACTION_SMS_PAIRING_RESPONSE, GeoPingMasterService.class, true,R.string.sms_action_pairing_response), //
	// Spy Event Notif
	SPY_SHUTDOWN("esd", Intents.ACTION_SMS_EVTSPY_SHUTDOWN, GeoPingMasterService.class,true, R.string.sms_action_spyevt_shutdown), //
	SPY_BOOT("esb", Intents.ACTION_SMS_EVTSPY_BOOT, GeoPingMasterService.class, true,R.string.sms_action_spyevt_boot), //
	SPY_LOW_BATTERY("elb", Intents.ACTION_SMS_EVTSPY_LOW_BATTERY, GeoPingMasterService.class, true,R.string.sms_action_spyevt_low_battery), //
	SPY_PHONE_CALL("epc", Intents.ACTION_SMS_EVTSPY_PHONE_CALL, GeoPingMasterService.class, true,R.string.sms_action_spyevt_phone_call), //
	SPY_SIM_CHANGE("eps", Intents.ACTION_SMS_EVTSPY_SIM_CHANGE, GeoPingMasterService.class, true,R.string.sms_action_spyevt_sim_change);

	// ===========================================================
	// Constructor
	// ===========================================================

	private SmsMessageActionEnum(String smsAction, String intentAction, Class<?> cls, boolean isMaster, int labelStringId) {
		this.intentAction = intentAction;
		this.smsAction = smsAction;
		this.serviceClass = cls;
		this.isMaster = isMaster;
		this.labelResourceId = labelStringId;
	}

	public final String intentAction;
	public final String smsAction;
	public final Class<?> serviceClass;
	public final boolean isMaster;
	
	public final int labelResourceId;
	
	
	 
	
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
	// Static Accessor
	// ===========================================================

	public static SmsMessageActionEnum getByIntentName(String fieldName) {
		if (fieldName == null) {
			return null;
		}
		return byIntentNames.get(fieldName);
	}

	public static SmsMessageActionEnum getBySmsCode(String fieldName) {
		if (fieldName == null) {
			return null;
		}
		return bySmsCodeNames.get(fieldName);
	}

	public static SmsMessageActionEnum getByCode(int code) {
		return SmsMessageActionEnum.values()[code];
	}
	public static SmsMessageActionEnum getByDbCode(String dbCode) {
		return SmsMessageActionEnum.valueOf(dbCode);
	}
	// ===========================================================
	// Instance Accessor
	// ===========================================================
	public String getLabel(Resources resources) {
		return resources.getString(labelResourceId);
	}

	public int getCode() {
		return ordinal();
	}

	public String getDbCode() {
		return name();
	}

}
