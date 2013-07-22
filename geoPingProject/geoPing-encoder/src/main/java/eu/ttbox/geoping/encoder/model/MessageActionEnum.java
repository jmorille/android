package eu.ttbox.geoping.encoder.model;


import java.util.HashMap;

public enum MessageActionEnum {

    // --- Is consume Slave
    // ----------------------
    GEOPING_REQUEST("WRY", false), //
    ACTION_GEO_PAIRING("PAQ", false), //
    // Remote Controle
    COMMAND_OPEN_APP ("cop", false ), //

    // --- Is consume Master
    // ----------------------
    LOC("LOC"), //
    LOC_DECLARATION("lod" ), //
    ACTION_GEO_PAIRING_RESPONSE("PAR"  ), //
    // Geofence
    GEOFENCE_Unknown_transition("fen"   ), //
    GEOFENCE_ENTER("fei" ), //
    GEOFENCE_EXIT("feo"  ), //
    // Spy Event Notif
    SPY_SHUTDOWN("esd" ), //
    SPY_BOOT("esb" ), //
    SPY_LOW_BATTERY("elb" ), //
    SPY_PHONE_CALL("epc"), //
    SPY_SIM_CHANGE("eps" );

    // ===========================================================
    // Constructor
    // ===========================================================

    private MessageActionEnum(String smsAction  ) {
        this(smsAction, true);
    }

    private MessageActionEnum(String smsAction,  boolean isConsumeMaster ) {
        this.smsAction = smsAction;
        this.intentAction = "eu.ttbox.geoping.SMS_ACTION_" + name();
        this.isConsumeMaster = isConsumeMaster;
    }

    public final String smsAction;
    public final String intentAction;
    public final boolean isConsumeMaster;


    // ===========================================================
    // Conversion Init
    // ===========================================================

    static final HashMap<String, MessageActionEnum> bySmsCodeNames;
    static final HashMap<String, MessageActionEnum> byIntentNames;
    static final HashMap<String, MessageActionEnum> byDbCodes;
    static HashMap<String, MessageActionEnum> byEnumNames;
    static {
        MessageActionEnum[] values = MessageActionEnum.values();
        HashMap<String, MessageActionEnum> smsCodes = new HashMap<String, MessageActionEnum>(values.length);
        HashMap<String, MessageActionEnum> intentNames = new HashMap<String, MessageActionEnum>(values.length);
        HashMap<String, MessageActionEnum> dbCodesNames = new HashMap<String, MessageActionEnum>(values.length);
        HashMap<String, MessageActionEnum> enumNames = new HashMap<String, MessageActionEnum>(values.length);
        for (MessageActionEnum field : values) {
            // Enum name
            enumNames.put(field.name(), field);
            // DbCodes
            dbCodesNames.put(field.name(), field);
            // Sms Code
            addAndCheckUnique(smsCodes, field, field.smsAction, true);
            // intent name
            addAndCheckUnique(intentNames, field, field.intentAction, false);
        }
        // Affect
        byEnumNames = enumNames;
        byDbCodes = dbCodesNames;
        bySmsCodeNames = smsCodes;
        byIntentNames = intentNames;
    }

    private static void addAndCheckUnique(HashMap<String, MessageActionEnum> map, MessageActionEnum field, String key, boolean isManage2LowerKey) {
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
    // Business Accessor
    // ===========================================================

    public String getDbCode() {
        return this.name();
    }

    // ===========================================================
    // Static Accessor
    // ===========================================================
    public static MessageActionEnum getByDbCode(String dbCode) {
        return getByEnumName(dbCode);
    }

    public static MessageActionEnum getByEnumName(String fieldName) {
        return byEnumNames.get(fieldName);
    }

    public static MessageActionEnum getBySmsCode(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        return bySmsCodeNames.get(fieldName);
    }

    public static MessageActionEnum getByIntentName(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        return byIntentNames.get(fieldName);
    }


}
