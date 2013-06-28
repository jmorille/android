package eu.ttbox.velib.ui.about;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import eu.ttbox.velib.CityLibApplication;
import eu.ttbox.velib.R;

/**
 * @see http://developer.android.com/guide/webapps/webview.html
 * @see http://www.monocube.com/2011/02/08/android-tutorial-html-file-in-webview/ URIl @see
 *      http://androidbook.blogspot.fr/2009/08/referring-to-android-resources-using.html
 * 
 */
public class VelibAccountActivity extends Activity {

	private WebView webView;

	public static final String ACTION_VIEW_HTML  = Intent.ACTION_VIEW; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		webView = (WebView) findViewById(R.id.html_view);
		webView.setWebViewClient(new WebViewClient());
		WebSettings webSettings = webView.getSettings();
		webSettings.setJavaScriptEnabled(true);
		webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
 
		final TextView versionView = (TextView) findViewById(R.id.version_view);
		versionView.setText(versionName());

	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntenet(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		handleIntenet(getIntent());
	}

	protected void handleIntenet(Intent intent) {
		if (intent != null) {
			if (ACTION_VIEW_HTML.equals(  intent.getAction())) { 
				String postParam = "Name=%s&Login=%s&Password=%s";
				String url = "https://abo-paris.cyclocity.fr/service/login";
//				String url = "http://www.velib.paris.fr/";
				webView.postUrl(url, postParam.getBytes()); 
			}  
		}
	}

	private String versionName() {
		return ((CityLibApplication) getApplication()).version();
	}
 

}
