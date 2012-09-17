package eu.ttbox.geoping.domain;

@Deprecated
public class GeoTrackSmsMsg {

    public String phone;
    public String action;
    public String body;

    public GeoTrackSmsMsg() {
        super();
    }

    public GeoTrackSmsMsg(String smsNumber, String action, String body) {
        super();
        this.phone = smsNumber;
        this.action = action;
        this.body = body;
    }

    @Override
    public String toString() {
        return "GeoTrackSmsMsg [smsNumber=" + phone + ", action=" + action + ", body=" + body + "]";
    }

}
