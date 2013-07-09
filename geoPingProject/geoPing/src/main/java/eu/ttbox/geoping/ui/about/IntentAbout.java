package eu.ttbox.geoping.ui.about;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.WebView;
import eu.ttbox.geoping.R;

public class IntentAbout {

    private static final String TAG = "IntentAbout";
    
    public static final String ACTION_VIEW_HTML = Intent.ACTION_VIEW;
    public static final String ACTION_VIEW_ABOUT = "eu.ttbox.geoping.ui.about.ACTION_VIEW_ABOUT";
    public static final String ACTION_VIEW_RELEASE_NOTES = "eu.ttbox.geoping.ui.about.ACTION_VIEW_RELEASE_NOTES";
    public static final String ACTION_VIEW_LICENCE = "eu.ttbox.geoping.ui.about.ACTION_VIEW_LICENCE";


    protected static void loadIntentContent(Intent intent, Activity activity, WebView webView ) {
        if (ACTION_VIEW_HTML.equals(intent.getAction())) {
            webView.loadUrl(intent.getData().getPath());
        } else if (ACTION_VIEW_RELEASE_NOTES.equals(intent.getAction())) {
            webView.loadData(readTextFromResource(activity, R.raw.release_notes), "text/html", "utf-8");
            activity.setTitle(R.string.prefs_relasenotes);
        } else if (ACTION_VIEW_ABOUT.equals(intent.getAction())) {
            webView.loadData(readTextFromResource(activity, R.raw.credits), "text/html", "utf-8");
            activity.setTitle(R.string.prefs_about);
        } else if (ACTION_VIEW_LICENCE.equals(intent.getAction())) {
            webView.loadData(readTextFromResource(activity, R.raw.licence), "text/html", "utf-8");
            activity.setTitle(R.string.prefs_license_activity_title);
        }
    }
    
    protected static String readTextFromResource(Context context , int resourceID) {
        InputStream raw = context.getResources().openRawResource(resourceID);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            try {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = raw.read(buffer)) > -1) {
                    stream.write(buffer, 0, len);
                }
                stream.flush();
            } finally {
                raw.close();
                stream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error read TextFromResource [" + resourceID + "] in Raw : " + e.getMessage(), e);
        }
        return stream.toString();
    }
    
}
