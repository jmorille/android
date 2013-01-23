package eu.ttbox.geoping.ui.pairing;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;

public class PairingNotificationFragment extends Fragment {

	private static final String TAG = "PairingNotificationFragment";

	// Instance
	private Uri entityUri;

	// ===========================================================
	// Constructors
	// ===========================================================

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
		View v = inflater.inflate(R.layout.pairing_notification, container, false);
		
		// Load Data
	    loadEntity(getArguments());

				 
		return v;
	}
	

	@Override
	public void onDestroy() { 
		super.onDestroy();
	}
	// ===========================================================
	// Accessor
	// ===========================================================
	
	
	// ===========================================================
	// Loader
	// ===========================================================

	private void loadEntity(Bundle agrs) { 
		if (agrs != null && agrs.containsKey(Intents.EXTRA_DATA_URI)) {
//			getActivity().getSupportLoaderManager().initLoader(PAIRING_EDIT_LOADER, bundle, pairingLoaderCallback);
//			Uri entityId =  Uri.parse( agrs.getString(Intents.EXTRA_DATA_URI)) ;
		}
	}
	 
	

}
