package eu.ttbox.geoping.ui.prefs.comp.version;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import eu.ttbox.geoping.GeoPingApplication;


public class AppVersionPreference extends Preference {

	 

	// This is the constructor called by the inflater
	public AppVersionPreference(Context context, AttributeSet attrs) {
		super(context, attrs); 
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		GeoPingApplication app = (GeoPingApplication)  getContext().getApplicationContext();
        setSummary(app.versionPackageName()); 
	}
   

}
