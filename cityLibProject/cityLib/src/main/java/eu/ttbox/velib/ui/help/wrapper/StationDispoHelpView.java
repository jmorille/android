package eu.ttbox.velib.ui.help.wrapper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;
import eu.ttbox.velib.R;
import eu.ttbox.velib.map.station.drawable.StationDispoDrawable;
import eu.ttbox.velib.map.station.drawable.StationDispoIcDrawable;
import eu.ttbox.velib.model.Station;

public class StationDispoHelpView extends TextView // View
{

	private static final String TAG = StationDispoHelpView.class.getSimpleName();

	// Config
	private int personExpected = 2;
	boolean drawCycle = true;
	boolean drawParking = true;

	// Instance value
	Paint paintBackground, paintText;
	StationDispoDrawable stationDispo;
	Station station;
	Point point;

	Bitmap parking, parkingFin, cycles, cyclesFin;

	int maxPanneauWidth, maxPanneauHeigh, halfPanneauWidth, halfPanneauHeigh;

	// Instance Value

	Bitmap imgParkingStatus;
	Bitmap imgCyclesStatus;
	String parkingString;
	String cycleString;

	public StationDispoHelpView(Context context) {
		super(context);
		initStationDispoHelpView();
	}

	public StationDispoHelpView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * @ see http://stackoverflow.com/questions/2029719/how-can-i-create-my-custom -properties-on-xml-for-android
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public StationDispoHelpView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initStationDispoHelpView();

