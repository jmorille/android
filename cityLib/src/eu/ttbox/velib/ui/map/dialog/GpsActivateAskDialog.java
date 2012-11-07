package eu.ttbox.velib.ui.map.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.provider.Settings;
import eu.ttbox.velib.R;

/**
 * All Setting @see http://developer.android.com/reference/android/provider/Settings.html
 * @author jmorille
 *
 */
public class GpsActivateAskDialog extends AlertDialog implements OnClickListener {
 
	 
	public GpsActivateAskDialog(Context context ) {
		this(context, 0 );
	}

	public GpsActivateAskDialog(Context context, int theme ) {
		super(context, theme); 
		// Init
		Context themeContext = getContext();
		setTitle(R.string.ask_activate_gps);
		setCancelable(true);
		setIcon(0);
		setCanceledOnTouchOutside(true);
		setButton(BUTTON_POSITIVE, themeContext.getText(R.string.yes), this);
		setButton(BUTTON_NEGATIVE, themeContext.getText(R.string.no), (OnClickListener) null);
 
	}
	
	@Override
	public void onClick(DialogInterface dialog, int which) {
		Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
		getContext().startActivity(intent);
	}
	
	
}
