package eu.ttbox.velib.ui.about;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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
public class AboutActivity extends Activity {

	private WebView webView;

	public static final String ACTION_VIEW_HTML  = Intent.ACTION_VIEW;
	public static final String ACTION_VIEW_ABOUT = "eu.ttbox.velib.ui.about.ACTION_VIEW_ABOUT";
	public static final String ACTION_VIEW_LICENCE = "eu.ttbox.velib.ui.about.ACTION_VIEW_LICENCE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		webView = (WebView) findViewById(R.id.html_view);
		webView.setWebViewClient(new WebViewClient());
		// Uri path = Uri.parse("android.resource://eu.ttbox.velib/raw/credits.html");
		// Uri path = Uri.parse("android.resource://com.androidbook.samplevideo/raw/credits.html");
		// htmlView.loadUrl("file:///android_asset/credits.html");
		// htmlView.loadUrl(path.getPath());

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
				webView.loadUrl(intent.getData().getPath());
			} else 
			if (ACTION_VIEW_ABOUT.equals(  intent.getAction())) {
				webView.loadData(readTextFromResource(R.raw.credits), "text/html", "utf-8");
 			} else if (ACTION_VIEW_LICENCE.equals(  intent.getAction())) {
				webView.loadData(readTextFromResource(R.raw.licence), "text/html", "utf-8");
 			}
		}
	}

	private String versionName() {
		return ((CityLibApplication) getApplication()).version();
	}

	private String readTextFromResource(int resourceID) {
		InputStream raw = getResources().openRawResource(resourceID);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		int i;
		try {
			i = raw.read();
			while (i != -1) {
				stream.write(i);
				i = raw.read();
			}
			raw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream.toString();
	}

}
