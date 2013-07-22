package eu.ttbox.geoping.ui.about;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;

public class AboutFragment extends Fragment {

    private static final String TAG = "AboutFragment";

    private WebView webView;

    
    // ===========================================================
    // Constructor
    // ===========================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { 
        Log.d(TAG, "onCreateView");
        View v = inflater.inflate(R.layout.about, container, false);
        // Bindings
        webView = (WebView) v.findViewById(R.id.html_view);
        webView.setWebViewClient(new WebViewClient());
        //
        final TextView versionView = (TextView) v.findViewById(R.id.version_view);
        versionView.setText(versionName());

        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    // ===========================================================
    // Life Cycle
    // ===========================================================

    @Override
    public void onResume() {
        super.onResume();
        handleIntent(getActivity().getIntent());
    }

    // ===========================================================
    // Intent
    // ===========================================================
 

    public void handleIntent(Intent intent) {
        if (intent != null) {
            IntentAbout.loadIntentContent(intent, getActivity(), webView); 
        }
    }

    // ===========================================================
    // Business
    // ===========================================================

    private String versionName() {
        return ((GeoPingApplication) getActivity().getApplication()).version();
    }
 
    
   

   
}
