package eu.ttbox.smstraker.activity;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import eu.ttbox.smstraker.GeoTrakerActivity;
import eu.ttbox.smstraker.R;
import eu.ttbox.smstraker.ShowMapActivity;
import eu.ttbox.smstraker.ui.prefs.TrakingPrefActivity;

public class AbstractSmsTrackerActivity extends Activity {

	// M�thode qui se d�clenchera lorsque vous appuierez sur le bouton menu du
	// t�l�phone
	public boolean onCreateOptionsMenu(Menu menu) {

		// Cr�ation d'un MenuInflater qui va permettre d'instancier un Menu XML
		// en un objet Menu
		MenuInflater inflater = getMenuInflater();
		// Instanciation du menu XML sp�cifier en un objet Menu
		inflater.inflate(R.menu.menu, menu);

		// Il n'est pas possible de modifier l'ic�ne d'ent�te du sous-menu via
		// le fichier XML on le fait donc en JAVA
		// menu.getItem(0).getSubMenu().setHeaderIcon(R.drawable.option_white);

		return true;
	}


	// M�thode qui se d�clenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
		// On regarde quel item a �t� cliqu� gr�ce � son id et on d�clenche une
		// action
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
		case R.id.menuQuitter:
			// Pour fermer l'application il suffit de faire finish()
			finish();
			return true;
		}
		return false;
	}
	
}
