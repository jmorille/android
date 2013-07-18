package eu.ttbox.velib.map.station;

import java.util.ArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.MapView.Projection;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import eu.ttbox.velib.R;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.map.geo.BoundingE6Box;
import eu.ttbox.velib.map.station.bubble.BubbleOverlayView;
import eu.ttbox.velib.map.station.drawable.StationDispoDrawable;
import eu.ttbox.velib.map.station.drawable.StationDispoIcDrawable;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.service.OnStationDispoUpdated;
import eu.ttbox.velib.service.VelibService;

/**
 * @see http 
 *      ://www.java2s.com/Open-Source/Android/UnTagged/veloid/com/xirgonium/
 *      android /veloid/StationNearActivity.java.htm 
 * 
 */
public class StationDispoOverlay extends Overlay implements OnStationDispoUpdated, SharedPreferences.OnSharedPreferenceChangeListener {

    private String TAG = "StationDispoOverlay";

    private Context context;
    private MapView mapView;
    private ArrayList<Station> stations;

    private Handler handler;
    private VelibService velibService;

    private ScheduledThreadPoolExecutor timer;

    SharedPreferences sharedPreferences;

    // Config
    
    // Paint

    private Paint circleUnkwonPaint;
    private Paint circleExpiredPaint;

    // Static values
    private static final long ONE_SECOND_IN_MS = AppConstants.ONE_SECOND_IN_MS;
    private static final long ONE_MINUTE_IN_MS = 60 * 1000;
    private static final long HALF_MINUTE_IN_MS = 30 * 1000;

    // Compute data
    private BoundingE6Box<Station> boundyBox;
    private Station selectedStation;

    // Display
    private Bitmap markerVelib;
    private int markerVelibCenterX;
    private int markerVelibCenterY;
    private BubbleOverlayView<Station> balloonView = null;
    private MapView.LayoutParams balloonViewLayoutParams;
    private Point myScreenCoords = new Point();

    // Bind to shared preference
    private int expectedVelo = 1;
    private long boundyBoxFixDelayInMs = DEFAULT_PREF_BOUNDYBOX_FIX_DELAY_IN_MS;
    private long checkDispoDeltaDelayInMs = DEFAULT_DISPO_DELTA_DELAY_IN_S * ONE_SECOND_IN_MS;
    private long checkDispoBubbleDeltaDelayInMs = DEFAULT_DISPO_BUBLE_DELTA_DELAY_IN_S * ONE_SECOND_IN_MS;

    // TODO
    long stationDispoDataAvaibilityInMs = ONE_MINUTE_IN_MS * 60;

    // Private Default Values
    private static final long DEFAULT_PREF_BOUNDYBOX_FIX_DELAY_IN_MS = 1000;
    private static final long DEFAULT_DISPO_DELTA_DELAY_IN_S = 60 * 5;
    private static final long DEFAULT_DISPO_BUBLE_DELTA_DELAY_IN_S = 60;

    private static final int DEFAULt_KEY_MIN_MAP_ZOOMLEVEL_DETAILS = 15;

    // Config Zoom Level
    private StationDispoDrawable stationDispoDetailView;
    // private StationDispoDrawable stationDispoFavoriteDrawable;
    /** Display Detail if zoomLevel > 16 */
    private int zoomLevelDisplayStationDetail = 15;

    /** Display Station Loc if zoomLevel > 13 */
    private int zoomLevelDisplayStation = 13;

    // Config values
    boolean displayDispoText = true;

    private UpdateStationDispoTimerTask updateStationDispoTimerTask = new UpdateStationDispoTimerTask();
    private ScheduledFuture updateDispoFutur;

