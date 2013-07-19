package eu.ttbox.geoping.encoder.model;


public enum MessageActionEnum {

    GEOPING_REQUEST("WRY"), //
    ACTION_GEO_PAIRING("PAQ"), //

    // Master
    LOC("LOC"), //
    LOC_DECLARATION("lod" ), //
    ACTION_GEO_PAIRING_RESPONSE("PAR"  ), //
    // Geofence
    GEOFENCE_Unknown_transition("fen"   ), //
    GEOFENCE_ENTER("fei" ), //
    GEOFENCE_EXIT("feo"  ), //
    // Remote Controle
    COMMAND_OPEN_APP ("cop" ), //
    // Spy Event Notif
    SPY_SHUTDOWN("esd" ), //
    SPY_BOOT("esb" ), //
    SPY_LOW_BATTERY("elb" ), //
    SPY_PHONE_CALL("epc"), //
    SPY_SIM_CHANGE("eps" );

    // ===========================================================
    // Constructor
    // ===========================================================

    private MessageActionEnum(String smsAction ) {
        this.smsAction = smsAction;
    }

    public final String smsAction;


}
