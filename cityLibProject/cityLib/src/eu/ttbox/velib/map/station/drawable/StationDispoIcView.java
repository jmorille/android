package eu.ttbox.velib.map.station.drawable;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import eu.ttbox.velib.R;
import eu.ttbox.velib.model.Station;

public class StationDispoIcView extends View {

    private static final String TAG = "StationDispoIcView";

    int parkingCount = 0;
    int cycleCount = 0;

    boolean drawCycle = true;
    boolean drawParking = true;

    // Other
    int expectedVelo = 2;
    int zoomLevel = 8;
    Point point;
    Paint paintBackground;

    // Config
    private int centerX;
    private int centerY;
    private int delta =  0;//5;
    private int externalRadius;

    private boolean drawStationDispo = false;;
 
    
    // Instance
    private StationDispoDrawable stationDispo;
    private Station station;

    public StationDispoIcView(Context context) {
        super(context);
        initStationDispoView();
    }

    public StationDispoIcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StationDispoIcView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //
        initStationDispoView();
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
        // Don't forget this
        a.recycle();

    }

    private void initStationDispoView() {
        stationDispo = new StationDispoIcDrawable(getContext(), expectedVelo);
        station = new Station();
        //
        point = new Point();
        paintBackground = new Paint();
        paintBackground.setStyle(Paint.Style.FILL);
        paintBackground.setAntiAlias(true);
        paintBackground.setColor(Color.WHITE);
    }

    public void setStationFavory(boolean isFavorite) {
        station.setFavory(isFavorite);
    }

    public void setStationParking(int parkingCount) {
        this.parkingCount = parkingCount;
        station.setStationParking(parkingCount);
        drawStationDispo = true;
    }

    public void setStationCycle(int cycleCount) {
        this.cycleCount = cycleCount;
        station.setStationCycle(cycleCount);
        drawStationDispo = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Compute Draw
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, String.format(" width = %s  // height = %s", getWidth(), getHeight()));
        }
        point.x = centerX;
        point.y = centerY;
         // Draw Station
//        if (drawStationDispo) {
          canvas.drawCircle(point.x, point.y, externalRadius, paintBackground);
//        }
        onSubBackground(canvas);
        if (drawStationDispo) {
        	stationDispo.drawForExternalRadius(canvas, externalRadius, station, point);
        }
     }

    public void onSubBackground(Canvas canvas) {
        
    }
           
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = (w / 2);
        centerY = (h / 2);
        externalRadius = Math.min(centerX - delta, centerY - delta);
        super.onSizeChanged(w, h, oldw, oldh);
    }

}