    // ===========================================================
    // Preferences
    // ===========================================================

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(TAG, "------------------------------------------------");
        Log.i(TAG, String.format("Preference change for key %s", key));
        Log.i(TAG, "------------------------------------------------");
        // Expected Velo
        expectedVelo = AppConstants.PREFS_KEY_USER_NUMBER_EXPECTED.equals(key) ? sharedPreferences.getInt(key, 2) : expectedVelo;
        stationDispoDetailView.setExpectedVelo(expectedVelo);
        // stationDispoFavoriteDrawable.setExpectedVelo(expectedVelo);
        // Other
        boundyBoxFixDelayInMs = AppConstants.PREFS_KEY_BOUNDYBOX_FIX_DELAY_IN_MS.equals(key) ? sharedPreferences.getLong(key, DEFAULT_PREF_BOUNDYBOX_FIX_DELAY_IN_MS) : boundyBoxFixDelayInMs;
        checkDispoDeltaDelayInMs = AppConstants.PREFS_KEY_CHEK_DISPO_DELTA_DELAY_IN_S.equals(key) ? sharedPreferences.getLong(key, DEFAULT_DISPO_DELTA_DELAY_IN_S) * ONE_SECOND_IN_MS
                : checkDispoDeltaDelayInMs;
        checkDispoBubbleDeltaDelayInMs = AppConstants.PREFS_KEY_CHEK_DISPO_BUBLE_DELTA_DELAY_IN_S.equals(key) ? sharedPreferences.getLong(key, DEFAULT_DISPO_BUBLE_DELTA_DELAY_IN_S) * ONE_SECOND_IN_MS
                : checkDispoBubbleDeltaDelayInMs;
        // Zoom Level
        zoomLevelDisplayStationDetail = AppConstants.PREFS_KEY_MIN_MAP_ZOOMLEVEL_DETAILS.equals(key) ? sharedPreferences.getInt(key, DEFAULt_KEY_MIN_MAP_ZOOMLEVEL_DETAILS)
                : zoomLevelDisplayStationDetail;

    }

    // ===========================================================
    // Constructors
    // ===========================================================

    // Other
    public StationDispoOverlay(Context context, MapView mapView, ArrayList<Station> trackPoints, VelibService velibService, Handler handler, ScheduledThreadPoolExecutor timer) {
        super(context);
        this.context = context;
        this.mapView = mapView;
        this.handler = handler;
        this.timer = timer;
        this.velibService = velibService;
        this.stations = trackPoints;
        // Create points
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        boundyBox = new BoundingE6Box<Station>(trackPoints);
        initVelibTrackOverlay();

        // Read configuration
        this.expectedVelo = sharedPreferences.getInt(AppConstants.PREFS_KEY_USER_NUMBER_EXPECTED, 1);
        this.boundyBoxFixDelayInMs = sharedPreferences.getLong(AppConstants.PREFS_KEY_BOUNDYBOX_FIX_DELAY_IN_MS, DEFAULT_PREF_BOUNDYBOX_FIX_DELAY_IN_MS);
        this.checkDispoDeltaDelayInMs = sharedPreferences.getLong(AppConstants.PREFS_KEY_CHEK_DISPO_DELTA_DELAY_IN_S, DEFAULT_DISPO_DELTA_DELAY_IN_S) * ONE_SECOND_IN_MS;
        this.checkDispoBubbleDeltaDelayInMs = sharedPreferences.getLong(AppConstants.PREFS_KEY_CHEK_DISPO_BUBLE_DELTA_DELAY_IN_S, 30) * ONE_SECOND_IN_MS;
        this.zoomLevelDisplayStationDetail = sharedPreferences.getInt(AppConstants.PREFS_KEY_MIN_MAP_ZOOMLEVEL_DETAILS, DEFAULt_KEY_MIN_MAP_ZOOMLEVEL_DETAILS);
        // register change listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        // Display
        stationDispoDetailView = new StationDispoIcDrawable(context, this.expectedVelo);
        // stationDispoFavoriteDrawable = new
        // StationDispoFavoriteStarDrawable(context, expectedVelo);

        enableTimer();
        // timer.scheduleAtFixedRate(updateStationDispoTimerTask, 0, 500,
        // TimeUnit.MILLISECONDS);
    }

    public synchronized void enableTimer() {
        if (updateDispoFutur == null) {
            updateDispoFutur = timer.scheduleAtFixedRate(updateStationDispoTimerTask, 0, 500, TimeUnit.MILLISECONDS);
        }

    }

    public synchronized void disableTimer() {
        if (updateDispoFutur != null) {
            while (!updateDispoFutur.isCancelled() && !updateDispoFutur.cancel(true)) {
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, "Need to try to remove scheduleAtFixedRate Task");
            }
            updateDispoFutur = null;
        }
        // timer.getQueue().clear();
        // timer.getQueue().remove(updateStationDispoTimerTask);
        // while(!timer.remove(updateStationDispoTimerTask));
        // if (!isRemove) {
        // Log.w(TAG,
        // "Could not remove updateStationDispoTimerTask for disableTimer");
        // }
    }

    public void enableDisplayDispoText() {
        setDisplayDispoText(true);
    }

    public void disableDisplayDispoText() {
        setDisplayDispoText(false);
    }

    private void setDisplayDispoText(boolean enable) {
        this.displayDispoText = enable;
        stationDispoDetailView.setDrawDisplayDispoText(enable);
        // stationDispoFavoriteDrawable.setDisplayDispoText(enable);
    }

    private void initVelibTrackOverlay() {
        Resources r = context.getResources();
        markerVelib = BitmapFactory.decodeResource(r, R.drawable.marker_velib_circle); // android_cycle
        markerVelibCenterX = markerVelib.getWidth() / 2;
        markerVelibCenterY = markerVelib.getHeight() / 2;
        // Unkon values
        circleUnkwonPaint = new Paint();
        circleUnkwonPaint.setAntiAlias(true);
        circleUnkwonPaint.setColor(r.getColor(R.color.station_dispo_unknown));
        // Expired values
        circleExpiredPaint = new Paint();
        circleExpiredPaint.setAntiAlias(true);
        circleExpiredPaint.setColor(Color.BLUE);

    }

    // ===========================================================
    // Accessor
    // ===========================================================

    public void setDrawDisplayCycleParking(boolean drawDisplayCycle, boolean drawDisplayParking) {
        stationDispoDetailView.setDrawDisplayExternalCircle(drawDisplayParking);
        stationDispoDetailView.setDrawDisplayInternalCircle(drawDisplayCycle);
        mapViewInvalidate();
    }

    public boolean isDrawDisplayParking() {
        return stationDispoDetailView.isDrawDisplayExternalCircle();
     }

  

    public boolean isDrawDisplayCycle() {
        return stationDispoDetailView.isDrawDisplayInternalCircle();

    }
 
    
    // ===========================================================
    // Map Draw
    // ===========================================================

    /**
     * @see http ://stackoverflow.com/questions/2176397/drawing-a-line-path-on
     *      -google-maps
     */
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        // if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG,
        // "Ask Draw drawMapLocations");
        if (!shadow) {
            int zoonLevel = mapView.getZoomLevel();
            if (zoonLevel > zoomLevelDisplayStation) {
                // getDisplayMapBoundyBox(canvas, mapView, shadow);
                long nowInMs = System.currentTimeMillis();
                drawMapLocations(canvas, mapView, shadow, zoonLevel, nowInMs);
                // drawInfoWindow(canvas, mapView, shadow, nowInMs);
                drawInfoWindowV2(canvas, mapView, shadow, nowInMs);
            }
        }
        // return super.draw(canvas, mapView, shadow, when);
    }

    // Rect outRect = new Rect();
    //
    // private BoundingE6Box getDisplayMapBoundyBox(MapView mapView, long
    // nowInMs) {
    // mapView.getDrawingRect(outRect);
    // GeoPoint p1 = mapView.getProjection().fromPixels(outRect.left,
    // outRect.top);
    // GeoPoint p2 = mapView.getProjection().fromPixels(outRect.right,
    // outRect.bottom);
    // boundyBox.updateBoundingE6Box(p1, p2, nowInMs);
    // return boundyBox;
    // }

    // ===========================================================
    // Map Event
    // ===========================================================


    /**
     * @see http 
     *      ://code.google.com/p/osmdroid/source/browse/trunk/osmdroid-android
     *      /src /org/andnav/osm/views/overlay/OpenStreetMapViewItemizedOverlay.
     *      java ?r=383
     */
    @Override
    public boolean onSingleTapUp(final MotionEvent event, final MapView mapView) {
        Projection pj = mapView.getProjection();
        IGeoPoint p = pj.fromPixels(event.getX(), event.getY());

        // Store whether prior popup was displayed so we can call invalidate() &
        // remove it if necessary.
        boolean onHandleEvent = false;
        boolean isRemovePriorPopup = selectedStation != null;
        int idPriorStation = -1;
        if (isRemovePriorPopup) {
            idPriorStation = selectedStation.getId();
        }
        // Next test whether a new popup should be displayed
        selectedStation = getHitMapLocation(mapView, p);
        if (isRemovePriorPopup && selectedStation != null && idPriorStation == selectedStation.getId()) {
            selectedStation = null;
            onHandleEvent = true;
        }
        if (isRemovePriorPopup || selectedStation != null) {
            hideBubble();
            mapView.invalidate();
            onHandleEvent = true;
        }
        // Lastly return true if we handled this onTap()
        // return selectedStation != null;
        return onHandleEvent;
    }

    @Override
    public boolean onDoubleTap(final MotionEvent event, final MapView mapView) {
        return super.onDoubleTap(event, mapView);
    }

    /**
     * Test whether an information balloon should be displayed or a prior
     * balloon hidden.
     * 
     * @see http://code.google.com/p/osmdroid/issues/detail?id=11
     */
    private Station getHitMapLocation(MapView mapView, IGeoPoint tapPoint) {
        // mapView.getLatitudeSpan()
        // mapView.getLongitudeSpan()
        // Track which MapLocation was hit...if any
        Station hitMapLocation = null;

        RectF hitTestRecr = new RectF();
        Point screenCoords = new Point();
        int zoonLevel = mapView.getZoomLevel();
        int selectRadius = zoonLevel + 6;
        Projection pj = mapView.getProjection();
        for (Station testLocation : stations) {
            // Translate the MapLocation's lat/long coordinates to screen
            // coordinates
            pj.toPixels(testLocation.asGeoPoint(), screenCoords);

            // Create a 'hit' testing Rectangle w/size and coordinates of our
            // icon
            // Set the 'hit' testing Rectangle with the size and coordinates of
            // our on screen icon
            hitTestRecr.set(-selectRadius, -selectRadius, selectRadius, selectRadius);
            hitTestRecr.offset(screenCoords.x, screenCoords.y);

            // Finally test for a match between our 'hit' Rectangle and the
            // location clicked by the user
            pj.toPixels(tapPoint, screenCoords);
            if (hitTestRecr.contains(screenCoords.x, screenCoords.y)) {
                hitMapLocation = testLocation;
                break;
            }
        }

        // Lastly clear the newMouseSelection as it has now been processed
        tapPoint = null;

        return hitMapLocation;
    }

    private boolean isDisplayStationDetail(Station station, long nowInMs, IMapView mapView) {
        int zoomLevel = mapView.getZoomLevel();
        boolean isSelectedStation = selectedStation != null && (selectedStation.getId() == station.getId());
        boolean isDisplayStationDetail = station.getVeloUpdated() + stationDispoDataAvaibilityInMs > nowInMs;
        if (isDisplayStationDetail) {
            isDisplayStationDetail = zoomLevel > zoomLevelDisplayStationDetail || isSelectedStation || station.isFavory();
        }
        return isDisplayStationDetail;

    }

    private void drawMapLocations(Canvas canvas, MapView mapView, boolean shadow, int zoomLevel, long nowInMs) {
        if (!shadow) {
            // int zoomLevel = mapView.getZoomLevel();
            // Check boundy box
            boundyBox.updateBoundingE6Box(mapView, nowInMs);
            // boolean isBoundyBoxFix = (boundyBox.getLastBoxUpdated() +
            // boundyBoxFixDelayInMs) < nowInMs;
            // Manage stations
            for (Station station : boundyBox.getBoundyBoxStations()) {
                boolean isSelectedStation = selectedStation != null && (selectedStation.getId() == station.getId());
                // Converts lat/lng-Point to OUR coordinates on the screen.
                GeoPoint stationGeoPoint = station.asGeoPoint();
                mapView.getProjection().toPixels(stationGeoPoint, myScreenCoords);
                // Display
                boolean isDisplayStationDetail = isDisplayStationDetail(station, nowInMs, mapView);
                if (isDisplayStationDetail) {
                    // Draw Station Dispo
                    stationDispoDetailView.draw(canvas, zoomLevel, station, myScreenCoords);
                    // Draw Selected Station
                    if (isSelectedStation) {
                        stationDispoDetailView.drawSelected(canvas, zoomLevel, station, myScreenCoords);
                    }
                } else if (zoomLevel > zoomLevelDisplayStationDetail) {
                    canvas.drawBitmap(markerVelib, myScreenCoords.x - markerVelibCenterX, myScreenCoords.y - markerVelibCenterY, circleExpiredPaint);
                 } else if (zoomLevel > zoomLevelDisplayStation) {
                    // Station Loc
                       canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, zoomLevel - 9, circleExpiredPaint);
                } else {
                    canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, 2, circleUnkwonPaint);
                }

            }
            // End It
            // Refresh Station dispos
            // if (zoomLevel > zoomLevelDisplayStationDetail) {
            // isBoundyBoxFix = boundyBox.checkForRefresh(250, nowInMs);
            // if (isBoundyBoxFix) {
            // velibService.updateDispoStationsAsync(checkDispoDeltaDelayInMs,
            // boundyBox.getBoundyBoxStations(), nowInMs);
            // }
            // }
            // else if (zoomLevel > 13) {
            // isBoundyBoxFix = boundyBox.checkForRefresh(200, nowInMs);
            // if (isBoundyBoxFix) {
            // velibService.updateDispoStationsAsync(checkDispoDeltaDelayInMs,
            // boundyBox.getBoundyBoxFavoriteStations(), nowInMs);
            // }
            // }
        }
    }

    @Override
    public void stationDispoUpdated(final Station updatedStaion) {
        // Check zoomLevel to Validate the Update
        boolean isDisplayStationDetail = isDisplayStationDetail(updatedStaion, updatedStaion.getVeloUpdated(), mapView);
        if (isDisplayStationDetail) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Point screenCoords = new Point();
                    int selectRadius = mapView.getZoomLevel() + 12;
                    Rect dirty = new Rect(-selectRadius, -selectRadius, selectRadius, selectRadius);
                    mapView.getProjection().toPixels(updatedStaion.asGeoPoint(), screenCoords);
                    dirty.offset(screenCoords.x, screenCoords.y);
                    mapViewInvalidate(dirty);
                    if (Log.isLoggable(TAG, Log.DEBUG))
                        Log.d(TAG, "mapView invalidate for 1 station");
                }
            });
        }
    }

    private void hideBubble() {
        if (balloonView != null) {
            balloonView.setVisibility(View.GONE);
            mapView.removeView(balloonView);
            balloonViewLayoutParams = null;
        }
    }

    private void drawInfoWindowV2(Canvas canvas, MapView mapView, boolean shadow, long nowInMs) {
        int balloonBottomOffset = 0;
        if (!shadow) {
            if (selectedStation != null) {

                if (balloonView == null) {
                    balloonView = new BubbleOverlayView<Station>(context, velibService, balloonBottomOffset);
                    balloonView.setVisibility(View.GONE);
                    // Todo add click listener
                }
                boolean balloonViewNotVisible = balloonView.getVisibility() != View.VISIBLE;
                if (balloonViewNotVisible) {
                    GeoPoint point = selectedStation.asGeoPoint();
                    // Compute Offset
                    int offsetX = 0; // 150
                    int offsetY = -20; // -20
                    final int halfBubbleWidth = 150;
                    Projection projection = mapView.getProjection();
                    Point bublleLimitPoint = new Point();
                    projection.toMapPixels(point, bublleLimitPoint);
                    Rect screenRect = projection.getScreenRect();
                    int bublleLimitPointX = bublleLimitPoint.x - screenRect.left;
                    // Min bubble
                    // Max Bubble Width
                    int maxBubbleX = bublleLimitPointX + halfBubbleWidth;
                    int maxDeltaBorderX = Math.min(halfBubbleWidth, maxBubbleX - mapView.getRight());
                    if (maxDeltaBorderX > 0) {
                        offsetX = -maxDeltaBorderX;
                    } else {
                        // Min Bubble Width
                        int minBubbleX = bublleLimitPointX - halfBubbleWidth;
                        if (minBubbleX < 0) {
                            offsetX = -minBubbleX;
                        }
                    }
                    // Log.d(TAG, "	mapView.getRight() => " + mapView.getRight()
                    // + "   :::  bublleLimitPoint " + bublleLimitPoint);
                    // Draw Layout

                    // balloonViewLayoutParams.alignment =
                    // MapView.LayoutParams.BOTTOM_CENTER;
                    // balloonViewLayoutParams.mode =
                    // MapView.LayoutParams.MODE_MAP;
                    // Visible
                    balloonView.setData(selectedStation, nowInMs);
                    balloonView.setVisibility(View.VISIBLE);

                    // Prepare Bubble Layout
                    boolean isRecycled = balloonViewLayoutParams!=null;
                    balloonViewLayoutParams = new MapView.LayoutParams(MapView.LayoutParams.WRAP_CONTENT,
                            MapView.LayoutParams.WRAP_CONTENT, point, MapView.LayoutParams.BOTTOM_CENTER,
                            offsetX, offsetY);
                    if (isRecycled) {
                        balloonView.setLayoutParams(balloonViewLayoutParams);
                    } else {
                        mapView.addView(balloonView, balloonViewLayoutParams);
                    }
                }
                // Update Time
                boolean isUpdated = balloonView.updateTime(nowInMs);
                // Ask Refresh Service
                if (balloonViewNotVisible && isUpdated && (selectedStation.getVeloUpdated() + checkDispoBubbleDeltaDelayInMs) < nowInMs) {
                    this.velibService.updateDispoStationsAsyncInPriority(checkDispoBubbleDeltaDelayInMs, selectedStation);
                }
            }
        }
    }

    private void mapViewInvalidate() {
        // mapView.invalidate();
        mapView.postInvalidate();
    }

    private void mapViewInvalidate(Rect dirty) {
        // mapView.invalidate(dirty);
        mapView.postInvalidate(dirty.left, dirty.top, dirty.right, dirty.bottom);
    }

    @Override
    public void stationDispoUpdated(final ArrayList<Station> stations) {
        // Use An handler to use the main thread for update
        // the view
        handler.post(new Runnable() {
            @Override
            public void run() {
                mapViewInvalidate();
                if (Log.isLoggable(TAG, Log.DEBUG))
                    Log.d(TAG, String.format("mapView invalidate for stations count %s", stations.size()));
            }
        });
    }

    public class CustomScheduledExecutor extends ScheduledThreadPoolExecutor {

        public CustomScheduledExecutor(int corePoolSize) {
            super(corePoolSize);
        }

    }

    private class UpdateStationDispoTimerTask implements Runnable {
        int idx = 0;

        @Override
        public void run() {
            // try {
            // if (Log.isLoggable(TAG, Log.DEBUG)) Log.d(TAG,
            // "########################################");
            if (Log.isLoggable(TAG, Log.DEBUG))
                Log.d(TAG, "### Check for Update Station Dispo ### " + idx++);
            int zoomLevel = mapView.getZoomLevel();
            if (zoomLevel > zoomLevelDisplayStationDetail) {
                long nowInMs = System.currentTimeMillis();
                boolean isBoundyBoxFix = boundyBox.isBoundyBoxFix(250, nowInMs);
                if (isBoundyBoxFix) {
                    ArrayList<Station> toDownloadStations = boundyBox.getBoundyBoxStations();
                    velibService.updateDispoStationsAsync(checkDispoDeltaDelayInMs, toDownloadStations, nowInMs);
                }
            }
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + getOuterType().hashCode();
            // result = prime * result + idx;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            UpdateStationDispoTimerTask other = (UpdateStationDispoTimerTask) obj;
            if (!getOuterType().equals(other.getOuterType()))
                return false;
            // if (idx != other.idx)
            // return false;
            return true;
        }

        private StationDispoOverlay getOuterType() {
            return StationDispoOverlay.this;
        }

    }
    // @Override
    // public boolean onTouchEvent(MotionEvent event, MapView mapView) {
    // // ---when user lifts his finger---
    // if (event.getAction() == 1) {
    // // mapView.invalidate();
    // // GeoPoint p = mapView.getProjection().fromPixels((int)
    // // event.getX(), (int) event.getY());
    // // Toast.makeText(getBaseContext(), p.getLatitudeE6() / 1E6 +
    // // "," + p.getLongitudeE6() / 1E6, Toast.LENGTH_SHORT).show();
    // }
    // return false;
    // }

}