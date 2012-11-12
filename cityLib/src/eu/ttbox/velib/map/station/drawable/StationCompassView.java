package eu.ttbox.velib.map.station.drawable;

import eu.ttbox.osm.ui.map.mylocation.CompassPictureFactory;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.location.Location;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class StationCompassView extends StationDispoIcView {

    private static final String TAG = "StationDirectionView";

    // Config
    final int northTriangleColor = CompassPictureFactory.DEFAULT_NORTH_TRIANGLE_COLOR;
    final int southTriangleColor = CompassPictureFactory.DEFAULT_SOUTH_TRIANGLE_COLOR;
    final int dotMiddleColor = CompassPictureFactory.DEFAULT_DOT_MIDDLE_COLOR;
    Paint northPaint;
    Paint southPaint;
    Paint centerPaint;
    // Data
    private float displayDensity;
    Location location;
    float bearing = 0;

    // Binding
    Paint mPaint;
    Bitmap compassRose;

    // Cache
    final Matrix mCompassMatrix = new Matrix();
    int COMPASS_ROSE_CENTER_X;
    int COMPASS_ROSE_CENTER_Y;
    int centerX;
    int centerY;

    // ===========================================================
    // Constructors
    // ===========================================================

    public StationCompassView(Context context) {
        super(context);
        initStationDispoView();
    }

    public StationCompassView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StationCompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //
        initStationDispoView();
    }

    private void initStationDispoView() {
        this.mPaint = new Paint();
        displayDensity = getResources().getDisplayMetrics().density;
        // Color
        northPaint = new Paint();
        northPaint.setColor(northTriangleColor);
        northPaint.setAntiAlias(true);
        northPaint.setStyle(Style.FILL);
        northPaint.setAlpha(220);

        // Paint design of south triangle (black)
        southPaint = new Paint();
        southPaint.setColor(southTriangleColor);
        southPaint.setAntiAlias(true);
        southPaint.setStyle(Style.STROKE);
        southPaint.setAlpha(220);

        // Create a little white dot in the middle of the compass rose
        centerPaint = new Paint();
        centerPaint.setColor(dotMiddleColor);
        centerPaint.setAntiAlias(true);
        centerPaint.setStyle(Style.FILL);
        centerPaint.setAlpha(220);

    }

    // ===========================================================
    // Accessor
    // ===========================================================

    public void onLocationChanged(Location location) {
        this.location = location;
    }

    public void setBearing(float bearing) {
        this.bearing = bearing;
        postInvalidate();
        Log.i(TAG, "###### setBearing : " + bearing);
    }

    // ===========================================================
    // Draw
    // ===========================================================

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Compute Draw

    }

    @Override
    public void onSubBackground(Canvas canvas) {
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, String.format(" width = %s  // height = %s", getWidth(), getHeight()));
        }
        mCompassMatrix.setRotate(-bearing, COMPASS_ROSE_CENTER_X, COMPASS_ROSE_CENTER_Y);
        mCompassMatrix.postTranslate(-COMPASS_ROSE_CENTER_X, -COMPASS_ROSE_CENTER_Y);
        mCompassMatrix.postTranslate(centerX, centerY);
        canvas.drawBitmap(compassRose, mCompassMatrix, mPaint);

    }

    /**
     * This is called during layout when the size of this view has changed. If
     * you were just added to the view hierarchy, you're called with the old
     * values of 0.
     * 
     * @param w
     *            Current width of this view.
     * @param h
     *            Current height of this view.
     * @param oldw
     *            Old width of this view.
     * @param oldh
     *            Old height of this view.
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = w / 2;
        centerY = h / 2;
        int radius = Math.min(centerX, centerY);
        compassRose = CompassPictureFactory.createCompassRoseBitmap(radius, displayDensity, northPaint, southPaint, centerPaint);
        COMPASS_ROSE_CENTER_X = compassRose.getWidth() / 2;
        COMPASS_ROSE_CENTER_Y = compassRose.getHeight() / 2;
        super.onSizeChanged(w, h, oldw, oldh);
    }

}
