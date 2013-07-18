package eu.ttbox.velib.ui.search.adapter;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import eu.ttbox.osm.ui.map.mylocation.CompassEnum;
import eu.ttbox.osm.ui.map.mylocation.sensor.LocationUtils;
import eu.ttbox.velib.R;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.map.station.drawable.StationCompassView;
import eu.ttbox.velib.map.station.drawable.StationDispoIcView;
import eu.ttbox.velib.model.FavoriteIconEnum;
import eu.ttbox.velib.model.StationHelper;

public class StationItemCurAdapter extends android.support.v4.widget.ResourceCursorAdapter implements LocationListener, SensorEventListener {

	private static final String TAG = "StationItemCurAdapter";

	private StationHelper helper;

	private boolean isNotBinding = true;

	// Config
	private long checkDispoDeltaDelayInMs;
	private Context context;
	// Data
	private final Display mDisplay;
	public Location lastLoc;
	public int azimuth;

	// Instance
	private ExecutorService executor = Executors.newSingleThreadExecutor();

    private ConcurrentLinkedQueue<ViewHolder> viewHolders = new ConcurrentLinkedQueue<StationItemCurAdapter.ViewHolder>();


	// ===========================================================
	// UI Handler
	// ===========================================================

	
	private static final int UI_MSG_SET_DISTANCE = 1;

	private Handler uiHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UI_MSG_SET_DISTANCE:
				ViewHolder holder = (ViewHolder) msg.obj;
				int distInM = msg.arg1;
				int bearing = msg.arg2;
				// Display Distance
				setDistanceBearing(holder, distInM, bearing);

