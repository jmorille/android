package eu.ttbox.velib.map;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.MotionEvent;
import eu.ttbox.velib.VelibMapActivity;

public class VeloSearchOverlay extends Overlay {

	private Handler handler;
	private VelibMapActivity velibMapViewer;

	private GestureDetector gestureDetector;

	public VeloSearchOverlay(Context context,  VelibMapActivity velibMapViewer, Handler handler) {
		super(context);
		this.velibMapViewer = velibMapViewer;
		this.handler = handler;
		// OnDoubleTapListener;
	}

	private GeoPoint lastGeoPoint;
	private long lastTime;
	private long DELTA_TIME_IN_MS = 1000;

	boolean isDoubleTapActivated = false;

//	@Override
//	public boolean onTap(GeoPoint p, MapView mapView) {
//		long now = System.currentTimeMillis();
//		if (lastGeoPoint != null && (lastTime + DELTA_TIME_IN_MS <= now)) {
//			// DOUBLE TAP
//			isDoubleTapActivated = true;
//		}
//		// Keep last poit
//		this.lastGeoPoint = p;
//		this.lastTime = now;
//
//		return super.onTap(p, mapView);
//	}

	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		float x = e.getX();
		float y = e.getY();
		int action = e.getAction();
		// mapView.set
		switch (action) {

		case MotionEvent.ACTION_DOWN:
			break;
		case MotionEvent.ACTION_UP:
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		}
		return super.onTouchEvent(e, mapView);
	}

	@Override
	protected void draw(Canvas c, MapView osmv, boolean shadow) {
		// TODO Auto-generated method stub
		
	}

}
