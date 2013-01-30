package eu.ttbox.geoping.ui.admob;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class BlockableAdView extends AdView {

	// ===========================================================
	// Constructors
	// ===========================================================

	public BlockableAdView(Activity activity, AdSize adSize, String adUnitId) {
		super(activity, adSize, adUnitId);
	}

	public BlockableAdView(Activity activity, AdSize[] adSizes, String adUnitId) {
		super(activity, adSizes, adUnitId);
	}

	public BlockableAdView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public BlockableAdView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	// ===========================================================
	// Add Loader
	// ===========================================================

	@Override
	public void loadAd(AdRequest adRequest) {
		// extremely simple way of determining if we should load an ad!
		// if the donate app is installed, don't show it.
		if (isAddBloked(getContext())) {
			this.stopLoading();
		} else {
			super.loadAd(adRequest);
		}
	}

	private boolean isAddBloked(Context context) {
		// TODO read property
		return false;
	}

}
