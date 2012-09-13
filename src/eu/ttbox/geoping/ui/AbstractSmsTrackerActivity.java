package eu.ttbox.geoping.ui;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import eu.ttbox.geoping.GeoTrakerActivity;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.ui.map.ShowMapActivity;
import eu.ttbox.geoping.ui.ping.GeoPingActivity;
import eu.ttbox.geoping.ui.prefs.TrakingPrefActivity;

public class AbstractSmsTrackerActivity extends FragmentActivity {

 
	public boolean onCreateOptionsMenu(Menu menu) { 
		MenuInflater inflater = getMenuInflater(); 
		inflater.inflate(R.menu.menu, menu); 
		// Il n'est pas possible de modifier l'ic�ne d'ent�te du sous-menu via
		// le fichier XML on le fait donc en JAVA
		// menu.getItem(0).getSubMenu().setHeaderIcon(R.drawable.option_white);

		return true;
	}
 
	public boolean onOptionsItemSelected(MenuItem item) { 
		switch (item.getItemId()) {
		case R.id.option:
			Intent intentOption = new Intent(this, TrakingPrefActivity.class);
			startActivity(intentOption);
			return true; 
		case R.id.menuGeotracker:
			Intent intentGeoTraker = new Intent(this, GeoTrakerActivity.class);
			startActivity(intentGeoTraker);
			return true;
		case R.id.menuMap:
			Intent intentMap = new Intent(this, ShowMapActivity.class);
			startActivity(intentMap);
			return true; 
		case R.id.menu_track_person:
			Intent intentGeoPing = new Intent(this, GeoPingActivity.class);
			startActivity(intentGeoPing);
			return true; 
		case R.id.menuQuitter:
			// Pour fermer l'application il suffit de faire finish()
			finish();
			return true;
		}
		return false;
	}
	
}
