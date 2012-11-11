package eu.ttbox.velib.ui.search.adapter;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import eu.ttbox.velib.R;
import eu.ttbox.velib.core.AppConstants;
import eu.ttbox.velib.map.station.drawable.StationCompassView;
import eu.ttbox.velib.map.station.drawable.StationDispoIcView;
import eu.ttbox.velib.model.FavoriteIconEnum;
import eu.ttbox.velib.model.StationHelper;

public class StationItemCurAdapter extends android.support.v4.widget.ResourceCursorAdapter implements LocationListener {

    private static final String TAG = "StationItemCurAdapter";

    private StationHelper helper;

    private boolean isNotBinding = true;

    // Config
    
    public Location lastLoc;

    // ===========================================================
    // Constructors
    // ===========================================================

    public StationItemCurAdapter(Context context, Cursor c, int flags, Location lastLoc) {
        super(context, R.layout.stations_list_item, c, flags);
        
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
        holder.location.setLatitude(latE6);
        holder.location.setLongitude(lngE6);

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
//        holder.stationCompass = (StationCompassView)view.findViewById(R.id.station_list_item_compass);
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

    private long getDistanceInMeter(int latE6, int lngE6) {
        long distInM = -1;
        if (lastLoc != null) {
            float[] results = new float[3];
            Location.distanceBetween(lastLoc.getLatitude(), lastLoc.getLongitude(), latE6 / AppConstants.E6, lngE6 / AppConstants.E6, results);
            distInM = (long) results[0];
        }
        return distInM;
    }

    private float getBearing(Location dest) {
        float result = 0;
        if (lastLoc != null) {
            result = lastLoc.bearingTo(dest);
        }
        return result;
    }

    // ===========================================================
    // Listener
    // ===========================================================

    @Override
    public void onLocationChanged(Location location) {
        lastLoc = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

}
