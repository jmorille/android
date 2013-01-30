package eu.ttbox.geoping.ui.prefs.comp.version;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import eu.ttbox.geoping.GeoPingApplication;

public class AppVersionPreference extends Preference {

	
	public static final String PREFS_DEV_MODE = "prefDevMode";


	private static final String TAG = "AppVersionPreference";
	
	
	private OnClickListener devModeOnClickListener = new OnClickListener() {

		int count = 8;

		@Override
		public void onClick(View v) {
			if (count > 0) {

				if (count < 5) {
					String text = "Plus que " + count + " pour être développeur";
					Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
				} 
				count -= 1;
				// Read the Dev Mode status
				if (count == 5) {
					Preference somePreference =	findPreferenceInHierarchy(PREFS_DEV_MODE); 
					boolean isDevMode = somePreference.isEnabled() ; 
					if (isDevMode) {
						count = -1;
					}
				}
			} else if (count == 0) {
				// Define Dev Mode
				Preference somePreference =	findPreferenceInHierarchy(PREFS_DEV_MODE);
				somePreference.setEnabled(true);
				// Show Message
				String text = "Bienvenue en développeur mode";
				Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
				count = -1;
			} else {
				String text = "Vous êtes déja en  développeur mode";
				Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
			}

		}
	};

	// This is the constructor called by the inflater
	public AppVersionPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AppVersionPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onBindView(View view) { 
		GeoPingApplication app = (GeoPingApplication) getContext().getApplicationContext();
		String verion = app.versionPackageName();
		setSummary(verion);  
		view.setOnClickListener(devModeOnClickListener);
		 // Super need to be last
		super.onBindView(view);
	}

}
