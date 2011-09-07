package eu.ttbox.smstraker;

import android.os.Binder;

public class GeoTrackingServiceBinder   extends Binder {

	private GeoTrackingService service = null; 
	  
    public GeoTrackingServiceBinder(GeoTrackingService service) { 
        super(); 
        this.service = service; 
    } 
 
    public GeoTrackingService getService(){ 
        return service; 
    } 
    
}
