package eu.ttbox.geoping.service.request;

import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import eu.ttbox.geoping.service.request.GeoPingRequestHandlerService.GeoPingRequest;

public class MultiGeoRequestLocationListener implements LocationListener {

    List<GeoPingRequest> geoPingRequestList;

    public MultiGeoRequestLocationListener(List<GeoPingRequest> geoPingRequestList) {
        super();
        this.geoPingRequestList = geoPingRequestList;
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

}
