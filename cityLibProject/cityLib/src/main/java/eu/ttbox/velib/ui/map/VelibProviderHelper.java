package eu.ttbox.velib.ui.map;


import android.content.SharedPreferences;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

public class VelibProviderHelper {

    public static eu.ttbox.velib.model.VelibProvider computeConditionVelibProvider(SharedPreferences sharedPreferences,GeoPoint lastKnownLocationAsGeoPoint) {
        eu.ttbox.velib.model.VelibProvider velibProvider = null;
        String providerName = sharedPreferences.getString(eu.ttbox.velib.core.AppConstants.PREFS_KEY_PROVIDER_SELECT, eu.ttbox.velib.model.VelibProvider.FR_PARIS.getProviderName());
        if (providerName != null) {
            velibProvider = eu.ttbox.velib.model.VelibProvider.getVelibProvider(providerName);
        } else if (lastKnownLocationAsGeoPoint != null) {
            ArrayList<eu.ttbox.velib.model.VelibProvider> providers = eu.ttbox.velib.model.VelibProvider.getVelibProviderInBoundyBox(lastKnownLocationAsGeoPoint);
            if (providers != null && !providers.isEmpty()) {
                velibProvider = providers.get(0);
            }
        }
        if (velibProvider == null) {
            velibProvider = eu.ttbox.velib.model.VelibProvider.FR_PARIS;
        }
        return velibProvider;
    }

}
