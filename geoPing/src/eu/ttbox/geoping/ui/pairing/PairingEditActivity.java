package eu.ttbox.geoping.ui.pairing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;

public class PairingEditActivity extends SherlockFragmentActivity   {

    private static final String TAG = "PairingEditActivity";

    private PairingEditFragment editFramgent;
   

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pairing_edit_activity);
         
        // Intents
        handleIntent(getIntent());
    }



    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        Log.d(TAG, "****************** onAttachFragment");

        if (fragment instanceof PairingEditFragment) {
            editFramgent = (PairingEditFragment) fragment;
        }
    }

  
    // ===========================================================
    // Life Cycle
    // ===========================================================
 
    // ===========================================================
    // Intent Handler
    // ===========================================================

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "handleIntent for action : " + action);
        }
        if (Intent.ACTION_EDIT.equals(action) || Intent.ACTION_DELETE.equals(action) ) {
            Uri entityUri = intent.getData();
            // Set Fragment
            
//            Bundle fragArgs = new Bundle();
//            fragArgs.putString(Intents.EXTRA_PERSON_ID,entityUri.getLastPathSegment()) ;
//            Log.d(TAG, "****************** setArguments");
//            editFramgent.setArguments(fragArgs); 
            editFramgent.loadEntity(entityUri.getLastPathSegment());
            // Tracker
            if (Intent.ACTION_DELETE.equals(action) ){
                GeoPingApplication.getInstance().tracker().trackPageView("/Pairing/delete");
            } else {
                GeoPingApplication.getInstance().tracker().trackPageView("/Pairing/edit");
            } 
        } else if (Intent.ACTION_INSERT.equals(action)) {
          editFramgent.prepareInsert();
            GeoPingApplication.getInstance().tracker().trackPageView("/Pairing/insert");
        }

    }

    // ===========================================================
    // Menu
    // ===========================================================

    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.menu_pairing_edit, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_save:
            editFramgent.onSaveClick();
            return true;
        case R.id.menu_delete:
            editFramgent.onDeleteClick();
            return true;
        case R.id.menu_select_contact:
            editFramgent.onSelectContactClick(null);
            return true;
        case R.id.menu_cancel:
            editFramgent.onCancelClick();
            return true;
        case R.id.menuQuitter:
            // Pour fermer l'application il suffit de faire finish()
            finish();
            return true;
        }
        return false;
    }


    // ===========================================================
    // Listener
    // ===========================================================



}
