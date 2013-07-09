package eu.ttbox.velib.map.provider;

import java.util.ArrayList;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import eu.ttbox.velib.map.geo.BoundingE6Box;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.VelibService;

/**
 * @see http ://chrisblunt.com/2010/08/12/android-map-view-double-taps-and-overlay -markers/
 * 
 */
public class VeloProviderItemizedOverlay extends ItemizedIconOverlay<VeloProviderOverlayItem> {

	private Context context;
	private VelibService velibService;

	// private ArrayList<VeloProviderOverlayItem> items = new ArrayList<VeloProviderOverlayItem>();

	private SharedPreferences sharedPreferences;

	private int zoomLimit = 13;
	private BoundingE6Box<VelibProvider> boundyBox;

	private Paint paint;

	public VeloProviderItemizedOverlay(Context aContext, Drawable marker, VelibService velibService) {
		// super(boundCenterBottom(marker));
		super(initListItems(), marker, new VeloProviderOnItemGestureListener(aContext, velibService), new DefaultResourceProxyImpl(aContext));
		this.velibService = velibService;
		this.context = aContext;
		// Init Service
		this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		initialiseOverlays();
	}

	private void initialiseOverlays() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setColor(Color.BLUE);
		paint.setAlpha(20);
		// Init datas
		// ArrayList<VelibProvider> velibs = initListItems();
		// init boundy box
		ArrayList<VelibProvider> velibs = new ArrayList<VelibProvider>(VelibProvider.getVelibProviders().length);
		for (VelibProvider velibProvider : VelibProvider.getVelibProviders()) {
			// Manage
			if (velibProvider.getBoundyBoxE6() != null) {
				velibs.add(velibProvider);
			}
		}
		boundyBox = new BoundingE6Box<VelibProvider>(velibs);
		populate();
	}

	private static ArrayList<VeloProviderOverlayItem> initListItems() {
		// Init datas
		ArrayList<VeloProviderOverlayItem> items = new ArrayList<VeloProviderOverlayItem>();
 		for (VelibProvider velibProvider : VelibProvider.getVelibProviders()) {
			VeloProviderOverlayItem item = new VeloProviderOverlayItem(velibProvider);
			items.add(item);

		} 
		return items;
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		int zoonLevel = mapView.getZoomLevel();
		if (zoonLevel <= zoomLimit) {
			// Update boundy box
			long nowInMs = 0;
			boundyBox.updateBoundingE6Box(mapView, nowInMs);
			// Display boundy box
			Point minScreenCoords = new Point();
			Point maxScreenCoords = new Point();
			for (VelibProvider item : boundyBox.getBoundyBoxStations()) {
				if (item.isBoundyBoxE6()) {
					GeoPoint pointMin = item.getBoundyBoxMinAsGeoPoint();
					GeoPoint pointMax = item.getBoundyBoxMaxAsGeoPoint();
					// Screen
					mapView.getProjection().toPixels(pointMin, minScreenCoords);
					mapView.getProjection().toPixels(pointMax, maxScreenCoords);
					// Draw Rect
					paint.setColor(item.getColor());
					paint.setAlpha(20);
					canvas.drawRect(minScreenCoords.x, minScreenCoords.y, maxScreenCoords.x, maxScreenCoords.y, paint);
				}
			}
			// Draw marker
			super.draw(canvas, mapView, shadow);
		}
	}

	// @Override
	// protected VeloProviderOverlayItem createItem(int i) {
	// return items.get(i);
	// }

	// @Override
	// public int size() {
	// return items.size();
	// }

	// @Override
	// public boolean onTap(GeoPoint p, MapView mapView) {
	// int zoonLevel = mapView.getZoomLevel();
	// if (zoonLevel <= zoomLimit) {
	// return super.onTap(p, mapView);
	// } else {
	// return false;
	// }
	// }

	@Override
	protected boolean onSingleTapUpHelper(final int index, final VeloProviderOverlayItem item, final MapView mapView) {
		int zoonLevel = mapView.getZoomLevel();
		if (zoonLevel <= zoomLimit) {
			return super.onSingleTapUpHelper(index, item, mapView);
		} else {
			return false;
		}
	}

	// @Override
	// protected boolean onTap(int index) {
	// return false;
	// VeloProviderOverlayItem item = items.get(index);
	// VelibProvider velibProvider = item.getVelibProvider();
	// //
	// VeloProviderDialog dialog = new VeloProviderDialog(context, velibProvider, velibService);
	// dialog.setTitle(item.getTitle());
	// dialog.setCancelable(true);
	// dialog.show();
	// return true;
	// }

	private static class VeloProviderOnItemGestureListener implements
			org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener<VeloProviderOverlayItem> {

		private Context context;
		private VelibService velibService;

		public VeloProviderOnItemGestureListener(Context context, VelibService velibService) {
			super();
			this.context = context;
			this.velibService = velibService;
		}

		@Override
		public boolean onItemSingleTapUp(int index, VeloProviderOverlayItem item) {
			VelibProvider velibProvider = item.getVelibProvider();
			//
			VeloProviderDialog dialog = new VeloProviderDialog(context, velibProvider, velibService);
			dialog.setTitle(item.getTitle());
			dialog.setCancelable(true);
			dialog.show();
			return true;
		}

		@Override
		public boolean onItemLongPress(int index, VeloProviderOverlayItem item) {
			return false;
		}

	}

}
