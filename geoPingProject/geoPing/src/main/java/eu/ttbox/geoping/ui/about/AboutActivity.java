package eu.ttbox.geoping.ui.about;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import eu.ttbox.geoping.R;

/**
 * @see http://developer.android.com/guide/webapps/webview.html
 * @see http
 *      ://www.monocube.com/2011/02/08/android-tutorial-html-file-in-webview/
 *      URIl @see
 *      http://androidbook.blogspot.fr/2009/08/referring-to-android-resources
 *      -using.html
 */
public class AboutActivity extends FragmentActivity {

    private AboutFragment aboutFragment;


    // ===========================================================
    // Constructor
    // ===========================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); 
        setContentView(R.layout.about_activity); 
    }

    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof AboutFragment) {
            aboutFragment = (AboutFragment) fragment;
        }
    }

    // ===========================================================
    // Other
    // ===========================================================

    @Override
    protected void onNewIntent(Intent intent) {
        aboutFragment.handleIntent(intent);
    }

}
