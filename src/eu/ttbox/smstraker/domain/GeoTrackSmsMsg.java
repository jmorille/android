package eu.ttbox.smstraker.domain;

public class GeoTrackSmsMsg {

    public String smsNumber;
    public String action;
    public String body;

    public GeoTrackSmsMsg() {
        super();
    }

    public GeoTrackSmsMsg(String smsNumber, String action, String body) {
        super();
        this.smsNumber = smsNumber;
        this.action = action;
        this.body = body;
    }

}
