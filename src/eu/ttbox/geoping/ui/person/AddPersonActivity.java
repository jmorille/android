package eu.ttbox.geoping.ui.person;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;

public class AddPersonActivity extends FragmentActivity {

    private static final String TAG = "AddPersonActivity";
    
    // Constant
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
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    protected void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        String action  =  intent.getAction();
        if (Log.isLoggable(TAG, Log.DEBUG)) {
            Log.d(TAG, "handleIntent for action : " +action);
        }
        if (Intent.ACTION_EDIT.equals(action)) {
            // TODO
         } else  if (Intent.ACTION_DELETE.equals(action)) {
            // TODO
        } else  if (Intent.ACTION_INSERT.equals(action)) {
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
        // Intent intent = new Intent(Intent.ACTION_PICK,
        // ContactsContract.Contacts.CONTENT_URI);
        //
//        Intent intent = new Intent(Intent.ACTION_PICK, Contacts.Phones.CONTENT_URI);
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
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
                ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME, // TODO Check for V10 compatibility
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
        ContentValues values = new ContentValues();
        values.put(PersonColumns.KEY_NAME, name);
        values.put(PersonColumns.KEY_PHONE, phone);
        // Content
        Uri uri = getContentResolver().insert(PersonProvider.Constants.CONTENT_URI, values);
        setResult(Activity.RESULT_OK);
        return uri;
    }

}
