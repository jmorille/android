package eu.ttbox.geoping.ui.person;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import java.util.Random;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.core.PhoneNumberUtils;
import eu.ttbox.geoping.domain.PersonProvider;
import eu.ttbox.geoping.domain.person.PersonDatabase.PersonColumns;
import eu.ttbox.geoping.domain.person.PersonHelper;
import eu.ttbox.geoping.service.core.ContactHelper;
import eu.ttbox.geoping.ui.core.validator.Form;
import eu.ttbox.geoping.ui.core.validator.validate.ValidateTextView;
import eu.ttbox.geoping.ui.core.validator.validator.NotEmptyValidator;
import eu.ttbox.geoping.ui.person.colorpicker.ColorPickerDialog;
import eu.ttbox.geoping.ui.person.holocolorpicker.HoloColorPickerDialog;
import eu.ttbox.geoping.ui.person.validator.ExistPersonPhoneValidator;

public class PersonEditFragment extends SherlockFragment implements ColorPickerDialog.OnColorChangedListener {

    private static final String TAG = "PersonEditFragment";

    // Constant
    private static final int PERSON_EDIT_LOADER = R.id.config_id_person_edit_loader;

    private static final int PICK_CONTACT = 1;
    private static final int REQUEST_PICK_PHOTO = 2;
    private static final int REQUEST_CROP_PHOTO = 3;
    private static final int PICK_COLOR = 4;

    // Paint
    Paint mPaint = new Paint();

    // Bindings
    private EditText nameEditText;
    private EditText phoneEditText;
    private Button colorPickerButton;
    private Button personPairingButton;
    private Button contactSelectButton;

    //Validator
    private Form formValidator;
    private ExistPersonPhoneValidator existValidator;

    // Image
    private PhotoEditorView photoImageView;

    // Instance
    private String entityId;
    private String contactId;


    // Listener
    private OnPersonSelectListener onPersonSelectListener;

    // Cache
    private PhotoThumbmailCache photoCache;

    // ===========================================================
    // Interface
    // ===========================================================

    public interface OnPersonSelectListener {