		// Read attr
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.StationDispoView);
		String parkingString = a.getString(R.styleable.StationDispoView_station_parking);
		// Log.i(TAG, parkingString);
		if (parkingString != null && parkingString.length() > 0) {
			int parkcinkgCount = Integer.parseInt(parkingString.toString());
			setStationParking(parkcinkgCount);
		}
		String cycleString = a.getString(R.styleable.StationDispoView_station_cycles);
		if (cycleString != null && cycleString.length() > 0) {
			int cycleCount = Integer.parseInt(cycleString.toString());
			setStationCycle(cycleCount);
		}
		// Read Draw Circle
		drawCycle = a.getBoolean(R.styleable.StationDispoView_draw_cycles, true);
		drawParking = a.getBoolean(R.styleable.StationDispoView_draw_parking, true);
		stationDispo.setDrawDisplayInternalCircle(drawCycle);
		stationDispo.setDrawDisplayExternalCircle(drawParking);

		// Don't forget this
		a.recycle();
		//
	}

	private void initStationDispoHelpView() {
		// Paint
		paintBackground = new Paint();
		paintBackground.setStyle(Paint.Style.FILL);
		paintBackground.setColor(Color.WHITE);

		paintText = new Paint();
		paintText.setColor(getCurrentTextColor()); // Color.WHITE
		paintText.setTextSize(getTextSize());
		paintText.setStrokeWidth(3);
		// Init station
		stationDispo = new StationDispoIcDrawable(getContext(), personExpected);
		stationDispo.setDrawDisplayDispoText(true);
		stationDispo.setDrawDisplayInternalCircle(drawCycle);
		stationDispo.setDrawDisplayExternalCircle(drawParking);
		// Sample staion
		station = new Station();
		station.setVeloTotal(10);
		station.setStationParking(0);
		station.setStationCycle(0);
		station.setBonus(false);
		station.setFavory(false);
		// Compute Point
		point = new Point();
		// Image
		parking = BitmapFactory.decodeResource(getResources(), R.drawable.panneau_parking);
		parkingFin = BitmapFactory.decodeResource(getResources(), R.drawable.panneau_parking_fin);
		cycles = BitmapFactory.decodeResource(getResources(), R.drawable.panneau_obligation_cycles);
		cyclesFin = BitmapFactory.decodeResource(getResources(), R.drawable.panneau_obligation_cycles_fin);
		for (Bitmap panneau : new Bitmap[] { parking, parkingFin, cycles, cyclesFin }) {
			maxPanneauWidth = Math.max(maxPanneauWidth, panneau.getWidth());
			maxPanneauHeigh = Math.max(maxPanneauWidth, panneau.getHeight());
		}
		halfPanneauWidth = maxPanneauWidth / 2;
		halfPanneauHeigh = maxPanneauHeigh / 2;
		// Defualt String
		parkingString = getResources().getString(R.string.help_station_dispo_parking_none);
		cycleString = getResources().getString(R.string.help_station_dispo_cycle_none);
		imgParkingStatus = parkingFin;
		imgCyclesStatus = cyclesFin;
	}

	public void setStationParking(int parkingCount) {
		station.setStationParking(parkingCount);
		// Compute Img
		if (parkingCount < 1) {
			imgParkingStatus = parkingFin;
		} else {
			imgParkingStatus = parking;
		}

		// Dispo String
		if (parkingCount == 0) {
			parkingString = getResources().getString(R.string.help_station_dispo_parking_none);
		} else if (parkingCount < personExpected) {
			parkingString = getResources().getString(R.string.help_station_dispo_parking_unexpected);
		} else {
			parkingString = getResources().getQuantityString(R.plurals.help_station_dispo_parking_count, parkingCount, parkingCount);
		}
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, String.format("for Parking count %s : => Message : %s", parkingCount, parkingString));
		}
	}

	public void setStationCycle(int cycleCount) {
		station.setStationCycle(cycleCount);
		// Compute Img
		if (cycleCount < 1) {
			imgCyclesStatus = cyclesFin;
		} else {
			imgCyclesStatus = cycles;
		}
		// Cycle String
		if (cycleCount == 0) {
			cycleString = getResources().getString(R.string.help_station_dispo_cycle_none);
		} else if (cycleCount < personExpected) {
			cycleString = getResources().getString(R.string.help_station_dispo_cycle_unexpected);
		} else {
			cycleString = getResources().getQuantityString(R.plurals.help_station_dispo_cycle_count, cycleCount, cycleCount);
		}
		if (Log.isLoggable(TAG, Log.DEBUG)) {
			Log.d(TAG, String.format("for Cycle count %s : => Message : %s", cycleCount, cycleString));
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// Draw backgroud color
		// canvas.drawPaint(paintBackground);
		int x = 0;
		int y = 0;

		// Center
		int mapView = 41;
		int maxDispoRadius = stationDispo.computeRadius(mapView);
		int canvasCenterY = maxDispoRadius + maxPanneauHeigh;

		// --- Draw Parking
		// ------------------
		point.x = maxDispoRadius + (maxDispoRadius / 4);
		point.y = canvasCenterY;
		canvas.drawCircle(point.x, point.y, maxDispoRadius, paintBackground);
		stationDispo.drawForExternalRadius(canvas, maxDispoRadius, station, point);

		// Panneau Parking
		x = point.x + maxDispoRadius + maxDispoRadius;
		y = point.y - maxPanneauHeigh - halfPanneauHeigh;
		int deltaLineX = (x - point.x) / 4;
		if (drawParking) {
			canvas.drawLines(new float[] { x, y + halfPanneauHeigh, x - deltaLineX, y + halfPanneauHeigh, x - deltaLineX, y + halfPanneauHeigh,
					point.x + (2 * mapView / 3), point.y - (mapView / 2) }, paintText);
			canvas.drawBitmap(imgParkingStatus, x, y, paintText);
			canvas.drawText(parkingString, x + maxPanneauWidth + (maxPanneauWidth / 5), y + halfPanneauHeigh, paintText);
		}

		// Panneay Cycle
		// x = point.x + maxDispoRadius + maxDispoRadius;
		y = point.y;
		if (drawCycle) {
			canvas.drawLines(new float[] { x, y + halfPanneauHeigh, x - deltaLineX, y + halfPanneauHeigh, x - deltaLineX, y + halfPanneauHeigh, point.x,
					point.y + (mapView / 4) }, paintText);
			canvas.drawBitmap(imgCyclesStatus, x, y, paintText);
			canvas.drawText(cycleString, x + maxPanneauWidth + (maxPanneauWidth / 5), y + halfPanneauHeigh, paintText);
		}
		// if the view is visible onDraw will be called at some point
		// in the future
		// invalidate();
	}

}
