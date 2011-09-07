package eu.ttbox.smstraker;

import android.os.Bundle;
import eu.ttbox.smstraker.activity.AbstractSmsTrackerActivity;

public class SmsTrakerActivity extends AbstractSmsTrackerActivity {
 
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    

}