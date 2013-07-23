package eu.ttbox.geoping.service.encoder;


import android.content.Context;

import java.util.HashMap;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.encoder.model.MessageActionEnum;

public class MessageActionEnumLabelHelper {

    private static final HashMap<MessageActionEnum, LabelHoder> byMessageActionEnum;

    static {

        LabelHoder[] holders = new LabelHoder[]{
                b(MessageActionEnum.GEOPING_REQUEST, R.string.sms_action_geoping_request), //
                b(MessageActionEnum.ACTION_GEO_PAIRING, R.string.sms_action_pairing_request), //
                // Master
                b(MessageActionEnum.LOC, R.string.sms_action_geoping_response), //
                b(MessageActionEnum.LOC_DECLARATION, R.string.sms_action_geoping_declaration), //
                b(MessageActionEnum.ACTION_GEO_PAIRING_RESPONSE, R.string.sms_action_pairing_response), //
                // Geofence
                b(MessageActionEnum.GEOFENCE_Unknown_transition, R.string.sms_action_geofence_transition_unknown), //
                b(MessageActionEnum.GEOFENCE_ENTER, R.string.sms_action_geofence_transition_enter), //
                b(MessageActionEnum.GEOFENCE_EXIT, R.string.sms_action_geofence_transition_exit), //
                // Remote Controle
                b(MessageActionEnum.COMMAND_OPEN_APP, R.string.sms_action_command_openApp), //
                // Spy Event Notif
                b(MessageActionEnum.SPY_SHUTDOWN, R.string.sms_action_spyevt_shutdown), //
                b(MessageActionEnum.SPY_BOOT, R.string.sms_action_spyevt_boot), //
                b(MessageActionEnum.SPY_LOW_BATTERY, R.string.sms_action_spyevt_low_battery), //
                b(MessageActionEnum.SPY_PHONE_CALL, R.string.sms_action_spyevt_phone_call), //
                b(MessageActionEnum.SPY_SIM_CHANGE, R.string.sms_action_spyevt_sim_change) //
        };
        HashMap<MessageActionEnum, LabelHoder> abyMessageActionEnum = new HashMap<MessageActionEnum, LabelHoder>(holders.length);
        for (LabelHoder holder : holders) {
            final MessageActionEnum key = holder.action;
            if (abyMessageActionEnum.containsKey(key)) {
                throw new IllegalArgumentException(String.format("Duplicated MessageActionEnumLabelHelper Map Key %s", key));
            }
            abyMessageActionEnum.put(key,  holder);
        }
        byMessageActionEnum = abyMessageActionEnum;
    }


    public static String getString(Context context, MessageActionEnum action) {
        LabelHoder holder = byMessageActionEnum.get(action);
        if (holder!=null) {
            return context.getString(holder.labelResourceId);
        }
        return null;
    }

    private static LabelHoder b(MessageActionEnum action, int labelResourceId) {
        return new LabelHoder(action, labelResourceId);
    }

    private static class LabelHoder {
        public final MessageActionEnum action;
        public final int labelResourceId;

        private LabelHoder(MessageActionEnum action, int labelResourceId) {
            this.action = action;
            this.labelResourceId = labelResourceId;
        }
    }

}
