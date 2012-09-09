package eu.ttbox.geoping;

import android.os.Bundle;
import eu.ttbox.geoping.ui.AbstractSmsTrackerActivity;

public class SmsTrakerActivity extends AbstractSmsTrackerActivity {
 
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    

}