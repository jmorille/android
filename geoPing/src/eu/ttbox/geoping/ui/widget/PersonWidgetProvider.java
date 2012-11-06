package eu.ttbox.geoping.ui.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.RemoteViews;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.core.NotifToasts;
import eu.ttbox.geoping.domain.PersonProvider;

/**
 * {link http://www.vogella.com/articles/AndroidWidgets/article.html}
 */
@TargetApi(14)
public class PersonWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "PersonWidgetProvider";

    private static final String ACTION_CLICK = "ACTION_CLICK";

    private static HandlerThread sWorkerThread;
    private static Handler sWorkerQueue;
    private static PersonWidgetDataProviderObserver sDataObserver;

    public PersonWidgetProvider() {
        // Start the worker thread
        sWorkerThread = new HandlerThread("PersonWidgetProvider-worker");
        sWorkerThread.start();
        sWorkerQueue = new Handler(sWorkerThread.getLooper());
    }

    @Override
    public void onEnabled(Context context) {
        // Register for external updates to the data to trigger an update of the
        // widget. When using
        // content providers, the data is often updated via a background
        // service, or in response to
        // user interaction in the main app. To ensure that the widget always
        // reflects the current
        // state of the data, we must listen for changes and update ourselves
        // accordingly.

        if (sDataObserver == null) {
            final ContentResolver r = context.getContentResolver();
            final AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            final ComponentName cn = new ComponentName(context, PersonWidgetProvider.class);
            sDataObserver = new PersonWidgetDataProviderObserver(mgr, cn, sWorkerQueue);
            r.registerContentObserver(PersonProvider.Constants.CONTENT_URI, true, sDataObserver);
        }
    }

    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();
        Log.w(TAG, String.format("OnReceive Intent %s : %s", action, intent));
        if (action.equals(ACTION_CLICK)) {
            final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            final String phoneNumber = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
             // Send it
            Intent intentGeoPing = Intents.sendSmsGeoPingRequest(ctx, phoneNumber);
            ctx.startService(intentGeoPing);
            // Notif
            NotifToasts.showToastSendGeoPingRequest(ctx, phoneNumber);
         }
        super.onReceive(ctx, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Get all ids
        ComponentName thisWidget = new ComponentName(context, PersonWidgetProvider.class);
        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        for (int widgetId : allWidgetIds) {
            // Specify the service to provide data for the collection widget.
            // Note that we need to
            // embed the appWidgetId via the data otherwise it will be ignored.
            final Intent intent = new Intent(context, PersonWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_person);
            rv.setRemoteAdapter(R.id.widget_person_list, intent);

            // Bind a click listener template for the contents of the weather
            // list. Note that we
            // need to update the intent's data if we set an extra, since the
            // extras will be ignored otherwise.
            final Intent onClickIntent = new Intent(context, PersonWidgetProvider.class);
            onClickIntent.setAction(PersonWidgetProvider.ACTION_CLICK);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0, onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_person_list, onClickPendingIntent);

            // Create some random data
            // int number = (new Random().nextInt(100));
            //
            // RemoteViews remoteViews = new
            // RemoteViews(context.getPackageName(),
            // R.layout.widget_geoping_sender);
            // Log.w("WidgetExample", String.valueOf(number));
            // // Set the text
            // remoteViews.setTextViewText(R.id.update, String.valueOf(number));
            //
            // // remoteViews.setRemoteAdapter( android.R.id.list, intent);
            // // Register an onClickListener
            // Intent intentClick = new Intent(context,
            // GeoPingSenderWidgetProvider.class);
            // intentClick.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            // intentClick.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
            // appWidgetIds);
            //
            // PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
            // 0, intentClick, PendingIntent.FLAG_UPDATE_CURRENT);
            // remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, rv);
        }
        // Super
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    class PersonWidgetDataProviderObserver extends ContentObserver {
        private AppWidgetManager mAppWidgetManager;
        private ComponentName mComponentName;

        PersonWidgetDataProviderObserver(AppWidgetManager mgr, ComponentName cn, Handler h) {
            super(h);
            mAppWidgetManager = mgr;
            mComponentName = cn;
        }

        @Override
        public void onChange(boolean selfChange) {
        	
        	Log.w(TAG, "*******************************************");
        	Log.w(TAG, "*** PersonWidget onChange DataProvider Observer");
            // The data has changed, so notify the widget that the collection
            // view needs to be updated.
            // In response, the factory's onDataSetChanged() will be called
            // which will requery the
            // cursor for the new data.
            mAppWidgetManager.notifyAppWidgetViewDataChanged(mAppWidgetManager.getAppWidgetIds(mComponentName), R.id.widget_person_list);
        }
    }
}
