package eu.ttbox.geoping.ui.map.track;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
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
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.GeoTrack;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.Person;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.ui.map.track.bubble.GeoTrackBubble;

public class GeoTrackOverlay extends Overlay implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "GeoTrackOverlay";

    private static final String SQL_SORT_DEFAULT = String.format("%s ASC", GeoTrackColumns.COL_TIME);

    private Context context;
    private final MapController mMapController;

    // Constant
    private static final int GEOTRACK_LIST_LOADER = R.id.config_id_geotrack_list_loader;

    // Service
    private final SharedPreferences sharedPreferences;
    private final LoaderManager loaderManager;
    public final Geocoder geocoder;
    private ScheduledExecutorService runOnFirstFixExecutor = Executors.newSingleThreadScheduledExecutor();

    // Listener

    // Config
    private Person person;
    private String timeBeginInMs;
    private String timeEndInMs;

    // Cached
    private Point myScreenCoords = new Point();
    private Path geoTracksPath = new Path();

    // Paint
    private Paint mPaint;
    private Paint mPointPaint;
    private Paint mCirclePaint;
    private Paint mCirclePaintBorder;

    // Bubble
    private GeoTrackBubble balloonView;
    private MapView.LayoutParams balloonViewLayoutParams;

    // instance
    private ArrayList<GeoTrack> geoTracks = new ArrayList<GeoTrack>();;

    private GeoTrack selectedGeoTrack;

    // ===========================================================
    // Ui Handler
    // ===========================================================

    private static final int UI_MSG_SET_ADDRESS = 1;

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case UI_MSG_SET_ADDRESS:
                if (balloonView != null) {
                    Address addr = (Address) msg.obj;
                    balloonView.setAddress(addr);
                }
                break;
            }
        }
    };

    // ===========================================================
    // Constructors
    // ===========================================================

    public GeoTrackOverlay(final Context ctx, final MapView mapView, LoaderManager loaderManager, Person userId, long timeDay) {
        this(ctx, mapView, new DefaultResourceProxyImpl(ctx), loaderManager, userId, timeDay);
    }

    public GeoTrackOverlay(final Context ctx, final MapView mapView, final ResourceProxy pResourceProxy, LoaderManager loaderManager, Person userId, long timeInMs) {
        super(pResourceProxy);
        this.context = ctx;
        this.person = userId;
        this.loaderManager = loaderManager;
        this.mMapController = mapView.getController();
        setDateRange(timeInMs);
        // Service
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        enableThreadExecutors();
        if (Geocoder.isPresent()) {
            this.geocoder = new Geocoder(context, Locale.getDefault());
        } else {
            this.geocoder = null;
            Log.w(TAG, "The Geocoder is not Present");
        }
        // Init
        initDirectionPaint(userId.color);
        onResume();
    }

    public void onResume() {
        Log.w(TAG, "##### onResume ####");
        // context.getContentResolver().registerContentObserver(GeoTrackerProvider.Constants.CONTENT_URI,
        // true, new MyContentObserver(uiHandler));
        // Prefs
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        // Listener
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intents.ACTION_NEW_GEOTRACK_INSERTED);
        context.registerReceiver(mStatusReceiver, filter);
        // Load Data
        loaderManager.initLoader(GEOTRACK_LIST_LOADER, null, geoTrackLoaderCallback);
    }

    public void onPause() {
        Log.w(TAG, "##### onPause ####");
        context.unregisterReceiver(mStatusReceiver);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach(final MapView mapView) {
        onPause();
        super.onDetach(mapView);
    }

    private void initDirectionPaint(int c) {
        // Text
        mPaint = new Paint();
        mPaint.setColor(Color.BLACK);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(3);
        // Point
        mPointPaint = new Paint();
        mPointPaint.setColor(c);
        mPointPaint.setStyle(Style.FILL_AND_STROKE);
        mPointPaint.setStrokeWidth(3);

        // Localisation
        mCirclePaint = new Paint();
        mCirclePaint.setColor(c);
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setAlpha(50);
        mCirclePaint.setStyle(Style.FILL);

        mCirclePaintBorder = new Paint(mCirclePaint);
        mCirclePaintBorder.setAlpha(150);
        mCirclePaintBorder.setStyle(Style.STROKE);
    }

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

    public void animateToLastKnowPosition() {
        int geoTrackSize = geoTracks.size();
        if (geoTrackSize > 0) {
            GeoTrack geoTrack = geoTracks.get(geoTrackSize - 1);
            mMapController.animateTo(geoTrack.asGeoPoint());
        }
    }

    // ===========================================================
    // Accessor
    // ===========================================================

    public void disableThreadExecutors() {
        if (runOnFirstFixExecutor != null) {
            runOnFirstFixExecutor.shutdown();
            runOnFirstFixExecutor = null;
        }
    }

    public synchronized void enableThreadExecutors() {
        if (runOnFirstFixExecutor == null) {
            runOnFirstFixExecutor = Executors.newSingleThreadScheduledExecutor();
        }
    }

    // ===========================================================
    // Listener
    // ===========================================================

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (AppConstants.PREFS_KEY_MYLOCATION_DISPLAY_GEOLOC.equals(key)) {
            boolean displayGeoLoc = sharedPreferences.getBoolean(AppConstants.PREFS_KEY_MYLOCATION_DISPLAY_GEOLOC, true);
            if (balloonView != null) {
                balloonView.setDisplayGeoLoc(displayGeoLoc);
            }
        }
    }

    // ===========================================================
    // Map Drawing
    // ===========================================================

    @Override
    protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
        if (shadow) {
            return;
        }
        MapView.Projection p = mapView.getProjection();
        // Draw Geo Tracks
        // Path geoTracksPath = computePath(mapView, geoTracks);
        // if (geoTracksPath != null) {
        //
        // }
        // Temp Point
        geoTracksPath.rewind();
        int idx = 0;
        for (GeoTrack geoTrack : geoTracks) {
            GeoPoint geoPoint = geoTrack.asGeoPoint();
            p.toMapPixels(geoPoint, myScreenCoords);
            // Line Path
            if (idx == 0) {
                geoTracksPath.moveTo(myScreenCoords.x, myScreenCoords.y);
            } else {
                geoTracksPath.lineTo(myScreenCoords.x, myScreenCoords.y);
            }
            // Point
            canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, 8, mPointPaint);
            // Increment Counter
            idx++;
        }
        if (idx > 1) {
            canvas.drawPath(geoTracksPath, mPaint);
        }
        // Draw Selected
        if (selectedGeoTrack != null) {
            // Select Point
            GeoTrack lastFix = selectedGeoTrack;
            GeoPoint geoPoint = lastFix.asGeoPoint();
            p.toMapPixels(geoPoint, myScreenCoords);
            // Compute Radius Accuracy
            final float groundResolutionInM = lastFix.computeGroundResolutionInMForZoomLevel(mapView.getZoomLevel());
            final float radius = ((float) lastFix.getAccuracy()) / groundResolutionInM;
            canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, radius, mCirclePaint);
            canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, radius, mCirclePaintBorder);
        }
    }

    // ===========================================================
    // Map Motion Event Management
    // ===========================================================

    @Override
    public boolean onTouchEvent(final MotionEvent event, final MapView mapView) {
        // if (event.getAction() == MotionEvent.ACTION_MOVE) {
        // }

        return super.onTouchEvent(event, mapView);
    }

    @Override
    public boolean onSingleTapUp(final MotionEvent event, final MapView mapView) {
        Projection pj = mapView.getProjection();
        IGeoPoint p = pj.fromPixels(event.getX(), event.getY());

        // Store whether prior popup was displayed so we can call invalidate() &
        // remove it if necessary.
        boolean onHandleEvent = false;
        boolean isRemovePriorPopup = selectedGeoTrack != null;
        long idPriorStation = -1;
        if (isRemovePriorPopup) {
            idPriorStation = selectedGeoTrack.getId();
        }
        // Next test whether a new popup should be displayed
        selectedGeoTrack = getHitMapLocation(mapView, p);
        if (isRemovePriorPopup && selectedGeoTrack != null && idPriorStation == selectedGeoTrack.getId()) {
            selectedGeoTrack = null;
            hideBubble();
            onHandleEvent = true;
        }
        if (selectedGeoTrack != null) {
            openBubble(mapView, selectedGeoTrack);
            onHandleEvent = true;
        } else if (isRemovePriorPopup) {
            hideBubble();
        }
        // if (isRemovePriorPopup || selectedGeoTrack != null) {
        // // TODO hideBubble();
        // mapView.invalidate();
        // onHandleEvent = true;
        // }
        // ?? balloonView

        return onHandleEvent;
    }

    private GeoTrack getHitMapLocation(MapView mapView, IGeoPoint tapPoint) {
        // Track which MapLocation was hit...if any
        GeoTrack hitMapLocation = null;
        RectF tapPointHitTestRect = new RectF();
        Point tapPointTestScreenCoords = new Point();
        int zoonLevel = mapView.getZoomLevel();
        int selectRadius = zoonLevel + 6;
        Projection pj = mapView.getProjection();
        for (GeoTrack testLocation : geoTracks) {
            // Translate the MapLocation's lat/long coordinates to screen
            // coordinates
            pj.toPixels(testLocation.asGeoPoint(), tapPointTestScreenCoords);

            // Create a 'hit' testing Rectangle w/size and coordinates of our
            // icon
            // Set the 'hit' testing Rectangle with the size and coordinates of
            // our on screen icon
            tapPointHitTestRect.set(-selectRadius, -selectRadius, selectRadius, selectRadius);
            tapPointHitTestRect.offset(tapPointTestScreenCoords.x, tapPointTestScreenCoords.y);

            // Finally test for a match between our 'hit' Rectangle and the
            // location clicked by the user
            pj.toPixels(tapPoint, tapPointTestScreenCoords);
            if (tapPointHitTestRect.contains(tapPointTestScreenCoords.x, tapPointTestScreenCoords.y)) {
                hitMapLocation = testLocation;
                break;
            }
        }
        return hitMapLocation;
    }

    // ===========================================================
    // Map Bubble
    // ===========================================================

    private boolean openBubble(MapView mapView, GeoTrack geoTrack) {
        boolean isRecycled = true;
        if (balloonView == null) {
            balloonView = new GeoTrackBubble(context);
            balloonView.setVisibility(View.GONE);
            balloonView.setDisplayGeoLoc(sharedPreferences.getBoolean(AppConstants.PREFS_KEY_MYLOCATION_DISPLAY_GEOLOC, false));
            isRecycled = false;
        }
        boolean balloonViewNotVisible = (View.VISIBLE != balloonView.getVisibility());
        if (balloonViewNotVisible) {
            // Compute Offset
            int offsetX = 0; // 150
            int offsetY = -20; // -20
            // Position Layout

            balloonViewLayoutParams = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, geoTrack.asGeoPoint(), MapView.LayoutParams.BOTTOM_CENTER,
                    offsetX, offsetY);
            if (isRecycled) {
                balloonView.setLayoutParams(balloonViewLayoutParams);
            } else {
                mapView.addView(balloonView, balloonViewLayoutParams);
            }
            balloonView.setVisibility(View.VISIBLE);
            // balloonView.setData(lastFix);
            setBubbleData(geoTrack);
            return true;
        } else {
            return hideBubble();
        }
    }

    private void setBubbleData(final GeoTrack geoTrack) {
        if (balloonView != null && View.VISIBLE == balloonView.getVisibility()) {
            balloonView.setData(person, geoTrack);
            // Runnable geocoderTask = new Runnable() {
            //
            // @Override
            // public void run() {
            // try {
            // if (geocoder != null) {
            // double lat = geoTrack.getLatitude();
            // double lng = geoTrack.getLongitude();
            // List<Address> addresses = geocoder.getFromLocation(lat,lng, 1);
            // if (addresses != null && !addresses.isEmpty()) {
            // final Address addr = addresses.get(0);
            // Message msg = uiHandler.obtainMessage(UI_MSG_SET_ADDRESS, addr);
            // uiHandler.sendMessage(msg);
            // }
            // }
            // } catch (IOException e) {
            // Log.e(TAG, "MyLocation Geocoder Error : " + e.getMessage());
            // }
            // }
            // };
            // runOnFirstFixExecutor.execute(geocoderTask);
        }
    }

    private boolean hideBubble() {
        boolean isHide = false;
        if (balloonView != null && View.GONE != balloonView.getVisibility()) {
            balloonView.setVisibility(View.GONE);
            isHide = true;
        }
        return isHide;
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
            String[] selectionArgs = new String[] { getPersonUserId(), timeBeginInMs, timeEndInMs };
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
            Log.d(TAG, String.format("Geotracks onLoadFinished with %s results", resultCount));
            GeoTrackHelper helper = new GeoTrackHelper().initWrapper(cursor);
            ArrayList<GeoTrack> points = new ArrayList<GeoTrack>(resultCount);
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
            geoTracksPath = null;
        }

    };

    // ===========================================================
    // Observer
    // ===========================================================

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

    private String getPersonUserId() {
        return person.phone;
    }

    private BroadcastReceiver mStatusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive Broadcast Intent action : " + action);
            if (Intents.ACTION_NEW_GEOTRACK_INSERTED.equals(action)) {
                Bundle extras = intent.getExtras();
                String userIdIntent = extras.getString(GeoTrackColumns.COL_USERID);
                if (getPersonUserId().equals(userIdIntent)) {
                    // TODO Load
                    Uri data = intent.getData();
                    GeoTrack addedGeoTrack = loadGeoTrackById(data);
                    if (addedGeoTrack != null) {
                        // Add GeoTrack
                        geoTracks.add(addedGeoTrack);
                        geoTracksPath = null;
                    }
                } else {
                    Log.d(TAG, "onReceive Intent action " + Intents.ACTION_NEW_GEOTRACK_INSERTED + " for another User than Mine");
                }
            }
        }

    };

    private GeoTrack loadGeoTrackById(Uri geoTrackUri) {
        GeoTrack result = null;
        Cursor c = context.getContentResolver().query(geoTrackUri, null, null, null, null);
        try {
            // Read value
            if (c != null && c.moveToFirst()) {
                GeoTrackHelper helper = new GeoTrackHelper().initWrapper(c);
                result = helper.getEntity(c);
                // Validate
                String resultUserId = result.userId;
                if (!getPersonUserId().equals(resultUserId)) {
                    Log.w(TAG, String.format("Ignore geoTrack %s for user %s : Current overlay for %s", geoTrackUri, resultUserId, person));
                }
            }
        } finally {
            c.close();
        }
        return result;
    }

}
