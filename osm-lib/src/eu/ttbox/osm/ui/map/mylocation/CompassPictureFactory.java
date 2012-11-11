package eu.ttbox.osm.ui.map.mylocation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Picture;

public class CompassPictureFactory {

    public static Picture createCompassRosePicture(final int mCompassRadius, final float displayDensity) {
        // Paint design of north triangle (it's common to paint north in red
        // color)
        final Paint northPaint = new Paint();
        northPaint.setColor(0xFFA00000);
        northPaint.setAntiAlias(true);
        northPaint.setStyle(Style.FILL);
        northPaint.setAlpha(220);

        // Paint design of south triangle (black)
        final Paint southPaint = new Paint();
        southPaint.setColor(Color.BLACK);
        southPaint.setAntiAlias(true);
        southPaint.setStyle(Style.FILL);
        southPaint.setAlpha(220);

        // Create a little white dot in the middle of the compass rose
        final Paint centerPaint = new Paint();
        centerPaint.setColor(Color.WHITE);
        centerPaint.setAntiAlias(true);
        centerPaint.setStyle(Style.FILL);
        centerPaint.setAlpha(220);

        // final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2 *
        // mScale);
        final int picBorderWidthAndHeight = (int) ((mCompassRadius + 5) * 2);
        final int center = picBorderWidthAndHeight / 2;

        // Record Rose
        Picture mCompassRose = new Picture();
        final Canvas canvas = mCompassRose.beginRecording(picBorderWidthAndHeight, picBorderWidthAndHeight);

        // Blue triangle pointing north
        final Path pathNorth = new Path();
        pathNorth.moveTo(center, center - (mCompassRadius - 3) * displayDensity);
        pathNorth.lineTo(center + 4 * displayDensity, center);
        pathNorth.lineTo(center - 4 * displayDensity, center);
        pathNorth.lineTo(center, center - (mCompassRadius - 3) * displayDensity);
        pathNorth.close();
        canvas.drawPath(pathNorth, northPaint);

        // Red triangle pointing south
        final Path pathSouth = new Path();
        pathSouth.moveTo(center, center + (mCompassRadius - 3) * displayDensity);
        pathSouth.lineTo(center + 4 * displayDensity, center);
        pathSouth.lineTo(center - 4 * displayDensity, center);
        pathSouth.lineTo(center, center + (mCompassRadius - 3) * displayDensity);
        pathSouth.close();
        canvas.drawPath(pathSouth, southPaint);

        // Draw a little white dot in the middle
        canvas.drawCircle(center, center, 2, centerPaint);

        mCompassRose.endRecording();
        return mCompassRose;

    }

    /**
     * 
     * @param mCompassRadius
     * @param displayDensity  ctx.getResources().getDisplayMetrics().density;
     * @return
     */
    public static Bitmap createCompassRoseBitmap(int mCompassRadius, final float displayDensity) {
        Picture mCompassRose = createCompassRosePicture(mCompassRadius, displayDensity);
        // Convert As BitMap
        Bitmap bm = Bitmap.createBitmap(mCompassRose.getWidth(), mCompassRose.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        mCompassRose.draw(c);
        return bm;
    }
}
