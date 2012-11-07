package eu.ttbox.velib.ui.map.poi;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MotionEvent;
import eu.ttbox.velib.R;

public class MyPoiOverlay  extends Overlay implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "GeoTrackOverlay";

//    private static final String SQL_SORT_DEFAULT = String.format("%s ASC", GeoTrackColumns.COL_TIME);

    private Context context;
    private final MapController mMapController;
    private final MapView mapView;

    // Constant
    private final int GEOTRACK_LIST_LOADER = R.id.config_id_myPoi_list_loader;

    // Service
    private final SharedPreferences sharedPreferences;
    private final LoaderManager loaderManager; 
    private ScheduledExecutorService runOnFirstFixExecutor = Executors.newSingleThreadScheduledExecutor();

    // Cached
    private Point myScreenCoords = new Point();
    private Point lastScreenCoords = new Point();
    

    // ===========================================================
    // Ui Handler
    // ===========================================================

    private static final int UI_MSG_SET_ADDRESS = 1;

    private Handler uiHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case UI_MSG_SET_ADDRESS:
//                if (balloonView != null) {
//                    String addr = (String) msg.obj;
//                    balloonView.setAddress(addr);
//                }
                break;
            }
        }
    };

    // ===========================================================
    // Constructors
    // ===========================================================

    public MyPoiOverlay(final Context ctx, final MapView mapView, LoaderManager loaderManager ) {
        this(ctx, mapView, new DefaultResourceProxyImpl(ctx), loaderManager );
    }

    public MyPoiOverlay(final Context ctx, final MapView mapView, final ResourceProxy pResourceProxy, LoaderManager loaderManager ) {
        super(pResourceProxy);
        this.context = ctx;
        this.loaderManager = loaderManager;
        this.mapView = mapView;
        this.mMapController = mapView.getController();
    
        // Service
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        enableThreadExecutors();
    }
    


    // ===========================================================
    // Life Cycle
    // ===========================================================

 
    private void onResume() {  
        Log.d(TAG, "##### onResume #### ")  ;
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this); 
        // Load Data
        loaderManager.initLoader(GEOTRACK_LIST_LOADER, null, geoTrackLoaderCallback);
    }
 
    private void onPause() {
        Log.d(TAG, "##### onPause #### ")  ;
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach(final MapView mapView) {
        Log.d(TAG, "##### onDetach #### "  );
        onPause();
        hideBubble();
        super.onDetach(mapView);
    }
    
    
    // ===========================================================
    // Internal Service
    // ===========================================================

    private void disableThreadExecutors() {
        if (runOnFirstFixExecutor != null) {
            runOnFirstFixExecutor.shutdown();
            runOnFirstFixExecutor = null;
        }
    }

    private synchronized void enableThreadExecutors() {
        if (runOnFirstFixExecutor == null) {
            runOnFirstFixExecutor = Executors.newSingleThreadScheduledExecutor();
        }
    }
    
    
    // ===========================================================
    // Accessor
    // ===========================================================

    

    // ===========================================================
    // Listener
    // ===========================================================

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        
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

//    @Override
//    public boolean onSingleTapUp(final MotionEvent event, final MapView mapView) {
//        Projection pj = mapView.getProjection();
//        IGeoPoint p = pj.fromPixels(event.getX(), event.getY());
//
//        // Store whether prior popup was displayed so we can call invalidate() &
//        // remove it if necessary.
//        boolean onHandleEvent = false;
//        boolean isRemovePriorPopup = selectedGeoTrack != null;
//        long idPriorStation = -1;
//        if (isRemovePriorPopup) {
//            idPriorStation = selectedGeoTrack.getId();
//        }
//        // Next test whether a new popup should be displayed
//        selectedGeoTrack = getHitMapLocation(mapView, p);
//        if (isRemovePriorPopup && selectedGeoTrack != null && idPriorStation == selectedGeoTrack.getId()) {
//            selectedGeoTrack = null;
//            hideBubble();
//            onHandleEvent = true;
//        }
//        if (selectedGeoTrack != null) {
//            openBubble(mapView, selectedGeoTrack);
//            onHandleEvent = true;
//        } else if (isRemovePriorPopup) {
//            hideBubble();
//        }
//        // if (isRemovePriorPopup || selectedGeoTrack != null) {
//        // // TODO hideBubble();
//        // mapView.invalidate();
//        // onHandleEvent = true;
//        // }
//        // ?? balloonView
//
//        return onHandleEvent;
//    }
//
//    private GeoTrack getHitMapLocation(MapView mapView, IGeoPoint tapPoint) {
//        // Track which MapLocation was hit...if any
//        GeoTrack hitMapLocation = null;
//        RectF tapPointHitTestRect = new RectF();
//        Point tapPointTestScreenCoords = new Point();
//        int zoonLevel = mapView.getZoomLevel();
//        int selectRadius = zoonLevel + 6;
//        Projection pj = mapView.getProjection();
//        for (GeoTrack testLocation : geoTracks) {
//            // Translate the MapLocation's lat/long coordinates to screen
//            // coordinates
//            pj.toPixels(testLocation.asGeoPoint(), tapPointTestScreenCoords);
//
//            // Create a 'hit' testing Rectangle w/size and coordinates of our
//            // icon
//            // Set the 'hit' testing Rectangle with the size and coordinates of
//            // our on screen icon
//            tapPointHitTestRect.set(-selectRadius, -selectRadius, selectRadius, selectRadius);
//            tapPointHitTestRect.offset(tapPointTestScreenCoords.x, tapPointTestScreenCoords.y);
//
//            // Finally test for a match between our 'hit' Rectangle and the
//            // location clicked by the user
//            pj.toPixels(tapPoint, tapPointTestScreenCoords);
//            if (tapPointHitTestRect.contains(tapPointTestScreenCoords.x, tapPointTestScreenCoords.y)) {
//                hitMapLocation = testLocation;
//                // break;
//            }
//        }
//        return hitMapLocation;
//    }

    // ===========================================================
    // Map Bubble
    // ===========================================================

    private boolean hideBubble() {
        boolean isHide = false;
//        if (balloonView != null && View.GONE != balloonView.getVisibility()) {
//            balloonView.setVisibility(View.GONE);
//            isHide = true;
//        }
        return isHide;
    }
    
//    private boolean openBubble(MapView mapView, GeoTrack geoTrack) {
//        boolean isRecycled = true;
//        if (balloonView == null) {
//            balloonView = new GeoTrackBubble(context);
//            balloonView.setVisibility(View.GONE);
//            balloonView.setDisplayGeoLoc(sharedPreferences.getBoolean(AppConstants.PREFS_KEY_MYLOCATION_DISPLAY_GEOLOC, false));
//            isRecycled = false;
//        }
//        boolean balloonViewNotVisible = (View.VISIBLE != balloonView.getVisibility());
//        if (balloonViewNotVisible) {
//            // Compute Offset
//            int offsetX = 0; // 150
//            int offsetY = -20; // -20
//            // Position Layout
//            balloonViewLayoutParams = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT, MapView.LayoutParams.WRAP_CONTENT, geoTrack.asGeoPoint(), MapView.LayoutParams.BOTTOM_CENTER,
//                    offsetX, offsetY);
//            if (isRecycled) {
//                balloonView.setLayoutParams(balloonViewLayoutParams);
//            } else {
//                mapView.addView(balloonView, balloonViewLayoutParams);
//            }
//            balloonView.setVisibility(View.VISIBLE);
//            // balloonView.setData(lastFix);
//            setBubbleData(geoTrack);
//            return true;
//        } else {
//            return hideBubble();
//        }
//    }
//
//    private void setBubbleData(final GeoTrack geoTrack) {
//        if (balloonView != null && View.VISIBLE == balloonView.getVisibility()) {
//            Log.d(TAG, String.format("setBubbleData for %s", geoTrack));
//            balloonView.setData(person, geoTrack);
//            if (geocodingAuto) {
//                doGeocodingData(geoTrack);
//            }
//        }
//    }

    
    // ===========================================================
    // Loader
    // ===========================================================

    private final LoaderManager.LoaderCallbacks<Cursor> geoTrackLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//            final String personPhone = getPersonPhone();
//            String sortOrder = SQL_SORT_DEFAULT;
//            String selection = null;
//            String[] selectionArgs = null;
//            Uri searchPhoneUri = null;
//            if (isPhoneExactMatch(personPhone)) {
//                searchPhoneUri =  GeoTrackerProvider.Constants.CONTENT_URI;
//                selection = String.format("%s = ? and %2$s >= ? and %2$s < ?", GeoTrackColumns.COL_PHONE, GeoTrackColumns.COL_TIME);
//                selectionArgs = new String[] { getPersonPhone(), timeBeginInMs, timeEndInMs };
//                Log.d(TAG, String.format("Sql request : %s / for param : user [%s] with date range(%s, %s)", selection, selectionArgs[0], selectionArgs[1], selectionArgs[2]));
//            } else {
//                selection = String.format("%1$s >= ? and %1$s < ?",   GeoTrackColumns.COL_TIME);
//                selectionArgs = new String[] {  timeBeginInMs, timeEndInMs };
//                searchPhoneUri = Uri.withAppendedPath(GeoTrackerProvider.Constants.CONTENT_URI_PHONE_FILTER, Uri.encode(personPhone));
//             }
//            // Loader
//            CursorLoader cursorLoader = new CursorLoader(context, searchPhoneUri, null, selection, selectionArgs, sortOrder);
//            return cursorLoader;
        	return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//            int resultCount = cursor.getCount();
//            Log.w(TAG, String.format("### Found %s Geotracks for %s", resultCount, person));
//            ArrayList<GeoTrack> points = new ArrayList<GeoTrack>(resultCount);
//            if (cursor.moveToFirst()) {
//                GeoTrackHelper helper = new GeoTrackHelper().initWrapper(cursor);
//                do {
//                    GeoTrack geoTrack = helper.getEntity(cursor);
//                    // Log.d(TAG, String.format("Cursor : %s", geoTrack));
//                    // Adding to list
//                    points.add(geoTrack);
//                    Log.d(TAG, String.format("Add New GeoTrack : %s", geoTrack));
//                } while (cursor.moveToNext());
//            }
//
//            geoTracks = points;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
//            geoTracks.clear();
        }

    };
    
}
