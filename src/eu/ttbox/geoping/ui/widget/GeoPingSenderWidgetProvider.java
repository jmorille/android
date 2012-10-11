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
public class GeoPingSenderWidgetProvider extends AppWidgetProvider {

    private static final String TAG = "GeoPingSenderWidgetProvider";

    private static final String ACTION_CLICK = "ACTION_CLICK";


    @Override
    public void onReceive(Context ctx, Intent intent) {
        final String action = intent.getAction();
        Log.w(TAG,String.format(  "OnReceive Intent %s : %s" , action, intent));
        if (action.equals(ACTION_CLICK)) {
            final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            final String phoneNumber = intent.getStringExtra(Intents.EXTRA_SMS_PHONE);
            final String formatStr = "Send GeoPing Request : %s"  ;
            Toast.makeText(ctx, String.format(formatStr, phoneNumber), Toast.LENGTH_SHORT).show();
            // Send it
            Intent intentGeoPing = Intents.sendSmsGeoPingRequest(ctx, phoneNumber);
            ctx.startService(intentGeoPing);
        }
        super.onReceive(ctx, intent);
    }
    
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		// Get all ids
//	    String[] projection = new String[] { PersonColumns.COL_NAME, PersonColumns.COL_PHONE};
//	    final ContentResolver r = context.getContentResolver();
//	    Cursor cursor = r.query(PersonProvider.Constants.CONTENT_URI, projection, null, null, PersonColumns.COL_NAME);
 	    
	    ComponentName thisWidget = new ComponentName(context, GeoPingSenderWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
		    // Specify the service to provide data for the collection widget.  Note that we need to
            // embed the appWidgetId via the data otherwise it will be ignored.
            final Intent intent = new Intent(context, WidgetPersonRemoteViewsService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            final RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_geoping_sender);
            rv.setRemoteAdapter(   R.id.widget_person_list, intent);

            // Bind a click listener template for the contents of the weather list.  Note that we
            // need to update the intent's data if we set an extra, since the extras will be
            // ignored otherwise.
            final Intent onClickIntent = new Intent(context, GeoPingSenderWidgetProvider.class);
            onClickIntent.setAction(GeoPingSenderWidgetProvider.ACTION_CLICK);
            onClickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            onClickIntent.setData(Uri.parse(onClickIntent.toUri(Intent.URI_INTENT_SCHEME)));
            final PendingIntent onClickPendingIntent = PendingIntent.getBroadcast(context, 0,
                    onClickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_person_list, onClickPendingIntent);
            
		    // Create some random data
//			int number = (new Random().nextInt(100));
//
//			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_geoping_sender);
//			Log.w("WidgetExample", String.valueOf(number));
//			// Set the text
//			remoteViews.setTextViewText(R.id.update, String.valueOf(number));
//
////			remoteViews.setRemoteAdapter(  android.R.id.list, intent);
//			// Register an onClickListener
//			Intent intentClick = new Intent(context, GeoPingSenderWidgetProvider.class); 
//			intentClick.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//			intentClick.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
//
//			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intentClick, PendingIntent.FLAG_UPDATE_CURRENT);
//			remoteViews.setOnClickPendingIntent(R.id.update, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, rv);
 		}
		// Super
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}

}
