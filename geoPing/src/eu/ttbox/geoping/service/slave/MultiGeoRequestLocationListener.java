package eu.ttbox.geoping.service.slave;

import java.util.concurrent.CopyOnWriteArrayList;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import eu.ttbox.geoping.service.slave.GeoPingSlaveLocationService.GeoPingRequest;

public class MultiGeoRequestLocationListener implements LocationListener {

    private CopyOnWriteArrayList<GeoPingRequest> geoPingRequestList;

    public MultiGeoRequestLocationListener() {
        super();
        this.geoPingRequestList = new CopyOnWriteArrayList<GeoPingRequest>();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!geoPingRequestList.isEmpty()) {
            for (GeoPingRequest request : geoPingRequestList) {
                request.onLocationChanged(location);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (!geoPingRequestList.isEmpty()) {
            for (GeoPingRequest request : geoPingRequestList) {
                request.onStatusChanged(provider, status, extras);
            }
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (!geoPingRequestList.isEmpty()) {
            for (GeoPingRequest request : geoPingRequestList) {
                request.onProviderEnabled(provider);
            }
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (!geoPingRequestList.isEmpty()) {
            for (GeoPingRequest request : geoPingRequestList) {
                request.onProviderDisabled(provider);
            }
        }
    }

    public boolean isEmpty() {
        return geoPingRequestList.isEmpty();
    }

    public boolean remove(GeoPingRequest request) {
        return geoPingRequestList.remove(request);
    }

    public boolean add(GeoPingRequest request) {
        return geoPingRequestList.add(request);
    }

    public void clear() {
        geoPingRequestList.clear();
    }

}
