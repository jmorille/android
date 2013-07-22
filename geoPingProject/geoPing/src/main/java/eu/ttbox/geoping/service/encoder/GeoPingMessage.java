package eu.ttbox.geoping.service.encoder;

import java.util.ArrayList;

import android.os.Bundle;


@Deprecated
public class GeoPingMessage {

    public String phone;
    public SmsMessageActionEnum action;
    public Bundle params;
    public String encodedParams;

    // Next Process Message
    public int nextStartIdx;
    public ArrayList<GeoPingMessage> multiMessages;
    
    public GeoPingMessage() {
        super();
    }

    public GeoPingMessage(String phone, SmsMessageActionEnum action, Bundle params) {
        super();
        this.phone = phone;
        this.action = action;
        this.params = params;
    }
    
    public void addMultiMessage(GeoPingMessage msg) {
        if (msg==null) {
            return;
        }
        if (multiMessages==null) {
           this.multiMessages = new ArrayList<GeoPingMessage>();
        }
        this.multiMessages.add(msg);
    }

    public boolean isMultiMessages() {
		return multiMessages!=null && !multiMessages.isEmpty();
	}

	@Override
    public String toString() {
        return "GeoPingMessage [phone=" + phone + ", action=" + action + ", params=" + params + "]";
    }

}
