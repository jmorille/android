package eu.ttbox.geoping.ui;

import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import eu.ttbox.geoping.R;

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
        boolean isConsume = MenuOptionsItemSelectionHelper.onOptionsItemSelected(this, item);
        if (isConsume) {
            return isConsume;
        } else {
            switch (item.getItemId()) {
            case R.id.menuQuitter:
                // Pour fermer l'application il suffit de faire finish()
                finish();
                return true;
            }
        }
        return false;
    }

}
