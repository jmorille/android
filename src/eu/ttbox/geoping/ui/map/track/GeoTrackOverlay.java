package eu.ttbox.geoping.ui.map.track;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.graphics.Path;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;

public class GeoTrackOverlay extends Overlay implements SharedPreferences.OnSharedPreferenceChangeListener {

	private static final String TAG = "GeoTrackOverlay";

	private static final String SQL_SORT_DEFAULT = String.format("%s ASC", GeoTrackColumns.COL_TIME);

	private Context context;
	// Constant
	private static final int GEOTRACK_LIST_LOADER = R.id.config_id_geotrack_list_loader;

	// Service
	private LoaderManager loaderManager;

	// Listener

	// Config
	private String userId;
	private String timeBeginInMs;
	private String timeEndInMs;

	// Cached
	private Point myScreenCoords = new Point();

	// Paint
	private Paint mPaint;

	// instance
	private List<GeoTrack> geoTracks = new ArrayList<GeoTrack>();;

	// ===========================================================
	// Ui Handler
	// ===========================================================

	private static final int UI_MSG_TOAST_ERROR = 1;

	private Handler uiHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UI_MSG_TOAST_ERROR: {

				break;
			}
			}
		}
	};

	// ===========================================================
	// Business
	// ===========================================================

	private void setDateRange(long timeInMs) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timeInMs);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		// Date To Midnight
		long beginMs = cal.getTimeInMillis();
		this.timeBeginInMs = String.valueOf(beginMs);
		cal.set(Calendar.HOUR_OF_DAY, 24);
		long endMs = cal.getTimeInMillis();
		this.timeEndInMs = String.valueOf(endMs);
		Log.d(TAG, String.format("Range Date %1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS,%1$tL to Now %2$tY-%2$tm-%2$td %2$tH:%2$tM:%2$tS,%2$tL", //
				beginMs, endMs));
	}

	// ===========================================================
	// Constructors
	// ===========================================================

	public GeoTrackOverlay(final Context ctx, final MapView mapView, LoaderManager loaderManager, String userId, long timeDay) {
		this(ctx, mapView, new DefaultResourceProxyImpl(ctx), loaderManager, userId, timeDay);
	}

	public GeoTrackOverlay(final Context ctx, final MapView mapView, final ResourceProxy pResourceProxy, LoaderManager loaderManager, String userId, long timeInMs) {
		super(pResourceProxy);
		this.context = ctx;
		this.userId = userId;
		setDateRange(timeInMs);
		// Service
		this.loaderManager = loaderManager;
		// Init
		initDirectionPaint();
	}

	public void onResume() {
		Log.w(TAG, "##### onResume ####");
		// context.getContentResolver().registerContentObserver(GeoTrackerProvider.Constants.CONTENT_URI,
		// true, new MyContentObserver(uiHandler));
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intents.ACTION_NEW_GEOTRACK_INSERTED);
		// context.registerReceiver(mStatusReceiver, filter);
		// Load Data
		loaderManager.initLoader(GEOTRACK_LIST_LOADER, null, geoTrackLoaderCallback);
	}

	public void onPause() {
		Log.w(TAG, "##### onResume ####");
		// context.unregisterReceiver(mStatusReceiver);
	}

	private void initDirectionPaint() {
		// D
		this.mPaint = new Paint();
		this.mPaint.setColor(Color.RED);
		this.mPaint.setStyle(Style.FILL_AND_STROKE);

	}

	// ===========================================================
	// Listener
	// ===========================================================

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	}

	// ===========================================================
	// Drawing On Map
	// ===========================================================

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		if (shadow) {
			return;
		}
		MapView.Projection p = mapView.getProjection();
		Path path = new Path();
		for (GeoTrack geoTrack : geoTracks) {
			GeoPoint geoPoint = geoTrack.asGeoPoint();
			p.toMapPixels(geoPoint, myScreenCoords);
			// path.addCircle(myScreenCoords.x, myScreenCoords.y, 8,
			// Direction.CCW);
			canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, 8, mPaint);
			// Log.d(TAG, "--------------------------");
			// Log.d(TAG, "geoTrack " + geoTrack);
			// Log.d(TAG, "geoPoint " + geoPoint);
			path.lineTo(myScreenCoords.x, myScreenCoords.y);
		}
		canvas.drawPath(path, mPaint);
	}

	// ===========================================================
	// Loader
	// ===========================================================

	private final LoaderManager.LoaderCallbacks<Cursor> geoTrackLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

		@Override
		public Loader<Cursor> onCreateLoader(int id, Bundle args) {
			Log.d(TAG, "onCreateLoader");
			String sortOrder = SQL_SORT_DEFAULT;
			String selection = String.format("%s = ? and %2$s >= ? and %2$s < ?", GeoTrackColumns.COL_USERID, GeoTrackColumns.COL_TIME);
			String[] selectionArgs = new String[] { userId, timeBeginInMs, timeEndInMs };
			Log.w(TAG, String.format("Prepare Sql Selection : %s / for param : user [%s] with date range(%s, %s)", selection, selectionArgs[0], selectionArgs[1], selectionArgs[2]));
			// selection = null;
			// selectionArgs = null;
			// Loader
			CursorLoader cursorLoader = new CursorLoader(context, GeoTrackerProvider.Constants.CONTENT_URI_GEOTRACKS, null, selection, selectionArgs, sortOrder);
			return cursorLoader;
		}

		@Override
		public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
			int resultCount = cursor.getCount();
			Log.d(TAG, String.format("onLoadFinished with %s results", resultCount));
			GeoTrackHelper helper = new GeoTrackHelper().initWrapper(cursor);
			List<GeoTrack> points = new ArrayList<GeoTrack>(resultCount);
			if (cursor.moveToFirst()) {
				do {
					GeoTrack geoTrack = helper.getEntity(cursor);
					Log.d(TAG, String.format("Cursor : %s", geoTrack));
					// Adding to list
					points.add(geoTrack);
				} while (cursor.moveToNext());
			}
			geoTracks = points;
		}

		@Override
		public void onLoaderReset(Loader<Cursor> loader) {
			geoTracks.clear();
		}

	};

	private class MyContentObserver extends ContentObserver {

		public MyContentObserver(Handler handler) {
			super(handler);
		}

		@Override
		public boolean deliverSelfNotifications() {
			return false;
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			Log.d(TAG, "Notifi Change for URI : " + uri);
			super.onChange(selfChange);
		}
	}

	private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Log.i(TAG, "onReceive Intent action : " + action);
			if (Intents.ACTION_NEW_GEOTRACK_INSERTED.equals(action)) {
				Bundle extras = intent.getExtras();
				String userId = extras.getString(GeoTrackColumns.COL_USERID);
			}
		}

	};

}