        void onPersonSelect(String id, String phone);

    }

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.track_person_edit, container, false);
        // Menu on Fragment
        setHasOptionsMenu(true);

        // Cache
        photoCache = ((GeoPingApplication) getActivity().getApplicationContext()).getPhotoThumbmailCache();

        // binding
        nameEditText = (EditText) v.findViewById(R.id.person_name);
        // nameEditText.setOnLongClickListener(new OnLongClickListener(){
        //
        // @Override
        // public boolean onLongClick(View v) {
        // onSelectContactClick(v);
        // return true;
        // }
        // });
        phoneEditText = (EditText) v.findViewById(R.id.person_phone);
        photoImageView = (PhotoEditorView) v.findViewById(R.id.person_photo_imageView);
        colorPickerButton = (Button) v.findViewById(R.id.person_color_picker_button);
        colorPickerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onColorPickerClick(v);
            }
        });
        personPairingButton = (Button) v.findViewById(R.id.person_pairing_button);
        personPairingButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onPairingClick(v);
            }
        });
        contactSelectButton = (Button) v.findViewById(R.id.select_contact_button);
        contactSelectButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSelectContactClick(v);
            }
        });
        // Form
        formValidator = createValidator(getActivity());
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Load Data
        loadEntity(getArguments());
    }


    // ===========================================================
    // Validator
    // ===========================================================

    public Form createValidator(Context context) {
        Form formValidator = new Form();
        // Name
        ValidateTextView nameTextField = new ValidateTextView(nameEditText)//
                .addValidator(new NotEmptyValidator());
        formValidator.addValidates(nameTextField);

        // Phone
          existValidator = new ExistPersonPhoneValidator(getActivity(), entityId);
        ValidateTextView phoneTextField = new ValidateTextView(phoneEditText)//
                .addValidator(new NotEmptyValidator()) //
                .addValidator(existValidator) //
                ;
        formValidator.addValidates(phoneTextField);

        return formValidator;
    }


    // ===========================================================
    // Menu
    // ===========================================================

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_person_edit, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
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
        }
        return false;
    }

    // ===========================================================
    // Accessor
    // ===========================================================

    private void setEntityId(String entityId) {
        this.entityId = entityId;
        this.existValidator.setEntityId(entityId);
    }

    public void setOnPersonSelectListener(OnPersonSelectListener onPersonSelectListener) {
        this.onPersonSelectListener = onPersonSelectListener;
    }

    private void loadEntity(Bundle agrs) {
        if (agrs != null && agrs.containsKey(Intents.EXTRA_PERSON_ID)) {
            String entityId = agrs.getString(Intents.EXTRA_PERSON_ID);
            loadEntity(entityId);
        } else {
            // prepare for insert
            prepareInsert();
        }
    }

    private void loadEntity(String entityId) {
        Log.d(TAG, "loadEntity : " + entityId);
        setEntityId(entityId);
        Bundle bundle = new Bundle();
        bundle.putString(Intents.EXTRA_PERSON_ID, entityId);
        getActivity().getSupportLoaderManager().initLoader(PERSON_EDIT_LOADER, bundle, personLoaderCallback);
    }

    public void onDeleteClick() {
        if (!TextUtils.isEmpty(entityId)) {
            Uri entityUri = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI, entityId);
            int deleteCount = getActivity().getContentResolver().delete(entityUri, null, null);
            Log.d(TAG, "Delete %s entity successuf");
            if (deleteCount > 0) {
                getActivity().setResult(Activity.RESULT_OK);
            }
            getActivity().finish();
        }
    }

    public void onSaveClick() {

        String name = nameEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        Uri uri = doSavePerson(name, phone, contactId );
        if (uri != null) {
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }

    }

    public void onCancelClick() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    public void onPairingClick(View v) {
        Intent intent = Intents.pairingRequest(getActivity(), phoneEditText.getText().toString(), entityId);
        getActivity().startService(intent);
    }

    // ===========================================================
    // Color
    // ===========================================================

    public void onColorPickerClick(View v) {
        // ColorPickerDialog dialog = new ColorPickerDialog(getActivity(), this,
        // mPaint.getColor());
        // dialog.show();

        // FragmentTransaction ft = getFragmentManager().beginTransaction();
        // Fragment prev =
        // getFragmentManager().findFragmentByTag("colorPickerDialog");
        // if (prev != null) {
        // ft.remove(prev);
        // }
        // ft.addToBackStack(null);
        // Create and show the dialog.
        HoloColorPickerDialog colorPicker = HoloColorPickerDialog.newInstance(mPaint.getColor());
        colorPicker.setTargetFragment(this, PICK_COLOR);
        colorPicker.show(getFragmentManager(), "colorPickerDialog");
    }

    @Override
    public void onColorChanged(int color) {
        Log.d(TAG, "Choose Color : " + color);
        mPaint.setColor(color);
        colorPickerButton.setBackgroundColor(color);
        if (entityId != null) {
            // Direct Persist Change
            ContentValues values = new ContentValues();
            values.put(PersonColumns.COL_COLOR, color);
            getActivity().getContentResolver().update(getUriEntity(), values, null, null);
        }
    }

    private void doColorChangeRamdom() {
        Random rand = new Random();
        int r = rand.nextInt(255);
        int g = rand.nextInt(255);
        int b = rand.nextInt(255);
        int ramdomColor = Color.rgb(r, g, b);
        onColorChanged(ramdomColor);
    }

    // ===========================================================
    // Contact Picker
    // ===========================================================

    /**
     * <a href="http://www.higherpass.com/Android/Tutorials/Working-With-Android-Contacts/">Working-With-Android-Contacts</a>
     *
     * @param v
     */
    public void onSelectContactClick(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);

        // Intent intent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT);
    }

    // ===========================================================
    // Photo
    // ===========================================================
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent result) {
        super.onActivityResult(requestCode, resultCode, result);

        switch (requestCode) {
            case (PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "PICK_CONTACT : " + result);
                    Uri contactData = result.getData();
                    saveContactData(contactData);
                    // finish();
                }
                break;
            case (REQUEST_PICK_PHOTO): {

                break;
            }
            case (REQUEST_CROP_PHOTO): {

                break;
            }
            case (PICK_COLOR): {
                Log.d(TAG, "PICK_COLOR : ");
                break;
            }

        }

    }

    // @Override
    // public void onActivityResult(int reqCode, int resultCode, Intent data) {
    // super.onActivityResult(reqCode, resultCode, data);
    //
    // switch (reqCode) {
    // case (PICK_CONTACT):
    // if (resultCode == Activity.RESULT_OK) {
    // Uri contactData = data.getData();
    // saveContactData(contactData);
    // // finish();
    // }
    // }
    // }

    // ===========================================================
    // Save Contact
    // ===========================================================

    /**
     * <a href="http://developer.android.com/guide/topics/providers/contacts-provider.html">contacts-provider</a>
     *
     * @param contactData
     */
    public void saveContactData(Uri contactData) {
        String selection = null;
        String[] selectionArgs = null;
        Log.d(TAG, "Select contact Uri : " + contactData);
        ContentResolver cr = getActivity().getContentResolver();
        Cursor c = getActivity().getContentResolver().query(contactData, new String[]{ //
                // BaseColumns._ID , //
                ContactsContract.Data.CONTACT_ID, //
                ContactsContract.CommonDataKinds.Identity.DISPLAY_NAME, //
                ContactsContract.CommonDataKinds.Phone.NUMBER, //
                ContactsContract.Contacts.LOOKUP_KEY, //
                ContactsContract.CommonDataKinds.Phone.TYPE}, selection, selectionArgs, null);
        // Uri contactLookupUri = ContactsContract.Data.getContactLookupUri(cr,
        // contactData);

        try {
            // Read value
            if (c != null && c.moveToFirst()) {
                String contactId = c.getString(0);
                String name = c.getString(1);
                String phone = c.getString(2);
               // String lookupKey = c.getString(3);
               // Uri lookupUri =  ContactsContract.Contacts.getLookupUri(Long.valueOf(contactId), lookupKey);
                // int type = c.getInt(4);
                Log.d(TAG, "Select contact Uri : " + contactData + " ==> Contact Id : " + contactId);
                // Log.d(TAG, "Select contact Uri : " + contactData +
                // " ==> Lookup Uri : " + contactLookupUri);
                // Check If exist in db
                String checkExistId = checkExistEntityId(cr, phone);
                // Save The select person
                if (checkExistId == null) {
                    Uri uri = doSavePerson(name, phone, contactId );
                } else {
                    Log.i(TAG, "Found existing Entity [" + checkExistId + "] for Phone : " + phone);
                    loadEntity(checkExistId);
                }
                // showSelectedNumber(type, number);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private String checkExistEntityId(ContentResolver cr, String phone) {
        Uri checkExistUri = PersonProvider.Constants.getUriPhoneFilter(phone);
        String[] checkExistProjections = new String[]{PersonColumns.COL_ID};
        Cursor checkExistCursor = cr.query(checkExistUri, checkExistProjections, null, null, null);
        String checkExistId = null;
        try {
            if (checkExistCursor.moveToNext()) {
                int checkExistColumnIndex = checkExistCursor.getColumnIndex(checkExistProjections[0]);
                checkExistId = checkExistCursor.getString(checkExistColumnIndex);
            }
        } finally {
            checkExistCursor.close();
        }
        return checkExistId;
    }

    // ===========================================================
    // Data Model Management
    // ===========================================================

    private String cleanPhone(String phone) {
        String cleanPhone = phone;
        if (cleanPhone != null) {
            cleanPhone = PhoneNumberUtils.normalizeNumber(phone);
        }
        if (cleanPhone != null) {
            cleanPhone = cleanPhone.trim();
            if (cleanPhone.length() < 1) {
                cleanPhone = null;
            }
        }
        return cleanPhone;
    }

    private String trimToNull(String nameDirty) {
        String name = nameDirty;
        if (name != null) {
            name = name.trim();
            if (name.length() < 1) {
                name = null;
            }
        }
        return name;
    }

    private Uri doSavePerson(String nameDirty, String phoneDirty, String contactId ) {
        String phone = cleanPhone(phoneDirty);
        String name = trimToNull(nameDirty);
        setPerson(name, phone, contactId );
        // Validate
        if (!formValidator.validate()) {
            return null;
        }
        // Prepare db insert
        ContentValues values = new ContentValues();
        values.put(PersonColumns.COL_NAME, name);
        values.put(PersonColumns.COL_PHONE, phone);
        values.put(PersonColumns.COL_COLOR, mPaint.getColor());
        if (contactId!=null) {
            values.put(PersonColumns.COL_CONTACT_ID, contactId);
        }

        Log.d(TAG, "Save Person with Contact Id : " + contactId);
        // Content
        boolean isListenerUpdated = false;
        Uri uri;
        if (entityId == null) {
            uri = getActivity().getContentResolver().insert(PersonProvider.Constants.CONTENT_URI, values);
            if (uri != null) {
                setEntityId(uri.getLastPathSegment());
                personPairingButton.setVisibility(View.VISIBLE);
                getActivity().setResult(Activity.RESULT_OK);
                isListenerUpdated = true;
            }
        } else {
            uri = getUriEntity();
            int count = getActivity().getContentResolver().update(uri, values, null, null);
            isListenerUpdated = true;
            if (count != 1) {
                Log.e(TAG, String.format("Error, %s entities was updates for Expected One", count));
            }
        }

        // Notifify listener
        if (onPersonSelectListener != null) {
            onPersonSelectListener.onPersonSelect(entityId, phone);
        }
        return uri;
    }

    private void setPerson(String name, String phone, String contactId ) {
        nameEditText.setText(name);
        phoneEditText.setText(phone);
        this.contactId = contactId;
        // Photo
        photoCache.loadPhoto(getActivity(), photoImageView  ,    contactId, phone);
    }

    private Uri getUriEntity() {
        return Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI, entityId);
    }

    public void prepareInsert() {
        setEntityId(null);
        doColorChangeRamdom();
        onSelectContactClick(null);
    }

    // ===========================================================
    // LoaderManager
    // ===========================================================

    private final LoaderManager.LoaderCallbacks<Cursor> personLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String entityId = args.getCharSequence(Intents.EXTRA_PERSON_ID).toString();
            Uri entityUri = Uri.withAppendedPath(PersonProvider.Constants.CONTENT_URI, entityId);
            // Loader
            CursorLoader cursorLoader = new CursorLoader(getActivity(), entityUri, null, null, null, null);
            return cursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
            Log.d(TAG, "onLoadFinished with cursor result count : " + cursor.getCount());
            // Display List
            if (cursor.moveToFirst()) {
                // Data
                PersonHelper helper = new PersonHelper().initWrapper(cursor);
                // Data
                contactId = helper.getContactId(cursor);
                long personId = helper.getPersonId(cursor);
                String personPhone = helper.getPersonPhone(cursor);
                // Validator
                existValidator.setEntityId(String.valueOf(personId));
                // Bind Values
                phoneEditText.setText(personPhone);
                helper.setTextPersonName(nameEditText, cursor);
                // Button
                personPairingButton.setVisibility(View.VISIBLE);
                contactSelectButton.setVisibility(View.GONE);
                int color = helper.getPersonColor(cursor);
                onColorChanged(color);
                // Affect Value

                // Notify listener
                if (onPersonSelectListener != null) {
                    onPersonSelectListener.onPersonSelect(String.valueOf(personId), personPhone);
                }
                // Photo
                photoCache.loadPhoto(getActivity(), photoImageView  ,    contactId, personPhone);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            setPerson(null, null, null);
        }

    };


    // ===========================================================
    // Others
    // ===========================================================

}
