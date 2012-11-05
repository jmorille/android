package eu.ttbox.geoping.ui.map.mylocation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Picture;

public class CompassPictureFactory {

    public static Picture createCompassRosePicture(int mCompassRadius, final float mScale) {
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
        pathNorth.moveTo(center, center - (mCompassRadius - 3) * mScale);
        pathNorth.lineTo(center + 4 * mScale, center);
        pathNorth.lineTo(center - 4 * mScale, center);
        pathNorth.lineTo(center, center - (mCompassRadius - 3) * mScale);
        pathNorth.close();
        canvas.drawPath(pathNorth, northPaint);

        // Red triangle pointing south
        final Path pathSouth = new Path();
        pathSouth.moveTo(center, center + (mCompassRadius - 3) * mScale);
        pathSouth.lineTo(center + 4 * mScale, center);
        pathSouth.lineTo(center - 4 * mScale, center);
        pathSouth.lineTo(center, center + (mCompassRadius - 3) * mScale);
        pathSouth.close();
        canvas.drawPath(pathSouth, southPaint);

        // Draw a little white dot in the middle
        canvas.drawCircle(center, center, 2, centerPaint);

        mCompassRose.endRecording();
        return mCompassRose;

    }

    public static Bitmap createCompassRoseBitmap(int mCompassRadius, final float mScale) {
        Picture mCompassRose = createCompassRosePicture(mCompassRadius, mScale);
        // Convert As BitMap
        Bitmap bm = Bitmap.createBitmap(mCompassRose.getWidth(), mCompassRose.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        mCompassRose.draw(c);
        return bm;
    }
}
