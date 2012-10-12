package eu.ttbox.geoping.ui.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;

/**
 * {link http://www.vogella.com/articles/AndroidWidgets/article.html}
 * 
 * @author jmorille
 * 
 */
@TargetApi(14)
public class PairingWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "PairingWidgetProvider";

    private static final String ACTION_CLICK = "ACTION_CLICK";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();
        Log.d(TAG, String.format("OnReceive Intent %s : %s", action, intent));
        if (action.equals(ACTION_CLICK)) {
            final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            final String phoneNumber = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            final String formatStr = "Send GeoPing Request : %s";
            Toast.makeText(ctx, String.format(formatStr, phoneNumber), Toast.LENGTH_SHORT).show();
            // Send it
            Intent intentGeoPing = Intents.sendSmsGeoPingResponse(ctx, phoneNumber);
            ctx.startService(intentGeoPing);
        }
        super.onReceive(ctx, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ComponentName thisWidget = new ComponentName(context, PairingWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            // Specify the service to provide data for the collection widget.
            // Note that we need to
            // embed the appWidgetId via the data otherwise it will be ignored.
            final Intent intent = new Intent(context, PersonWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_pairing);
            rv.setRemoteAdapter(R.id.widget_person_list, intent);

            // Bind a click listener template for the contents of the weather
            // list. Note that we
            // need to update the intent's data if we set an extra, since the
            // extras will be ignored otherwise.
            final Intent onClickIntent = new Intent(context, PairingWidgetProvider.class);
            onClickIntent.setAction(PairingWidgetProvider.ACTION_CLICK);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_person_list, onClickPendingIntent);

        
            appWidgetManager.updateAppWidget(widgetId, rv);
        }
        // Super
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

}
