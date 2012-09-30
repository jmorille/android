package eu.ttbox.geoping.ui;

import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;
import eu.ttbox.geoping.GeoTrakerActivity;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.map.ShowMapActivity;
import eu.ttbox.geoping.ui.pairing.PairingListActivity;
import eu.ttbox.geoping.ui.person.PersonListActivity;
import eu.ttbox.geoping.ui.prefs.TrakingPrefActivity;

public class MenuOptionsItemSelectionHelper {

    public static boolean onOptionsItemSelected(Context context, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.option:
            Intent intentOption = new Intent(context, TrakingPrefActivity.class);
            context.startActivity(intentOption);
            return true;
        case R.id.menuGeotracker:
            Intent intentGeoTraker = new Intent(context, GeoTrakerActivity.class);
            context.startActivity(intentGeoTraker);
            return true;
        case R.id.menuMap:
            Intent intentMap = new Intent(context, ShowMapActivity.class);
            context.startActivity(intentMap);
            return true;
        case R.id.menu_pairing:
            Intent intentPairing = new Intent(context, PairingListActivity.class);
            context.startActivity(intentPairing);
            return true; 
        case R.id.menu_track_person:
            Intent intentGeoPing = new Intent(context, PersonListActivity.class);
            context.startActivity(intentGeoPing);
            return true;
        }
        return false;
    }
}
