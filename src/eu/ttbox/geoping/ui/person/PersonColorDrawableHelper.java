package eu.ttbox.geoping.ui.person;

import android.graphics.AvoidXfermode.Mode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;

public class PersonColorDrawableHelper {

    
    public static Drawable getListBackgroundColor(int color) {
        int colorBackground = Color.TRANSPARENT;
        int colorFocus = Color.argb(50, Color.red(color), Color.green(color), Color.blue(color));
        RoundRectShape rs = new RoundRectShape(new float[] { 10, 10, 10, 10, 10, 10, 10, 10 }, null, null);
        ShapeDrawable sdOff = new BorderShapeDrawable(rs, colorBackground, color, 10);
        ShapeDrawable sdOn = new BorderShapeDrawable(rs, colorBackground, colorFocus, 10);

        StateListDrawable stld = new StateListDrawable();
        stld.addState(new int[] { android.R.attr.state_enabled }, sdOff);
        stld.addState(new int[] { android.R.attr.state_pressed }, sdOn);
        return stld;
    }

    public static Drawable getBubbleBackgroundColor(int color) {
        RoundRectShape rs = new RoundRectShape(new float[] { 20, 20, 20, 20, 20, 20, 20, 20 }, null, null);
        BubbleBackgroudShapeDrawable sdOff = new BubbleBackgroudShapeDrawable(rs, color, color, 2);
        return sdOff;
    }
    
    /**
     * {link http://www.betaful.com/2012/01/programmatic-shapes-in-android/}
     * 
     * @author jmorille
     * 
     */
    public static class BorderShapeDrawable extends ShapeDrawable {
        Paint fillpaint;
        Paint strokepaint;
        private static final int WIDTH = 5;
        private int strokeWidth;

        public BorderShapeDrawable(Shape s, int fill, int stroke, int strokeWidth) {
            super(s);
            this.strokeWidth = strokeWidth;
            fillpaint = new Paint(this.getPaint());
            fillpaint.setColor(fill);
            strokepaint = new Paint(fillpaint);
            strokepaint.setStyle(Paint.Style.STROKE);
            strokepaint.setStrokeWidth(strokeWidth);
            strokepaint.setColor(stroke);
        }

        protected void onDraw(Shape shape, Canvas canvas, Paint paint) {
            // V1
            shape.draw(canvas, fillpaint);
            shape.draw(canvas, strokepaint);

            // V2
            // shape.resize(canvas.getClipBounds().right,
            // canvas.getClipBounds().bottom);
            // shape.draw(canvas, fillpaint);
            //
            // Matrix matrix = new Matrix();
            // matrix.setRectToRect(new RectF(0, 0,
            // canvas.getClipBounds().right, canvas.getClipBounds().bottom), new
            // RectF(strokeWidth / 2, strokeWidth / 2,
            // canvas.getClipBounds().right - strokeWidth
            // / 2, canvas.getClipBounds().bottom - strokeWidth / 2),
            // Matrix.ScaleToFit.FILL);
            // canvas.concat(matrix);
            //
            // shape.draw(canvas, strokepaint);
        }

        public void setFillColour(int c) {
            fillpaint.setColor(c);
        }

        public void setStrokeColour(int c) {
            strokepaint.setColor(c);
        }

    }
    
    public static class BubbleBackgroudShapeDrawable extends ShapeDrawable {
        Paint fillpaint;
        Paint strokepaint;
         private int strokeWidth;

        public BubbleBackgroudShapeDrawable(Shape s,final int fill, int stroke, int strokeWidth) {
            super(s);
            this.strokeWidth = strokeWidth;
            fillpaint =  this.getPaint();
//            fillpaint.setColor(fill);
            final Rect r = getBounds();
//            fillpaint.setShader(new LinearGradient(0, r.width(), 0, 0,  fill, Color.WHITE, Shader.TileMode.MIRROR));
            ShapeDrawable.ShaderFactory sf = new ShapeDrawable.ShaderFactory() {
                @Override
                public Shader resize(int width, int height) { 
                    LinearGradient lg =   new LinearGradient(0, width, 0, 0,  Color.WHITE,  fill, Shader.TileMode.REPEAT);
                    return lg;
                }
            };
            setShaderFactory(sf);
            strokepaint = new Paint(fillpaint);
            strokepaint.setStyle(Paint.Style.STROKE);
            strokepaint.setStrokeWidth(strokeWidth);
            strokepaint.setColor(stroke);
        }

        protected void onDraw(Shape shape, Canvas canvas, Paint paint) {
            // V1
            shape.draw(canvas, fillpaint);
            shape.draw(canvas, strokepaint);

            // V2
            // shape.resize(canvas.getClipBounds().right,
            // canvas.getClipBounds().bottom);
            // shape.draw(canvas, fillpaint);
            //
            // Matrix matrix = new Matrix();
            // matrix.setRectToRect(new RectF(0, 0,
            // canvas.getClipBounds().right, canvas.getClipBounds().bottom), new
            // RectF(strokeWidth / 2, strokeWidth / 2,
            // canvas.getClipBounds().right - strokeWidth
            // / 2, canvas.getClipBounds().bottom - strokeWidth / 2),
            // Matrix.ScaleToFit.FILL);
            // canvas.concat(matrix);
            //
            // shape.draw(canvas, strokepaint);
        }

        public void setFillColour(int c) {
            fillpaint.setColor(c);
        }

        public void setStrokeColour(int c) {
            strokepaint.setColor(c);
        }

    }
    
    
}
