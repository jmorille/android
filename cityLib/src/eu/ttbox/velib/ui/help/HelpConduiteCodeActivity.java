package eu.ttbox.velib.ui.help;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import eu.ttbox.velib.R;
import eu.ttbox.velib.VelibMapActivity;

public class HelpConduiteCodeActivity extends Fragment	 {

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.help_conduite_code, container, false);
 
		// Button Start Map
		Button startMap01 = (Button) v.findViewById(R.id.help_button1_start_application);
		Button startMap02 = (Button)  v.findViewById(R.id.help_button2_start_application);
		View.OnClickListener startListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent startMap = new Intent(getActivity(), VelibMapActivity.class);
				startActivity(startMap);
			}
		};
		startMap01.setOnClickListener(startListener);
		startMap02.setOnClickListener(startListener);
		
		return v;
	}

}
