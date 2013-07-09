package eu.ttbox.geoping.ui.smslog;

import android.content.Context;
import android.graphics.drawable.Drawable;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.model.SmsLogTypeEnum;

/**
 * Created by jmorille on 19/06/13.
 */
public class SmsLogResources {

    public final Drawable incoming;
    public final Drawable outgoing_request;
    public final Drawable outgoing;
    //        public final Drawable missed;
//        public final Drawable voicemail;
    public final Drawable outgoing_delivery_ack;
    public final Drawable outgoing_error;

    public final String actionGeoPingRequest;
    public final String actionGeoPingResponse;
    public final String actionPairingRequest;
    public final String actionPairingResponse;

    public SmsLogResources(Context context) {
        final android.content.res.Resources r = context.getResources();
        incoming = r.getDrawable(R.drawable.ic_call_incoming);
        outgoing_request = r.getDrawable(R.drawable.ic_call_outgoing_request );
        outgoing = r.getDrawable(R.drawable.ic_call_outgoing );
        outgoing_error = r.getDrawable(R.drawable.ic_call_outgoing_error );
        outgoing_delivery_ack = r.getDrawable(R.drawable.ic_call_outgoing_delivery_ack );
//            missed = r.getDrawable(R.drawable.ic_call_missed );
//            voicemail = r.getDrawable(R.drawable.ic_call_voicemail_holo_dark);
        // Text
        actionGeoPingRequest = r.getString(R.string.sms_action_geoping_request);
        actionGeoPingResponse = r.getString(R.string.sms_action_geoping_response);
        actionPairingRequest = r.getString(R.string.sms_action_pairing_request);
        actionPairingResponse = r.getString(R.string.sms_action_pairing_response);
    }

    public Drawable getCallTypeDrawable(SmsLogTypeEnum callType) {
        switch (callType) {
            case RECEIVE:
                return this.incoming;
            case SEND_REQ:
                return this.outgoing_request;
            case SEND_ACK:
                return this.outgoing;
            case SEND_DELIVERY_ACK:
                return this.outgoing_delivery_ack;
            case SEND_ERROR:
                return this.outgoing_error;
            default:
                throw new IllegalArgumentException("invalid call type: " + callType);
        }
    }
}
