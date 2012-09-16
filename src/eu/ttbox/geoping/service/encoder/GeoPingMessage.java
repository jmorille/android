package eu.ttbox.geoping.service.encoder;

import android.os.Bundle;

public class GeoPingMessage {

    public String phone;
    public String action;
    public Bundle params;
    
    
    public GeoPingMessage() {
        super(); 
    }


    public GeoPingMessage(String phone, String action, Bundle params) {
        super();
        this.phone = phone;
        this.action = action;
        this.params = params;
    }
    
    
}
