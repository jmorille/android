package eu.ttbox.geoping.service.geofence.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.google.android.gms.location.Geofence;

import java.util.ArrayList;
import java.util.List;

import eu.ttbox.geoping.domain.GeoFenceProvider;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.domain.pairing.GeoFenceHelper;
import eu.ttbox.geoping.service.geofence.GeoFenceLocationService;


public class GeofenceBootRegisterReceiver extends BroadcastReceiver {

    private static final String TAG = "GeofenceBootRegisterReceiver";


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.d(TAG, "### ####################################### ### ");
            Log.d(TAG, "### ### Geofence BOOT : Register fences ### ### ");
            Log.d(TAG, "### ####################################### ### ");
            // Search Geofence to register
            List<Geofence> geofences = getAllGeofences(context);
            if (geofences != null && !geofences.isEmpty()) {
                // Register It
                GeoFenceLocationService geoFenceLocationService = new GeoFenceLocationService(context);
                geoFenceLocationService.addGeofences(geofences);
            }
        }
    }

    private List<Geofence> getAllGeofences(Context context) {
        ArrayList<Geofence> result = null;
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(GeoFenceProvider.Constants.CONTENT_URI, null, null, null, null);
        try {
            int cursorCount = cursor.getCount();
            if (cursorCount > 0) {
                result = new ArrayList<Geofence>(cursorCount);
                GeoFenceHelper helper = new GeoFenceHelper().initWrapper(cursor);
                while (cursor.moveToNext()) {
                    CircleGeofence circleGeofence = helper.getEntity(cursor);
                    Geofence fence = circleGeofence.toGeofence();
                    result.add(fence);
                }
            }
        } finally {
            cursor.close();
        }
        return result;
    }
}