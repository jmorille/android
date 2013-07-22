package eu.ttbox.geoping.ui.widget.pairing;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import eu.ttbox.geoping.R;

/**
 * {link
 * http://android-er.blogspot.fr/2010/10/simple-home-screen-app-widget-with
 * .html}
 * 
 * 
 */
public class PairingWidgetConfig extends Activity {

    private static final String TAG = "PairingWidgetConfig";

    private Button configOkButton;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.widget_pairing_config);

        configOkButton = (Button) findViewById(R.id.okconfig);
        configOkButton.setOnClickListener(configOkButtonOnClickListener);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If they gave us an intent without the widget id, just bail.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    private Button.OnClickListener configOkButtonOnClickListener = new Button.OnClickListener() {

        @Override
        public void onClick(View arg0) {
            // TODO Auto-generated method stub

            final Context context = PairingWidgetConfig.this;

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            // RemoteViews views = new RemoteViews(context.getPackageName(),
            // R.layout.hellowidget_layout);
            // appWidgetManager.updateAppWidget(mAppWidgetId, views);
            PairingWidgetProvider.updateAppWidget(context, appWidgetManager, mAppWidgetId);

            Toast.makeText(context, "PairingWidgetConfig.onClick(): " + String.valueOf(mAppWidgetId), Toast.LENGTH_LONG).show();

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

}
