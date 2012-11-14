package eu.ttbox.velib.map.station.drawable;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Typeface;
import android.view.View;
import eu.ttbox.velib.R;
import eu.ttbox.velib.model.Station;

public class StationDispoDrawable extends View {
	protected final String TAG = getClass().getSimpleName();

	protected static final int CIRCLE_BORDER_STROKE_HALF_WIDTH = 2;
	protected static final int CIRCLE_BORDER_STROKE_WIDTH = CIRCLE_BORDER_STROKE_HALF_WIDTH * 2;

	// Init
	protected Context context;
	protected int expectedVelo = 2;

	// config
	protected boolean drawDisplayDispoText = true;
	protected boolean drawDisplayInternalCircle = true;
	protected boolean drawDisplayExternalCircle = true;

	// Paint
	protected Paint textPaint;

	protected Paint circleUnkwonPaint;
	protected Paint selectedBorderPaint;
	protected Paint circlePaintGreen, circlePaintGreenExternal, circlePaintBorderGreen;
	protected Paint circlePaintYellow, circlePaintYellowExternal, circlePaintBorderYellow;
	protected Paint circlePaintRed, circlePaintRedExternal, circlePaintBorderRed;

	// Cache
	private int cacheZoomLevel, cacheZoomLevelRadiusExternal;
	private int radiusExternal, radiusInternal, radiusExternalEffectifWidth, radiusExternalEffectifRadius, radiusInternalBorder, radiusExternalBorder;
	protected int textPaintSize = 16;

	// Other
	protected float densityMultiplier = 1f;

	public StationDispoDrawable(Context context, int expectedVelo) {
		super(context);
		this.context = context;
		this.expectedVelo = expectedVelo;
		initVelibTrackOverlay();
	}

	private void initVelibTrackOverlay() {
		Resources r = context.getResources();
		densityMultiplier = context.getResources().getDisplayMetrics().density;
		// Text paint
		textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(textPaintSize);
		textPaint.setTypeface(Typeface.DEFAULT_BOLD); 
		 
		// textPaint.setTextSize(20*densityMultiplier);
		textPaint.setTextAlign(Align.CENTER);
		// Unkon values
		circleUnkwonPaint = new Paint();
		circleUnkwonPaint.setAntiAlias(true);
		circleUnkwonPaint.setColor(r.getColor(R.color.station_dispo_unknown));
		// Selected
		selectedBorderPaint = new Paint();
		selectedBorderPaint.setColor(r.getColor(R.color.station_select_border));
		selectedBorderPaint.setAntiAlias(true);
		selectedBorderPaint.setStrokeWidth(CIRCLE_BORDER_STROKE_WIDTH);
		selectedBorderPaint.setStyle(Paint.Style.STROKE);

		// Circle Green
		circlePaintGreen = new Paint();
		circlePaintGreen.setAntiAlias(true);
		circlePaintGreen.setColor(r.getColor(R.color.station_dispo_ok));

		circlePaintGreenExternal = new Paint(circlePaintGreen);
		circlePaintGreenExternal.setStyle(Paint.Style.STROKE);

		// Circle Border Green
		circlePaintBorderGreen = new Paint();
		circlePaintBorderGreen.setAntiAlias(true);
		circlePaintBorderGreen.setColor(r.getColor(R.color.station_dispo_ok_border));
		circlePaintBorderGreen.setStyle(Paint.Style.STROKE);
		circlePaintBorderGreen.setStrokeWidth(CIRCLE_BORDER_STROKE_WIDTH);

		// Circle Yellow
		circlePaintYellow = new Paint();
		circlePaintYellow.setAntiAlias(true);
		circlePaintYellow.setColor(r.getColor(R.color.station_dispo_warning));

		circlePaintYellowExternal = new Paint(circlePaintYellow);
		circlePaintYellowExternal.setStyle(Paint.Style.STROKE);

		// Border Yellow
		circlePaintBorderYellow = new Paint();
		circlePaintBorderYellow.setAntiAlias(true);
		circlePaintBorderYellow.setColor(r.getColor(R.color.station_dispo_warning));
		circlePaintBorderYellow.setStyle(Paint.Style.STROKE);
		circlePaintBorderYellow.setStrokeWidth(CIRCLE_BORDER_STROKE_WIDTH);

		// Circle Red
		circlePaintRed = new Paint();
		circlePaintRed.setAntiAlias(true);
		circlePaintRed.setColor(r.getColor(R.color.station_dispo_ko));

		circlePaintRedExternal = new Paint(circlePaintRed);
		circlePaintRedExternal.setStyle(Paint.Style.STROKE);

		// Border Red
		circlePaintBorderRed = new Paint();
		circlePaintBorderRed.setAntiAlias(true);
		circlePaintBorderRed.setColor(r.getColor(R.color.station_dispo_ko_border));
		circlePaintBorderRed.setStyle(Paint.Style.STROKE);
		circlePaintBorderGreen.setStrokeWidth(CIRCLE_BORDER_STROKE_WIDTH);

	}

