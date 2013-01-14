package eu.ttbox.geoping.ui.admob;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.ads.Ad;
import com.google.ads.AdListener;
import com.google.ads.AdRequest.ErrorCode;
import com.google.ads.AdView;

import eu.ttbox.geoping.R;

/**
 * {link https://developers.google.com/mobile-ads-sdk/docs/admob/fundamentals?hl=iw-IL}
  *
 */
public class AdmobFragment extends Fragment  implements AdListener  {
    
    private static final String TAG = "AdmobFragment";
    private AdView adView;
    
    // ===========================================================
    // Constructors
    // ===========================================================
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.admob, container, false);
     // Create the adView
        adView = ( AdView)v.findViewById(R.id.adView);
        adView.setAdListener(this);
        // Request
//        AdRequest request = new AdRequest();
//        request.setLocation(location);
//        adView.loadAd(request);
        return v;
    }

    /**
     * Sent when AdView.loadAd has succeeded.
     */
    @Override
    public void onReceiveAd(Ad ad) { 
    }
    
    /**
     * Sent when loadAd has failed, typically because of network failure, an application configuration error, or a lack of ad inventory. You may wish to log these events for debugging:
     */
    @Override
    public void onFailedToReceiveAd(Ad ad, ErrorCode errorCode) { 
        Log.e(TAG, "failed to receive ad (" + errorCode + ")");
    }

    /**
     * Called when an Activity is created in front of your app, presenting the user with a full-screen ad UI in response to their touching ad.
     */
    @Override
    public void onPresentScreen(Ad ad) { 
    }

    /**
     * Called when the full-screen Activity presented with onPresentScreen has been dismissed and control is returning to your app.
     */
    @Override
    public void onDismissScreen(Ad ad) { 
    }


    /**
     * Called when an Ad touch will launch a new application.
     */
    @Override
    public void onLeaveApplication(Ad ad) { 
    }




}
