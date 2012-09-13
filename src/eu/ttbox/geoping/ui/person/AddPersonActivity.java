package eu.ttbox.geoping.ui.person;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import eu.ttbox.geoping.GeoTrakerActivity;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.ui.map.ShowMapActivity;
import eu.ttbox.geoping.ui.ping.GeoPingActivity;
import eu.ttbox.geoping.ui.prefs.TrakingPrefActivity;

public class AddPersonActivity extends FragmentActivity {

    private static final String TAG = "AddPersonActivity";

    // Constant
    private static final int PERSON_EDIT_LOADER = R.id.config_id_person_edit_loader;

    private static final int PICK_CONTACT = 0;

    // Bindings
    private EditText nameEditText;
    private EditText phoneEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_person);
        // binding
        nameEditText = (EditText) findViewById(R.id.person_name);
        phoneEditText = (EditText) findViewById(R.id.person_phone);
        // Intents
        handleIntent(getIntent());
    }

    public boolean onCreateOptionsMenu(Menu menu) { 
        MenuInflater inflater = getMenuInflater(); 
        inflater.inflate(R.menu.menu_person_edit, menu); 
        // Il n'est pas possible de modifier l'ic�ne d'ent�te du sous-menu via
        // le fichier XML on le fait donc en JAVA
        // menu.getItem(0).getSubMenu().setHeaderIcon(R.drawable.option_white);

        return true;
    }
 
    public boolean onOptionsItemSelected(MenuItem item) { 
        switch (item.getItemId()) {
        case R.id.menu_save:
            Intent intentOption = new Intent(this, TrakingPrefActivity.class);
            startActivity(intentOption);
            return true; 
        case R.id.menu_delete:
            Intent intentGeoTraker = new Intent(this, GeoTrakerActivity.class);
            startActivity(intentGeoTraker);
            return true;
        case R.id.menu_select_contact:
            Intent intentMap = new Intent(this, ShowMapActivity.class);
            startActivity(intentMap);
            return true; 
        case R.id.menuGeoPing:
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
        if (Intent.ACTION_EDIT.equals(action)) {
            // TODO Set id
            Uri data = intent.getData();
            String entityId = data.getLastPathSegment();
            Bundle bundle = new Bundle();
            bundle.putString(Intents.EXTRA_USERID, entityId);
            getSupportLoaderManager().initLoader(PERSON_EDIT_LOADER, bundle, personLoaderCallback);
        } else if (Intent.ACTION_DELETE.equals(action)) {
            // TODO
        } else if (Intent.ACTION_INSERT.equals(action)) {
            // TODO
        }

    }

    public void onSaveClick(View v) {
        String name = nameEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        Uri uri = savePerson(name, phone);
        finish();
    }

    public void onCancelClick(View v) {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void onSelectContactClick(View v) { 
        Intent intent = new Intent(android.provider.Contacts.Intents.UI.LIST_STARRED_ACTION);
//        Intent intent = new Intent(ContactsContract.Intents.UI.LIST_STARRED_ACTION);

//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        // run
        startActivityForResult(intent, PICK_CONTACT);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
        case (PICK_CONTACT):
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                saveContactData(contactData);
                finish();
            }
        }
    }

    private void saveContactData(Uri contactData) {
        Cursor c = getContentResolver().query(contactData, new String[] { //
                ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME, // TODO
                                                                        // Check
                                                                        // for
                                                                        // V10
                                                                        // compatibility
                        ContactsContract.CommonDataKinds.Phone.NUMBER, //
                        ContactsContract.CommonDataKinds.Phone.TYPE }, null, null, null);
        try {
            // Read value
            if (c != null && c.moveToFirst()) {
                String name = c.getString(0);
                String phone = c.getString(1);
                int type = c.getInt(2);
                savePerson(name, phone);
                // showSelectedNumber(type, number);
            }
        } finally {
            c.close();
        }
    }

    private Uri savePerson(String name, String phone) {
        setPerson(name, phone);
        // Prepare db insert
        ContentValues values = new ContentValues();
        values.put(PersonColumns.KEY_NAME, name);
        values.put(PersonColumns.KEY_PHONE, phone);
        // Content
        Uri uri = getContentResolver().insert(PersonProvider.Constants.CONTENT_URI_PERSON, values);
        setResult(Activity.RESULT_OK);
        return uri;
    }

    private void setPerson(String name, String phone) {
        nameEditText.setText(name);
        phoneEditText.setText(phone);
    }

    private final LoaderManager.LoaderCallbacks<Cursor> personLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String entityId = args.getCharSequence(Intents.EXTRA_USERID).toString();
            Uri entityUri = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI_PERSON, String.format("/%s", entityId));
            // Loader
            CursorLoader cursorLoader = new CursorLoader(AddPersonActivity.this, entityUri, null, null, null, null);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.d(TAG, "onLoadFinished with cursor result count : " + cursor.getCount());
            // Display List
            if (cursor.moveToFirst()) {
                PersonHelper helper = new PersonHelper().initWrapper(cursor);
                helper.setTextPersonName(nameEditText, cursor).setTextPersonPhone(phoneEditText, cursor);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            setPerson(null, null);
        }

    };
}
