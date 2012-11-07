package eu.ttbox.velib.ui.help;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import eu.ttbox.velib.R;
import eu.ttbox.velib.VelibMapActivity;

public class HelpConduiteCodeActivity extends Activity {

	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help_conduite_code);
		// Button Start Map
		Button startMap01 = (Button) findViewById(R.id.help_button1_start_application);
		Button startMap02 = (Button) findViewById(R.id.help_button2_start_application);
		View.OnClickListener startListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent startMap = new Intent(getApplicationContext(), VelibMapActivity.class);
				startActivity(startMap);
			}
		};
		startMap01.setOnClickListener(startListener);
		startMap02.setOnClickListener(startListener);
	}

}
