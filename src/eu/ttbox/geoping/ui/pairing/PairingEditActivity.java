package eu.ttbox.geoping.ui.pairing;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Paint;
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
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.domain.pairing.PairingHelper;

public class PairingEditActivity extends FragmentActivity {

    private static final String TAG = "PairingEditActivity";

    // Constant
    private static final int PAIRING_EDIT_LOADER = R.id.config_id_pairing_edit_loader;

    private static final int PICK_CONTACT = 0;

    // Paint
    Paint mPaint = new Paint();

    // Bindings
    private EditText nameEditText;
    private EditText phoneEditText; 

    // Instance
    private String entityId;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pairing_edit);
        // binding
        nameEditText = (EditText) findViewById(R.id.person_name);
        phoneEditText = (EditText) findViewById(R.id.person_phone); 
        // Intents
        handleIntent(getIntent());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_pairing_edit, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_save:
            onSaveClick();
            return true;
        case R.id.menu_delete:
            onDeleteClick();
            return true;
        case R.id.menu_select_contact:
            onSelectContactClick(null);
            return true;
        case R.id.menu_cancel:
            onCancelClick();
            return true;
        case R.id.menuQuitter:
            // Pour fermer l'application il suffit de faire finish()
            finish();
            return true;
        }
        return false;
    }

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
        if (Intent.ACTION_EDIT.equals(action)) {
            Uri data = intent.getData(); 
            loadEntity( data.getLastPathSegment());
         } else if (Intent.ACTION_DELETE.equals(action)) {
            // TODO
        } else if (Intent.ACTION_INSERT.equals(action)) {
            this.entityId = null; 
        }

    }

    private void loadEntity(String entityId) {
        this.entityId =entityId;
        Bundle bundle = new Bundle();
        bundle.putString(Intents.EXTRA_SMS_PHONE, entityId);
        getSupportLoaderManager().initLoader(PAIRING_EDIT_LOADER, bundle, pairingLoaderCallback);
    }
    
    // ===========================================================
    // Listener
    // ===========================================================

    public void onDeleteClick() {
        Uri entityUri = Uri.withAppendedPath(PairingProvider.Constants.CONTENT_URI ,   entityId);
        int deleteCount = getContentResolver().delete(entityUri, null, null);
        Log.d(TAG, "Delete %s entity successuf");
        if (deleteCount > 0) {
            setResult(Activity.RESULT_OK);
        }
        finish();
    }

    public void onSaveClick() {
        String name = nameEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        Uri uri = doSavePairing(name, phone);
        setResult(Activity.RESULT_OK);
        finish();
    }

    public void onCancelClick() {
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    /**
     * {link http://www.higherpass.com/Android/Tutorials/Working-With-Android-
     * Contacts/}
     * 
     * @param v
     */
    public void onSelectContactClick(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        // run
        startActivityForResult(intent, PICK_CONTACT);
    }

    public void onPairingClick(View v) {
        Intent intent = Intents.pairingRequest(this, phoneEditText.getText().toString(), entityId);
        startService(intent);
    }
 
    // ===========================================================
    // Activity Result handler
    // ===========================================================

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

    // ===========================================================
    // Contact Picker
    // ===========================================================

    private void saveContactData(Uri contactData) {
        String selection = null;
        String[] selectionArgs = null;
        Cursor c = getContentResolver().query(contactData, new String[] { //
                ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME, // TODO
                                                                        // Check
                                                                        // for
                                                                        // V10
                                                                        // compatibility
                        ContactsContract.CommonDataKinds.Phone.NUMBER, //
                        ContactsContract.CommonDataKinds.Phone.TYPE }, selection, selectionArgs, null);
        try {
            // Read value
            if (c != null && c.moveToFirst()) {
                String name = c.getString(0);
                String phone = c.getString(1);
                int type = c.getInt(2);
                doSavePairing(name, phone);
                // showSelectedNumber(type, number);
            }
        } finally {
            c.close();
        }
    }

    // ===========================================================
    // Data Model Management
    // ===========================================================

    private String cleanPhone(String phone) {
        String cleanPhone = phone;
        if (cleanPhone != null) {
            cleanPhone = cleanPhone.replaceAll(" ", "");
        }
        return cleanPhone;
    }

    private Uri doSavePairing(String name, String phoneDirty) {
        String phone = cleanPhone(phoneDirty);
        setPairing(name, phone);
        // Prepare db insert
        ContentValues values = new ContentValues();
        values.put(PairingColumns.COL_NAME, name);
        values.put(PairingColumns.COL_PHONE, phone);
        values.put(PairingColumns.COL_COLOR, mPaint.getColor());
        // Content
        Uri uri;
        if (entityId == null) {
            uri = getContentResolver().insert(PairingProvider.Constants.CONTENT_URI , values);
            setResult(Activity.RESULT_OK);
        } else {
            uri = Uri.withAppendedPath(PairingProvider.Constants.CONTENT_URI , entityId);
            int count = getContentResolver().update(uri, values, null, null);
            if (count != 1) {
                Log.e(TAG, String.format("Error, %s entities was updates for Expected One", count));
            }
        }
        return uri;
    }

    private void setPairing(String name, String phone) {
        nameEditText.setText(name);
        phoneEditText.setText(phone);
    }

    // ===========================================================
    // LoaderManager
    // ===========================================================

    private final LoaderManager.LoaderCallbacks<Cursor> pairingLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String entityId = args.getCharSequence(Intents.EXTRA_SMS_PHONE).toString();
            Uri entityUri = Uri.withAppendedPath(PairingProvider.Constants.CONTENT_URI, String.valueOf(  entityId));
            // Loader
            CursorLoader cursorLoader = new CursorLoader(PairingEditActivity.this, entityUri, null, null, null, null);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.d(TAG, "onLoadFinished with cursor result count : " + cursor.getCount());
            // Display List
            if (cursor.moveToFirst()) {
                // Data
                PairingHelper helper = new PairingHelper().initWrapper(cursor);
                helper.setTextPairingName(nameEditText, cursor)//
                        .setTextPairingPhone(phoneEditText, cursor); 
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            setPairing(null, null);
        }

    };

}