	public void draw(final Canvas canvas, final int zoomLevel, final Station station, final Point myScreenCoords) {
		// Log.d(TAG, "Map Zoom Level =" + zoomLevel );
		// Compte Radius For Level
		int radiusExternal = computeRadius(zoomLevel);
		drawForExternalRadius(canvas, radiusExternal, station, myScreenCoords);

	}

	public void drawForExternalRadius(final Canvas canvas, final int radiusExternal, final Station station, final Point myScreenCoords) {
		defineDrawConfigForRadiusExternal(radiusExternal);
		// Draw Internal circles
		if (drawDisplayInternalCircle) {
			drawDispoStations(canvas, myScreenCoords, station.getStationCycle(), this.expectedVelo, true);
		}
		// Draw External circles
		if (drawDisplayExternalCircle) {
			drawDispoStations(canvas, myScreenCoords, station.getStationParking(), this.expectedVelo, false);
		}
	}

	public void drawSelected(final Canvas canvas, final int zoomLevel, final Station station, final Point myScreenCoords) {
		int radiusSelected = zoomLevel + 11; // Zoom 17 => 27
		canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, radiusSelected, selectedBorderPaint);
	}

	public int computeRadius(int zoomLevel) {
		if (zoomLevel != cacheZoomLevel) {
			// Compute Level
			this.cacheZoomLevelRadiusExternal = zoomLevel + 11; // Zoom 17 => 28
			// defineDrawConfigForRadiusExternal(this.radiusExternal);
			// Save Cache
			this.cacheZoomLevel = zoomLevel;
		}
		// Manage border witdth
		// radiusInternal = radiusInternal-CIRCLE_BORDER_STROKE_HALF_WIDTH;
		// radiusExternal = radiusExternal-CIRCLE_BORDER_STROKE_HALF_WIDTH;
		return cacheZoomLevelRadiusExternal;
	}

	public void defineDrawConfigForRadiusExternal(int _radiusExternal) {
		if (this.radiusExternal != _radiusExternal) {
			this.radiusInternal = (_radiusExternal / 2); // Zoom 17 => 14 => zoomLevel-7
			this.radiusExternalEffectifWidth = radiusInternal / 2;// Zoom 17 => 7
			this.radiusExternalEffectifRadius = _radiusExternal - radiusExternalEffectifWidth; // Zoom 17 => 21
			// Border
			this.radiusInternalBorder = radiusInternal - CIRCLE_BORDER_STROKE_HALF_WIDTH;
			this.radiusExternalBorder = _radiusExternal - CIRCLE_BORDER_STROKE_HALF_WIDTH;
			// Define External "Selected Paint Color"
			circlePaintGreenExternal.setStrokeWidth(radiusInternal); // 14;
			circlePaintRedExternal.setStrokeWidth(radiusInternal); // 14;
			circlePaintYellowExternal.setStrokeWidth(radiusInternal); // 14;
			// Text Size
			// TODO textPaint.setTextSize(textPaintSize);

			// close cache value
			this.radiusExternal = _radiusExternal;
		}
	}

	private void drawDispoStations(Canvas canvas, Point myScreenCoords, int veloDispo, int expectedVelo, boolean internalCircle) {

		// Select Color
		Paint selectedPaintColor;
		Paint selectedPaintBorderColor;
		if (veloDispo >= expectedVelo) {
			selectedPaintColor = internalCircle ? circlePaintGreen : circlePaintGreenExternal;
			selectedPaintBorderColor = circlePaintBorderGreen;
		} else if (veloDispo <= 0) {
			selectedPaintColor = internalCircle ? circlePaintRed : circlePaintRedExternal;
			selectedPaintBorderColor = circlePaintBorderRed;
		} else {
			selectedPaintColor = internalCircle ? circlePaintYellow : circlePaintYellowExternal;
			selectedPaintBorderColor = circlePaintBorderYellow;
		}

		// Compute radius
		int radiusInPixel;
		int radiusBorderInPixel;
		if (internalCircle) {
			// Internal Circle
			radiusInPixel = radiusInternal; // zoomLevel - 6; // Zoom 17 => 11
			radiusBorderInPixel = radiusInternalBorder; // radiusInternal-CIRCLE_BORDER_STROKE_HALF_WIDTH;
			// Log.d(TAG, String.format( "Circle Internal radius =%s / Border = %s", radiusInPixel,radiusBorderInPixel ));
		} else {
			// External Circle
			radiusInPixel = radiusExternalEffectifRadius; // zoomLevel + 2;// Zoom 17 => 19
			radiusBorderInPixel = radiusExternalBorder; // zoomLevel + 10; // Zoom 17 => 27
			// TODO radius Border Width InPixel not be set here // = radiusInternal
			// selectedPaintColor.setStrokeWidth(radiusInternal); //14;
			// Log.d(TAG, String.format( "Circle External radius =%s / Border = %s / Border Width %s", radiusInPixel,radiusBorderInPixel,
			// radiusBorderWidthInPixel ));
		}
		// Circle Draw
		canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, radiusInPixel, selectedPaintColor);
		// selectedPaintBorderColor.setStrokeWidth(1);
		// selectedPaintBorderColor.setColor(Color.BLACK);
		canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, radiusBorderInPixel, selectedPaintBorderColor);

		// for test
		// selectedBorderPaint.setStrokeWidth(1);
		// canvas.drawCircle(myScreenCoords.x, myScreenCoords.y, radiusExternal, selectedBorderPaint);

		// Dispo Text
		drawDispoText(canvas, myScreenCoords, veloDispo, expectedVelo, internalCircle);
	}

	protected void drawDispoText(Canvas canvas, Point myScreenCoords, int veloDispo, int expectedVelo, boolean internalCircle) {
		// Dispo Text
		boolean isDisplayDispoText = drawDisplayDispoText;
		if (veloDispo < 1) {
			isDisplayDispoText = false;
		}
		if (isDisplayDispoText) {
			int deltaY = 7;
			String dispoText = String.valueOf(veloDispo);
			// float[] widths = new float[dispoText.length()];
			// deltaY= textPaint.getTextWidths(dispoText, widths)/2;
			if (internalCircle) {
				canvas.drawText(dispoText, myScreenCoords.x, myScreenCoords.y + deltaY, textPaint);
			} else {
				canvas.drawText(dispoText, myScreenCoords.x, myScreenCoords.y + radiusExternalEffectifRadius, textPaint);
			}
		}
	}

	// ### Config ### //
	// ############## //


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
       //TODO 
        super.onSizeChanged(w, h, oldw, oldh);
    }
    
	public void setExpectedVelo(int expectedVelo) {
		this.expectedVelo = expectedVelo;
	}

	public void setDrawDisplayDispoText(boolean enable) {
		this.drawDisplayDispoText = enable;
	}

	public boolean isDrawDisplayInternalCircle() {
		return drawDisplayInternalCircle;
	}

	public void setDrawDisplayInternalCircle(boolean drawDisplayInternalCircle) {
		this.drawDisplayInternalCircle = drawDisplayInternalCircle;
	}

	public boolean isDrawDisplayExternalCircle() {
		return drawDisplayExternalCircle;
	}

	public void setDrawDisplayExternalCircle(boolean drawDisplayExternalCircle) {
		this.drawDisplayExternalCircle = drawDisplayExternalCircle;
	}

	public boolean isDrawDisplayDispoText() {
		return drawDisplayDispoText;
	}
}
