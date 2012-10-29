package eu.ttbox.geoping.ui.map.track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
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
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.core.PhoneNumberUtils;
import eu.ttbox.geoping.domain.GeoTrackerProvider;
import eu.ttbox.geoping.domain.core.PhoneDatabaseUtils;
import eu.ttbox.geoping.domain.geotrack.GeoTrackDatabase.GeoTrackColumns;
import eu.ttbox.geoping.domain.geotrack.GeoTrackHelper;
import eu.ttbox.geoping.domain.model.GeoTrack;
import eu.ttbox.geoping.domain.model.Person;
import eu.ttbox.geoping.ui.map.track.bubble.GeoTrackBubble;

public class GeoTrackOverlay extends Overlay implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "GeoTrackOverlay";

    private static final String SQL_SORT_DEFAULT = String.format("%s ASC", GeoTrackColumns.COL_TIME);

    private Context context;
    private final MapController mMapController;
    private final MapView mapView;

    // Constant
    private final int GEOTRACK_LIST_LOADER;// =
                                           // R.id.config_id_geotrack_list_loader;

    // Service
    private final SharedPreferences sharedPreferences;
    private final LoaderManager loaderManager;
    public final Geocoder geocoder;
    private ScheduledExecutorService runOnFirstFixExecutor = Executors.newSingleThreadScheduledExecutor();

    // Listener
    private BroadcastReceiver mStatusReceiver;
    private IntentFilter mStatusReceiverIntentFilter;
    // Config
    private Person person;
    private String timeBeginInMs;
    private String timeEndInMs;
    private boolean geocodingAuto = false;
    // Cached
    private Point myScreenCoords = new Point();
    private Point lastScreenCoords = new Point();

    // Paint
    private Paint mPaint;
    private Paint mGeoPointPaint;
    private Paint mGeoPointOldPaint;
    private Paint mGeoPointAccuracyCirclePaint;
    private Paint mGeoPointAccuracyCirclePaintBorder;

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
                    String addr = (String) msg.obj;
                    balloonView.setAddress(addr);
                }
                break;
            }
        }
    };

    // ===========================================================
    // Constructors
    // ===========================================================

    public GeoTrackOverlay(final Context ctx, final MapView mapView, LoaderManager loaderManager, Person userId, long timeDay, boolean geocodingAuto) {
        this(ctx, mapView, new DefaultResourceProxyImpl(ctx), loaderManager, userId, timeDay, geocodingAuto);
    }

    public GeoTrackOverlay(final Context ctx, final MapView mapView, final ResourceProxy pResourceProxy, LoaderManager loaderManager, Person person, long timeInMs, boolean geocodingAuto) {
        super(pResourceProxy);
        GEOTRACK_LIST_LOADER = R.id.config_id_geotrack_list_loader + (int) person.id + 1000;
        // person.id;
        Log.d(TAG, "#################################");
        Log.d(TAG, "### Create " + person);
        Log.d(TAG, "#################################");
        this.context = ctx;
        this.person = person;
        this.geocodingAuto = geocodingAuto;
        this.loaderManager = loaderManager;
        this.mapView = mapView;
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
        // Listener
        mStatusReceiver = new StatusReceiver();
        try {
            mStatusReceiverIntentFilter = new IntentFilter(Intents.ACTION_NEW_GEOTRACK_INSERTED, GeoTrackerProvider.Constants.ITEM_MIME_TYPE);
        } catch (MalformedMimeTypeException e) {
            Log.e(TAG, "Coud not create Intenfilter for mStatusReceiver : " + e.getMessage());
            e.printStackTrace();
        }

        // Init
        initDirectionPaint(person.color);
        onResume();
    }

    private void onResume() {
        Log.d(TAG, "##### onResume #### " + person);
        // context.getContentResolver().registerContentObserver(GeoTrackerProvider.Constants.CONTENT_URI,
        // true, new MyContentObserver(uiHandler));
        // Prefs
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        // Listener
        context.registerReceiver(mStatusReceiver, mStatusReceiverIntentFilter);
        // Load Data
        loaderManager.initLoader(GEOTRACK_LIST_LOADER, null, geoTrackLoaderCallback);
    }

    private void onPause() {
        Log.d(TAG, "##### onPause #### " + person);
        context.unregisterReceiver(mStatusReceiver);
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDetach(final MapView mapView) {
        Log.d(TAG, "##### onDetach #### " + person);
        onPause();
        hideBubble();
        super.onDetach(mapView);
    }

    private void initDirectionPaint(int c) {
        // Text
        mPaint = new Paint();
        mPaint.setColor(c);
        mPaint.setAlpha(100);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(3);
        // Geo Point
        mGeoPointPaint = new Paint();
        mGeoPointPaint.setAntiAlias(true);
        mGeoPointPaint.setColor(c);
        mGeoPointPaint.setStyle(Style.FILL_AND_STROKE);
        mGeoPointPaint.setStrokeWidth(3);
        // Geo Point
        mGeoPointOldPaint = new Paint(mGeoPointPaint);
        mGeoPointOldPaint.setAlpha(80);

        // Localisation
        mGeoPointAccuracyCirclePaint = new Paint();
        mGeoPointAccuracyCirclePaint.setColor(c);
        mGeoPointAccuracyCirclePaint.setAntiAlias(true);
        mGeoPointAccuracyCirclePaint.setAlpha(50);
        mGeoPointAccuracyCirclePaint.setStyle(Style.FILL);

        mGeoPointAccuracyCirclePaintBorder = new Paint(mGeoPointAccuracyCirclePaint);
        mGeoPointAccuracyCirclePaintBorder.setAlpha(150);
        mGeoPointAccuracyCirclePaintBorder.setStyle(Style.STROKE);
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
            animateToGeoTrack(geoTrack);
        }
    }

    public void animateToGeoTrack(GeoTrack geoTrack) {
        mMapController.animateTo(geoTrack.asGeoPoint());
    }

    private void invalidateMapFor(GeoTrack geoTrack) {
        // TODO for GeoPoint
        mapView.invalidate();
    }

    private void addNewGeoTrack(GeoTrack geoTrack) {
        Log.d(TAG, String.format("Add New GeoTrack : %s", geoTrack));
        geoTracks.add(geoTrack);
        Collections.sort(geoTracks);
        invalidateMapFor(geoTrack);
        animateToGeoTrack(geoTrack);
        // geoTracksPath.reset();
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

        int idx = 0;
        int geoTrackSize = geoTracks.size();
        for (GeoTrack geoTrack : geoTracks) {
            idx++;
            GeoPoint geoPoint = geoTrack.asGeoPoint();
            p.toMapPixels(geoPoint, myScreenCoords);
            // Line Path
            boolean isLast = geoTrackSize == idx;
            if (idx > 1) {
                canvas.drawLine(lastScreenCoords.x, lastScreenCoords.y, myScreenCoords.x, myScreenCoords.y, mPaint);
            }
            // Point
            canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, 4, mGeoPointPaint);
            if (isLast) {
                canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, 8, mGeoPointAccuracyCirclePaintBorder);
                canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, 12, mGeoPointAccuracyCirclePaintBorder);
            }
            // End Loop
            lastScreenCoords.x = myScreenCoords.x;
            lastScreenCoords.y = myScreenCoords.y;
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
            canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, radius, mGeoPointAccuracyCirclePaint);
            canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, radius, mGeoPointAccuracyCirclePaintBorder);
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
                // break;
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
            Log.d(TAG, String.format("setBubbleData for %s", geoTrack));
            balloonView.setData(person, geoTrack);
            if (geocodingAuto) {
                doGeocodingData(geoTrack);
            }
        }
    }

    private void doGeocodingData(final GeoTrack geoTrack) {
        if (geoTrack.address == null || geoTrack.address.length() < 1) {
            final String entityId = geoTrack.getIdAsString();
            Runnable geocoderTask = new Runnable() {

                @Override
                public void run() {
                    try {
                        if (geocoder != null) {
                            // Write Geocoding Text
                            Message msgTemp = uiHandler.obtainMessage(UI_MSG_SET_ADDRESS, "Geocoding...");
                            uiHandler.sendMessage(msgTemp);
                            // Do Geocoding
                            double lat = geoTrack.getLatitude();
                            double lng = geoTrack.getLongitude();
                            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                            if (addresses != null && !addresses.isEmpty()) {
                                final Address addr = addresses.get(0);
                                String addrString = GeoTrackHelper.getAddressAsString(addr);
                                // Display Result
                                Message msg = uiHandler.obtainMessage(UI_MSG_SET_ADDRESS, addrString);
                                uiHandler.sendMessage(msg);
                                // Save
                                if (entityId != null) {
                                    Uri uri = Uri.withAppendedPath(GeoTrackerProvider.Constants.CONTENT_URI, entityId);
                                    ContentValues values = new ContentValues(1);
                                    values.put(GeoTrackColumns.COL_ADDRESS, addrString);
                                    int count = context.getContentResolver().update(uri, values, null, null);
                                    if (count > 0) {
                                        geoTrack.setAddress(addrString);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "MyLocation Geocoder Error : " + e.getMessage());
                    }
                }
            };
            runOnFirstFixExecutor.execute(geocoderTask);
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

    private boolean isPhoneExactMatch( String personPhone) { 
        return AppConstants.LOCAL_DB_KEY.equals(personPhone) || personPhone.startsWith("+");
    }
    
    private boolean isPhoneEquals(String otherPhone) {
        final String personPhone = getPersonPhone();
        boolean isPhoneExactMatch = isPhoneExactMatch(personPhone);
        if (isPhoneExactMatch) {
            return personPhone.equals(otherPhone);
        } else {
            String minMatch = PhoneNumberUtils.toCallerIDMinMatch(personPhone);
            String otherMinMatch = PhoneNumberUtils.toCallerIDMinMatch(otherPhone);
            return minMatch.equals(otherMinMatch);
        }
    }
    private final LoaderManager.LoaderCallbacks<Cursor> geoTrackLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            final String personPhone = getPersonPhone();
            String sortOrder = SQL_SORT_DEFAULT;
            String selection = null;
            String[] selectionArgs = null;
            Uri searchPhoneUri = null;
            if (isPhoneExactMatch(personPhone)) {
                searchPhoneUri =  GeoTrackerProvider.Constants.CONTENT_URI;
                selection = String.format("%s = ? and %2$s >= ? and %2$s < ?", GeoTrackColumns.COL_PHONE, GeoTrackColumns.COL_TIME);
                selectionArgs = new String[] { getPersonPhone(), timeBeginInMs, timeEndInMs };
                Log.d(TAG, String.format("Sql request : %s / for param : user [%s] with date range(%s, %s)", selection, selectionArgs[0], selectionArgs[1], selectionArgs[2]));
            } else {
                selection = String.format("%1$s >= ? and %1$s < ?",   GeoTrackColumns.COL_TIME);
                selectionArgs = new String[] {  timeBeginInMs, timeEndInMs };
                searchPhoneUri = Uri.withAppendedPath(GeoTrackerProvider.Constants.CONTENT_URI_PHONE_FILTER, Uri.encode(personPhone));
             }
            // Loader
            CursorLoader cursorLoader = new CursorLoader(context, searchPhoneUri, null, selection, selectionArgs, sortOrder);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            int resultCount = cursor.getCount();
            Log.w(TAG, String.format("### Found %s Geotracks for %s", resultCount, person));
            ArrayList<GeoTrack> points = new ArrayList<GeoTrack>(resultCount);
            if (cursor.moveToFirst()) {
                GeoTrackHelper helper = new GeoTrackHelper().initWrapper(cursor);
                do {
                    GeoTrack geoTrack = helper.getEntity(cursor);
                    // Log.d(TAG, String.format("Cursor : %s", geoTrack));
                    // Adding to list
                    points.add(geoTrack);
                    Log.d(TAG, String.format("Add New GeoTrack : %s", geoTrack));
                } while (cursor.moveToNext());
            }

            geoTracks = points;
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            geoTracks.clear();
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
            Log.e(TAG, "########################################");
            Log.e(TAG, "### ContentObserver Notify Change for URI : " + uri);
            super.onChange(selfChange);
        }
    }

    private String getPersonPhone() {
        return person.phone;
    }

    private class StatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "StatusReceiver onReceive  action : " + action);
            if (Intents.ACTION_NEW_GEOTRACK_INSERTED.equals(action)) {
                Bundle extras = intent.getExtras();
                String userIdIntent = extras.getString(Intents.EXTRA_SMS_PHONE);
                Log.d(TAG, "StatusReceiver for userId : " + userIdIntent);
                if (isPhoneEquals(userIdIntent)) {
                    // TODO Load Data
                    Uri data = intent.getData();
                    Log.d(TAG, "StatusReceiver Starting loaded data form uri : " + data);
                    GeoTrack addedGeoTrack = loadGeoTrackById(data);
                    Log.d(TAG, String.format("Data loader for Uri %s : %s", data, addedGeoTrack));
                    if (addedGeoTrack != null) {
                        // Add GeoTrack
                        addNewGeoTrack(addedGeoTrack);

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
                String resultUserId = result.phone;
                if (!getPersonPhone().equals(resultUserId)) {
                    Log.w(TAG, String.format("Ignore geoTrack %s for user %s : Current overlay for %s", geoTrackUri, resultUserId, person));
                }
            }
        } finally {
            c.close();
        }
        return result;
    }

}
