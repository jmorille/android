package eu.ttbox.geoping.ui.map.track;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;

public class GeoTrackOverlay extends Overlay {

    private static final String TAG = "GeoTrackOverlay";

    private static final String SQL_SORT_DEFAULT = String.format("%s ASC", GeoTrackColumns.COL_TIME);

    private Context context;
    // Listener

    // Config
    private String userId;
    private String timeBegin;
    private String timeEnd;

    // instance
    private List<GeoTrack> geoTracks = new ArrayList<GeoTrack>();;

    public GeoTrackOverlay(Context context, String userId) {
        super();
        this.context = context;
        this.userId = userId;
    }

    public void onResume() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_NEW_GEOTRACK);
        context.registerReceiver(mStatusReceiver, filter);
    }

    public void onPause() {
        context.unregisterReceiver(mStatusReceiver);
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
       Projection p =   mapView.getProjection();
        Path path = new Path();
        Point point = new Point();
        for (GeoTrack geoTrack : geoTracks) {
            GeoPoint geoPoint = geoTrack.asGeoPoint();
            p.toPixels(geoPoint, point) ;
            path.lineTo(point.x, point.y);
        }
        return super.draw(canvas, mapView, shadow, when);
    }

    private final LoaderManager.LoaderCallbacks<Cursor> personLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String sortOrder = SQL_SORT_DEFAULT;
            String selection = String.format("%s = ? and %s >= %s", GeoTrackColumns.COL_USERID, GeoTrackColumns.COL_TIME);
            String[] selectionArgs = new String[] { userId, timeBegin, timeEnd };
            // Loader
            CursorLoader cursorLoader = new CursorLoader(context, GeoTrackerProvider.Constants.CONTENT_URI, null, selection, selectionArgs, sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.d(TAG, "onLoadFinished");
            GeoTrackHelper helper = new GeoTrackHelper().initWrapper(cursor);
            List<GeoTrack> points = new ArrayList<GeoTrack>();
            if (cursor.moveToFirst()) {
                do {
                    GeoTrack geoTrack = helper.getEntity(cursor);
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

    private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive Intent action : " + action);
            if (Intents.ACTION_NEW_GEOTRACK.equals(action)) {
                Bundle extras = intent.getExtras();
                String userId = extras.getString(GeoTrackColumns.COL_USERID);
            }
        }
    };

}
