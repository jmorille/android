package eu.ttbox.geoping.service.encoder;

import java.util.HashMap;

import android.content.res.Resources;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.service.master.GeoPingMasterService;
import eu.ttbox.geoping.service.slave.GeoPingSlaveService;

public enum SmsMessageActionEnum {

	// Slave
	// GEOPING_REQUEST_AS_GMAP("GMap",  Intents.ACTION_SMS_GEOPING_REQUEST_HANDLER, GeoPingSlaveService.class, false, R.string.sms_action_geoping_request), //
	GEOPING_REQUEST("WRY", Intents.ACTION_SMS_GEOPING_REQUEST_HANDLER, GeoPingSlaveService.class, false, R.string.sms_action_geoping_request), //
	ACTION_GEO_PAIRING("PAQ", Intents.ACTION_SMS_PAIRING_RESQUEST, GeoPingSlaveService.class, false, R.string.sms_action_pairing_request), //

	// Master
	LOC("LOC", Intents.ACTION_SMS_GEOPING_RESPONSE_HANDLER, GeoPingMasterService.class, true, R.string.sms_action_geoping_response), //
	LOC_DECLARATION("lod", Intents.ACTION_SMS_GEOPING_DECLARATION_HANDLER, GeoPingMasterService.class, true, R.string.sms_action_geoping_declaration), //
	ACTION_GEO_PAIRING_RESPONSE("PAR", Intents.ACTION_SMS_PAIRING_RESPONSE, GeoPingMasterService.class, true, R.string.sms_action_pairing_response), //
    // Geofence
    GEOFENCE_Unknown_transition("fen", Intents.ACTION_SMS_GEOFENCE_RESPONSE_HANDLER, GeoPingMasterService.class, true, R.string.sms_action_geofence_transition_unknown ), //
    GEOFENCE_ENTER("fei", Intents.ACTION_SMS_GEOFENCE_ENTER_RESPONSE_HANDLER, GeoPingMasterService.class, true, R.string.sms_action_geofence_transition_enter ), //
    GEOFENCE_EXIT("feo", Intents.ACTION_SMS_GEOFENCE_EXIT_RESPONSE_HANDLER, GeoPingMasterService.class, true, R.string.sms_action_geofence_transition_exit ), //

	// Spy Event Notif
	SPY_SHUTDOWN("esd", Intents.ACTION_SMS_EVTSPY_SHUTDOWN, GeoPingMasterService.class, true, R.string.sms_action_spyevt_shutdown), //
	SPY_BOOT("esb", Intents.ACTION_SMS_EVTSPY_BOOT, GeoPingMasterService.class, true, R.string.sms_action_spyevt_boot), //
	SPY_LOW_BATTERY("elb", Intents.ACTION_SMS_EVTSPY_LOW_BATTERY, GeoPingMasterService.class, true, R.string.sms_action_spyevt_low_battery), //
	SPY_PHONE_CALL("epc", Intents.ACTION_SMS_EVTSPY_PHONE_CALL, GeoPingMasterService.class, true, R.string.sms_action_spyevt_phone_call), //
	SPY_SIM_CHANGE("eps", Intents.ACTION_SMS_EVTSPY_SIM_CHANGE, GeoPingMasterService.class, true, R.string.sms_action_spyevt_sim_change);

	// ===========================================================
	// Constructor
	// ===========================================================

	private SmsMessageActionEnum(String smsAction, String intentAction, Class<?> cls, boolean isMaster, int labelStringId) {
		this.intentAction = intentAction;
		this.smsAction = smsAction;
		this.serviceClass = cls;
		this.isMasterConsume = isMaster;
		this.labelResourceId = labelStringId;
	}

	public final String intentAction;
	public final String smsAction;
	public final Class<?> serviceClass;
	public final boolean isMasterConsume;

	public final int labelResourceId;

	// ===========================================================
	// Conversion Init
	// ===========================================================

	static final HashMap<String, SmsMessageActionEnum> bySmsCodeNames;
	static final HashMap<String, SmsMessageActionEnum> byIntentNames;
	static final HashMap<String, SmsMessageActionEnum> byDbCodes;

	static {
		SmsMessageActionEnum[] values = SmsMessageActionEnum.values();
		HashMap<String, SmsMessageActionEnum> smsCodes = new HashMap<String, SmsMessageActionEnum>(values.length);
		HashMap<String, SmsMessageActionEnum> intentNames = new HashMap<String, SmsMessageActionEnum>(values.length);
		HashMap<String, SmsMessageActionEnum> dbCodesNames = new HashMap<String, SmsMessageActionEnum>(values.length);
		for (SmsMessageActionEnum field : values) {
			// DbCodes
			dbCodesNames.put(field.name(), field);
			// Sms Code
			addAndCheckUnique(smsCodes, field, field.smsAction, true);
			// intent name
			addAndCheckUnique(intentNames, field, field.intentAction, false);
		}
		// Affect
		byDbCodes = dbCodesNames;
		bySmsCodeNames = smsCodes;
		byIntentNames = intentNames;
	}

	private static void addAndCheckUnique(HashMap<String, SmsMessageActionEnum> map, SmsMessageActionEnum field, String key, boolean isManage2LowerKey) {
		if (map.containsKey(key)) {
			throw new IllegalArgumentException(String.format("Duplicated Key %s", key));
		}
		map.put(key, field);
		// Compatibility for version Lower than 0.1.5 (37)
		// COuld suppress this code if no v37
		if (isManage2LowerKey) {
			String lowerKey = key.toLowerCase();
			if (!key.equals(lowerKey)) {
				map.put(lowerKey, field);
			}
		}
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
		return byDbCodes.get(dbCode);
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
