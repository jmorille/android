package eu.ttbox.geoping;

import com.google.android.gcm.GCMRegistrar;

import android.os.Bundle;
import android.util.Log;
import eu.ttbox.geoping.service.GCMIntentService;
import eu.ttbox.geoping.ui.AbstractSmsTrackerActivity;

/**
 * TODO {link http://www.e-nature.ch/tech/?tag=android-support-v4}
 * 
 * @author jmorille
 * 
 */
public class SmsTrakerActivity extends AbstractSmsTrackerActivity {

    private static final String TAG = "SmsTrakerActivity";



	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        registerGCM();
    }

    
    
    private void registerGCM() {
    	GCMRegistrar.checkDevice(this);
    	GCMRegistrar.checkManifest(this);
    	final String regId = GCMRegistrar.getRegistrationId(this);
    	if (regId.equals("")) {
    	  GCMRegistrar.register(this, GCMIntentService.SENDER_ID);
    	} else {
    	  Log.v(TAG, "Already registered");
    	}
    }
    
}