				break;
			}
		}
		
	};


	// ===========================================================
	// Constructors
	// ===========================================================

	public StationItemCurAdapter(Context context, Cursor c, int flags, Location lastLoc, Display mDisplay, long checkDispoDeltaDelayInMs) {
		super(context, R.layout.stations_list_item, c, flags);
		this.context = context;
		this.mDisplay = mDisplay;
		this.lastLoc = lastLoc; 
	}


	// ===========================================================
	// Life Cycle
	// ===========================================================

	public void close() {
		executor.shutdownNow();
		viewHolders.clear();
		
	}
	
	// ===========================================================
	// Bindings
	// ===========================================================

	private void intViewBinding(View view, Context context, Cursor cursor) {
		// Init Cursor
		helper = new StationHelper().initWrapper(cursor);
		isNotBinding = false;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (isNotBinding) {
			intViewBinding(view, context, cursor);
		}
		ViewHolder holder = (ViewHolder) view.getTag();

		helper.setTextWithIdx(holder.distanceText, cursor, helper.numberIdx) //
				.setTextWithIdx(holder.ocupationText, cursor, helper.nameIdx) //
				.setTextWithIdx(holder.adressText, cursor, helper.addressIdx);

		long stationUpdated = helper.getStationUpdated(cursor);
		int cycleCount = helper.getStationCycle(cursor);
		int parkingCount = helper.getStationParking(cursor);
		// setViewStationDispo(holder.dispoIcView, stationUpdated, cycleCount,
		// parkingCount);

		setViewFavoriteImage(holder.iconFavorite, helper.getFavoriteIconEnum(cursor));
		// Location
		int latE6 = helper.getLatitudeE6(cursor);
		int lngE6 = helper.getLongitudeE6(cursor);
		holder.location.setLatitude(latE6 / AppConstants.E6);
		holder.location.setLongitude(lngE6 / AppConstants.E6);
		// Compute Bearing
		setViewStationDispo(holder.stationCompass, stationUpdated, cycleCount, parkingCount);
		viewHolders.add(holder);
		if (lastLoc != null) {
			int bearing  = (int)computeBearing(holder);
			int distInM = getDistanceInMeter(holder.location);
			setDistanceBearing(holder, distInM, bearing); 
		} else {
			helper.setTextWithIdx(holder.distanceText, cursor, helper.numberIdx);
		}
		// Update
		// long nowInMs = System.currentTimeMillis();
		// if ((stationUpdated + checkDispoDeltaDelayInMs) < nowInMs) {
		// TODO
		// }
	}

	public void setViewFavoriteImage(ImageView v, FavoriteIconEnum favoriteType) {
		if (favoriteType == null) {
			v.setVisibility(View.GONE);
		} else {
			v.setVisibility(View.VISIBLE);
			v.setImageResource(favoriteType.getImageResource());
		}
	}

	private void setViewStationDispo(StationDispoIcView v, long stationUpdated, int cycleCount, int parkingCount) {
		if (cycleCount > -1 && parkingCount > -1) {
			// FIXME stationUpdated
			v.setStationCycle(cycleCount);
			v.setStationParking(parkingCount);
			// v.setStationFavory(true);
			v.setVisibility(View.VISIBLE);
		} else {
//			v.setVisibility(View.INVISIBLE);
		}
	}


	private void setDistanceBearing(ViewHolder holder, int distInM, int bearing) {
		String distInUnit = convertDistanceMeterInDisplayUnit(distInM);
		holder.distanceText.setText(distInUnit);
		// Display Bearing
		CompassEnum compass = CompassEnum.getCardinalPoint(bearing);
		String compassAsString = null;
		if (compass != null) {
			compassAsString = compass.getI18nLabelShort(context);
		}
		holder.bearingTextView.setText(String.format("%s° %s", (int) bearing, compassAsString));
	}
	
	// ===========================================================
	// holder
	// ===========================================================

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = super.newView(context, cursor, parent);
		// Then populate the ViewHolder
		ViewHolder holder = new ViewHolder();
		holder.distanceText = (TextView) view.findViewById(R.id.station_list_item_distance);
		holder.ocupationText = (TextView) view.findViewById(R.id.station_list_item_ocupation);
		holder.adressText = (TextView) view.findViewById(R.id.station_list_item_adress);
		// holder.dispoIcView = (StationDispoIcView)
		// view.findViewById(R.id.station_list_item_dispo);
		holder.iconFavorite = (ImageView) view.findViewById(R.id.station_list_item_icon_favorite);
		holder.stationCompass = (StationCompassView) view.findViewById(R.id.station_list_item_compass);
		holder.bearingTextView = (TextView) view.findViewById(R.id.station_list_item_bearing);
		// Cache
		holder.location = new Location("gps");

		// and store it inside the layout.
		view.setTag(holder);
		return view;

	}

	static class ViewHolder {
		Location location;
		// Data
        // StationDispoIcView dispoIcView;
		
		// Binding
		TextView distanceText;
		TextView ocupationText;
		TextView adressText;
		ImageView iconFavorite;
		StationCompassView stationCompass;
		TextView bearingTextView;

	}

	// ===========================================================
	// Bussiness
	// ===========================================================

	private Runnable computeBearingRunnable = new Runnable() {

		@Override
		public void run() {
			if (lastLoc != null) {
				for (ViewHolder holder : viewHolders) {
					computeBearing(holder);
				}
			}
		}
	};

	private Runnable computeDistancegRunnable = new Runnable() {

		@Override
		public void run() {
			if (lastLoc != null) {
				for (ViewHolder holder : viewHolders) {
					float bearing = computeBearing(holder);
					// Distance
					int distInM = getDistanceInMeter(holder.location);
					Message msg = uiHandler.obtainMessage(UI_MSG_SET_DISTANCE, holder);
					msg.arg1 = distInM;
					msg.arg2 = (int) bearing;
					uiHandler.sendMessage(msg);
				}
			}
		}
	};

	public void updateholderBearing() {
		executor.execute(computeBearingRunnable);
	}

	public float computeBearing(ViewHolder holder) {
		float bearing = -1;
		if (lastLoc != null) {
			bearing = lastLoc.bearingTo(holder.location);
			holder.stationCompass.setBearing(getDisplayRotation() + azimuth - bearing);
		}
		return bearing;
	}

	private int getDistanceInMeter(Location stationLoc) {
		int distInM = -1;
		if (lastLoc != null) {
			float[] results = new float[3];
			Location.distanceBetween(lastLoc.getLatitude(), lastLoc.getLongitude(), stationLoc.getLatitude(), stationLoc.getLongitude(), results);
			distInM = (int) results[0];
		}
		return distInM;
	}

	private String convertDistanceMeterInDisplayUnit(int distInM) {
		String distInUnit = "";
		if (distInM < 1000) {
			distInUnit = String.format("%s m", distInM);
		} else {
			double distInKm = distInM / 1000d;
			distInUnit = String.format("%.2f Km", distInKm);
		}
		return distInUnit;
	}

	// ===========================================================
	// Location Listener
	// ===========================================================

	@Override
	public void onLocationChanged(Location location) {
		if (LocationUtils.isBetterLocation(location, lastLoc)) {
			lastLoc = location;
			executor.execute(computeDistancegRunnable);
		}
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	// ===========================================================
	// Orientation Listener
	// ===========================================================

	private int getDisplayRotation() {
		switch (mDisplay.getRotation()) { // .getOrientation()
		case Surface.ROTATION_90:
			return 90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return 270;
		default:
			return 0;
		}
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
			if (event.values != null) {
				int newAzimut = roudAzimuth(event.values[0]);
				if (azimuth != newAzimut) {
					azimuth = newAzimut;
					updateholderBearing();
				}
			}
		}
	}

	private int roudAzimuth(float eventVal) {
		return Math.round(eventVal);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

}
