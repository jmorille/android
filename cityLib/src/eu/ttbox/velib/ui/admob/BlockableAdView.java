package eu.ttbox.velib.ui.admob;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import eu.ttbox.velib.core.AppConstants;

public class BlockableAdView extends AdView implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = "BlockableAdView";
	
	private boolean isAddBlocked = false;
	// ===========================================================
	// Constructors
	// ===========================================================

	public BlockableAdView(Activity activity, AdSize adSize, String adUnitId) {
		super(activity, adSize, adUnitId);
		init();
	}

	public BlockableAdView(Activity activity, AdSize[] adSizes, String adUnitId) {
		super(activity, adSizes, adUnitId);
		init();
	}

	public BlockableAdView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public BlockableAdView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		isAddBlocked = sharedPreferences.getBoolean(AppConstants.PREFS_ADD_BLOCKED, false);
		Log.d(TAG, "Add Blocked : " + isAddBlocked);
		// Register
		 sharedPreferences.registerOnSharedPreferenceChangeListener(this);
	}

	// ===========================================================
	// Preferences
	// ===========================================================

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	    if (key.equals(AppConstants.PREFS_ADD_BLOCKED)) {
	    	  isAddBlocked = sharedPreferences.getBoolean(AppConstants.PREFS_ADD_BLOCKED, false);
	    	  AdRequest adRequest = new AdRequest();
	    	  loadAd(adRequest);
	    } 
	}
	// ===========================================================
	// Add Loader
	// ===========================================================

	@Override
	public void loadAd(AdRequest adRequest) {
		// extremely simple way of determining if we should load an ad!
		// if the donate app is installed, don't show it.
		if (isAddBlocked()) {
			Log.d(TAG, "Stop Loading Add"  );
			this.stopLoading(); 
		} else {
			Log.d(TAG, "Add Loading Request : " + adRequest  );
			super.loadAd(adRequest);
		}
	}

	private boolean isAddBlocked(){
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		boolean isAddBlocked = sharedPreferences.getBoolean(AppConstants.PREFS_ADD_BLOCKED, false);
		return isAddBlocked;
	}



}
