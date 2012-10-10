package eu.ttbox.geoping.ui.widget;

import java.util.Random;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;

/**
 * {link http://www.vogella.com/articles/AndroidWidgets/article.html}
 * 
 * @author jmorille
 * 
 */
@TargetApi(14)
public class GeoPingSenderWidgetProvider extends AppWidgetProvider {

	private static final String ACTION_CLICK = "ACTION_CLICK";

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
            rv.setRemoteAdapter( widgetId, R.id.widget_person_list, intent);

		    
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
