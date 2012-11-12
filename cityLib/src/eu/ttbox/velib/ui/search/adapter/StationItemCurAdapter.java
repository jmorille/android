package eu.ttbox.velib.ui.search.adapter;

import java.util.concurrent.CopyOnWriteArrayList;
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
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
    private long checkDispoDeltaDelayInMs ;
    
    // Data
    private final Display mDisplay;
    public Location lastLoc;
    public int azimuth;

    // Instance
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    CopyOnWriteArrayList<ViewHolder> viewHolders = new CopyOnWriteArrayList<StationItemCurAdapter.ViewHolder>();

    // ===========================================================
    // Constructors
    // ===========================================================

    public StationItemCurAdapter(Context context, Cursor c, int flags, Location lastLoc, Display mDisplay, long checkDispoDeltaDelayInMs) {
        super(context, R.layout.stations_list_item, c, flags); 
        this.mDisplay = mDisplay;
        this.lastLoc = lastLoc;
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
        setViewStationDispo(holder.dispoIcView, stationUpdated, cycleCount, parkingCount);
       
        setViewFavoriteImage(holder.iconFavorite, helper.getFavoriteIconEnum(cursor));
        // Location
        int latE6 = helper.getLatitudeE6(cursor);
        int lngE6 = helper.getLongitudeE6(cursor);
        holder.location.setLatitude(latE6 / AppConstants.E6);
        holder.location.setLongitude(lngE6 / AppConstants.E6);
        // Compute Bearing
        setViewStationDispo(holder.stationCompass, stationUpdated, cycleCount, parkingCount);
         viewHolders.add(holder);
        computeBearing( holder);
        // Update
//        long nowInMs = System.currentTimeMillis();
//        if ((stationUpdated + checkDispoDeltaDelayInMs) < nowInMs) {
//            TODO
//        }
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
            v.setVisibility(View.INVISIBLE);
        }
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
        holder.dispoIcView = (StationDispoIcView) view.findViewById(R.id.station_list_item_dispo);
        holder.iconFavorite = (ImageView) view.findViewById(R.id.station_list_item_icon_favorite);
        holder.stationCompass = (StationCompassView) view.findViewById(R.id.station_list_item_compass);
        // Cache
        holder.location = new Location("gps");

        // and store it inside the layout.
        view.setTag(holder);
        return view;

    }

    static class ViewHolder {
        Location location;

        // Binding
        TextView distanceText;
        TextView ocupationText;
        StationDispoIcView dispoIcView;
        TextView adressText;
        ImageView iconFavorite;
        StationCompassView stationCompass;

       

    }

    // ===========================================================
    // Bussiness
    // ===========================================================

    private Runnable computeBearingRunnable = new Runnable() {
        
        @Override
        public void run() {
            for (ViewHolder holder : viewHolders) {
                computeBearing( holder);
             }
        }
    };
    
    
    
    
    public void updateholderBearing() {
        executor.execute(computeBearingRunnable);
    }

    
    public void computeBearing(ViewHolder holder) {
        float bearing = lastLoc.bearingTo(holder.location);
        holder.stationCompass.setBearing( getDisplayRotation() + azimuth-bearing );
    }
    
    private String getDistanceInMeter(int latE6, int lngE6) {
        String distInUnit = "";
        if (lastLoc != null) {
            float[] results = new float[3];
            Location.distanceBetween(lastLoc.getLatitude(), lastLoc.getLongitude(), latE6 / AppConstants.E6, lngE6 / AppConstants.E6, results);
            long distInM = (long) results[0];
            if (distInM<1000) {
                distInUnit = String.format("% sm", distInM);
            } else {
                double distInKm = distInM / 1000d;
                distInUnit = String.format("%.2f Km", distInKm);
            }
        }
        return distInUnit;
    }

    private float getBearing(Location dest) {
        float result = 0;
        if (lastLoc != null) {
            result = lastLoc.bearingTo(dest);
        }
        return result;
    }

    // ===========================================================
    // Location Listener
    // ===========================================================

    @Override
    public void onLocationChanged(Location location) {
        if (LocationUtils.isBetterLocation(location, lastLoc)) {
            lastLoc = location;
            updateholderBearing();
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
