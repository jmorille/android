package eu.ttbox.velib.map;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class TouchScreen extends LinearLayout {

	private List<PointF> pointsToDraw = new ArrayList<PointF>();

	private Paint touchPaint;

	public TouchScreen(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public TouchScreen(Context context) {
		super(context);
		init();
	}

	private void init() {
		touchPaint = new Paint();
		touchPaint.setARGB(255, 0, 0, 255);
		touchPaint.setAntiAlias(true);
		touchPaint.setStyle(Style.STROKE);
		touchPaint.setStrokeWidth(2);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		pointsToDraw.add(new PointF(event.getX(), event.getY()));
		invalidate();
		return true;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.dispatchDraw(canvas);

		Iterator<PointF> iterator = pointsToDraw.iterator();
		while (iterator.hasNext()) {
			PointF p = iterator.next();
			canvas.drawCircle(p.x, p.y, 5, touchPaint);
		}
	}
}
