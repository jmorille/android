package eu.ttbox.geoping.service.geofence;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationStatusCodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import eu.ttbox.geoping.R;

public class GeoFenceLocationService {

    private static final String TAG = "GeoFenceLocationService";
    private Context mContext;
    // CallBack
    private GooglePlayServicesClient.OnConnectionFailedListener mOnConnectionFailedListener = new GooglePlayServicesClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.e(TAG, "Connection Failed on LocationClient Service");
        }
    };


    // ===========================================================
    // Constructors
    // ===========================================================

    public GeoFenceLocationService(Context context) {
        mContext = context;
    }


    // ===========================================================
    // Accessors
    // ===========================================================

    public void addGeofences(Geofence geofence) {
        GeopingConnectionCallbacks callbacks = new GeopingConnectionCallbacks(null, geofence);
        LocationClient mLocationClient = new LocationClient(mContext, callbacks, mOnConnectionFailedListener);
        callbacks.locationClient = mLocationClient;
        // Connect
        mLocationClient.connect();
    }

    /**
     * Remove the geofences in a list of geofence IDs. To remove all current geofences associated
     * with a request, you can also call removeGeofencesByIntent.
     * <p/>
     * <b>Note: The List must contain at least one ID, otherwise an Exception is thrown</b>
     *
     * @param geofenceIds A List of geofence IDs
     */
    public void removeGeofencesById(List<String> geofenceIds) throws
            IllegalArgumentException, UnsupportedOperationException {
        // If the List is empty or null, throw an error immediately
        if ((null == geofenceIds) || (geofenceIds.size() == 0)) {
            throw new IllegalArgumentException();
        }
        GeopingRemoveGeofencesConnectionCallbacks callbacks = new GeopingRemoveGeofencesConnectionCallbacks(null, geofenceIds);
        LocationClient  mLocationClient = new LocationClient(mContext, callbacks, mOnConnectionFailedListener);
        callbacks.locationClient = mLocationClient;
        mLocationClient.connect();
    }

    private PendingIntent createRequestPendingIntent() {
        // Create an Intent pointing to the IntentService
        Intent intent = new Intent(mContext, ReceiveTransitionsIntentService.class);
            /*
             * Return a PendingIntent to start the IntentService.
             * Always create a PendingIntent sent to Location Services
             * with FLAG_UPDATE_CURRENT, so that sending the PendingIntent
             * again updates the original. Otherwise, Location Services
             * can't match the PendingIntent to requests made with it.
             */
        return PendingIntent.getService(
                mContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    // ===========================================================
    // Callback
    // ===========================================================

    private class GeopingOnRemoveGeofencesResultListener implements LocationClient.OnRemoveGeofencesResultListener {

        private LocationClient locationClient;

        private GeopingOnRemoveGeofencesResultListener(LocationClient locationClient) {
            this.locationClient = locationClient;
        }

        @Override
        public void onRemoveGeofencesByRequestIdsResult(int statusCode, String[] geofenceRequestIds) {
            // If removing the geocodes was successful
            if (LocationStatusCodes.SUCCESS == statusCode) {

                // Create a message containing all the geofence IDs removed.
                String msg = mContext.getString(R.string.remove_geofences_id_success,
                        Arrays.toString(geofenceRequestIds));
                Log.i(TAG, msg);
            } else {
                String msg = mContext.getString(
                        R.string.remove_geofences_id_failure,
                        statusCode,
                        Arrays.toString(geofenceRequestIds)
                );
                Log.e(TAG, msg);
            }
            // Disconnect
            locationClient.disconnect();

        }

        @Override
        public void onRemoveGeofencesByPendingIntentResult(int statusCode, PendingIntent pendingIntent) {
            // If removing the geofences was successful
            if (statusCode == LocationStatusCodes.SUCCESS) {

                // In debug mode, log the result
                Log.d(GeofenceUtils.APPTAG,
                        mContext.getString(R.string.remove_geofences_intent_success));
            } else {
                // Always log the error
                Log.e(GeofenceUtils.APPTAG,
                        mContext.getString(R.string.remove_geofences_intent_failure, statusCode));
            }
            // Disconnect
            locationClient.disconnect();
        }
    }

    private class GeopingRemoveGeofencesConnectionCallbacks implements GooglePlayServicesClient.ConnectionCallbacks {
        private  List<String> geofences;
        private LocationClient locationClient;

        public GeopingRemoveGeofencesConnectionCallbacks() {
        }

        public GeopingRemoveGeofencesConnectionCallbacks(LocationClient locationClient, List<String> geofences) {
            this.locationClient = locationClient;
            this.geofences = geofences;
        }

        @Override
        public void onConnected(Bundle bundle) {
            Log.i(TAG, "Connection Success on LocationClient Service");

            // register Service
            if (locationClient != null) {
//                locationClient.addGeofences();

                Log.i(TAG, "remove Geofences : " +  geofences);
                GeopingOnRemoveGeofencesResultListener callbacks = new GeopingOnRemoveGeofencesResultListener(locationClient);
                locationClient.removeGeofences(geofences, callbacks);
            }

        }

        @Override
        public void onDisconnected() {
            Log.i(TAG, "Disconnected on LocationClient Service");
        }
    }

    private class GeopingConnectionCallbacks implements GooglePlayServicesClient.ConnectionCallbacks {

        private java.util.List<Geofence> geofences;
        private LocationClient locationClient;

        private GeopingConnectionCallbacks() {
        }

        private GeopingConnectionCallbacks(LocationClient locationClient, Geofence geofence) {
            this.locationClient = locationClient;
            ArrayList<Geofence> toAdd = new ArrayList<Geofence>(1);
            toAdd.add(geofence);
            this.geofences = toAdd;
        }

        @Override
        public void onConnected(Bundle bundle) {
            Log.i(TAG, "Connection Success on LocationClient Service");
            if (bundle != null && !bundle.isEmpty()) {
                Set<String> keys = bundle.keySet();
                for (String key : keys) {
                    Log.i(TAG, "Connection Success : " + key + " = " + bundle.get(key));
                }
            }
            // register Service
            if (locationClient != null) {
//                locationClient.addGeofences();
                Log.i(TAG, "addGeofences : " + geofences);
                locationClient.addGeofences(geofences, createRequestPendingIntent(), new GeopingOnAddGeofencesResultListener(locationClient));
            }
        }

        @Override
        public void onDisconnected() {
            Log.i(TAG, "Disconnected on LocationClient Service");

        }
    }

    private class GeopingOnAddGeofencesResultListener implements LocationClient.OnAddGeofencesResultListener {

        private LocationClient locationClient;

        private GeopingOnAddGeofencesResultListener(LocationClient locationClient) {
            this.locationClient = locationClient;
        }

        @Override
        public void onAddGeofencesResult(int statusCode, String[] geofenceRequestIds) {
            // If adding the geocodes was successful
            if (LocationStatusCodes.SUCCESS == statusCode) {
                // Create a message containing all the geofence IDs added.
                String msg = mContext.getString(R.string.add_geofences_result_success,
                        Arrays.toString(geofenceRequestIds));
                // In debug mode, log the result
                Log.d(TAG, msg);
            } else {
                String msg = mContext.getString(
                        R.string.add_geofences_result_failure,
                        statusCode,
                        Arrays.toString(geofenceRequestIds)
                );
                // Log an error
                Log.e(TAG, msg);
            }
            // Disconnect
            locationClient.disconnect();
        }
    }

}
