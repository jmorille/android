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
import eu.ttbox.smstraker.TrakingPrefActivity;

public class AbstractSmsTrackerActivity extends Activity {

	// Méthode qui se déclenchera lorsque vous appuierez sur le bouton menu du
	// téléphone
	public boolean onCreateOptionsMenu(Menu menu) {

		// Création d'un MenuInflater qui va permettre d'instancier un Menu XML
		// en un objet Menu
		MenuInflater inflater = getMenuInflater();
		// Instanciation du menu XML spécifier en un objet Menu
		inflater.inflate(R.menu.menu, menu);

		// Il n'est pas possible de modifier l'icône d'entête du sous-menu via
		// le fichier XML on le fait donc en JAVA
		// menu.getItem(0).getSubMenu().setHeaderIcon(R.drawable.option_white);

		return true;
	}


	// Méthode qui se déclenchera au clic sur un item
	public boolean onOptionsItemSelected(MenuItem item) {
		// On regarde quel item a été cliqué grâce à son id et on déclenche une
		// action
		switch (item.getItemId()) {
		case R.id.option:
			Intent intentOption = new Intent(this, TrakingPrefActivity.class);
			startActivity(intentOption);
			return true;
		case R.id.favoris:
			Toast.makeText(this, "Favoris", Toast.LENGTH_SHORT).show();
			return true;
		case R.id.stats:
			Toast.makeText(this, "Stats", Toast.LENGTH_SHORT).show();
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
