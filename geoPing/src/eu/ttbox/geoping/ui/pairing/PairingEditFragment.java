package eu.ttbox.geoping.ui.pairing;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.core.AppConstants;
import eu.ttbox.geoping.core.Intents;
import eu.ttbox.geoping.core.NotifToasts;
import eu.ttbox.geoping.core.PhoneNumberUtils;
import eu.ttbox.geoping.domain.PairingProvider;
import eu.ttbox.geoping.domain.model.PairingAuthorizeTypeEnum;
import eu.ttbox.geoping.domain.pairing.PairingDatabase.PairingColumns;
import eu.ttbox.geoping.domain.pairing.PairingHelper;
import eu.ttbox.geoping.service.core.ContactHelper;
import eu.ttbox.geoping.ui.core.BindingHelper;
import eu.ttbox.geoping.ui.core.validator.Form;
import eu.ttbox.geoping.ui.core.validator.validate.ValidateTextView;
import eu.ttbox.geoping.ui.core.validator.validator.NotEmptyValidator;
import eu.ttbox.geoping.ui.pairing.validator.ExistPairingPhoneValidator;
import eu.ttbox.geoping.ui.person.PhotoEditorView;
import eu.ttbox.geoping.ui.person.PhotoThumbmailCache;

public class PairingEditFragment extends SherlockFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "PairingEditFragment";

    // Constant
    private static final int PAIRING_EDIT_LOADER = R.id.config_id_pairing_edit_loader;

    public static final int PICK_CONTACT = 0;

    // Service
    private SharedPreferences sharedPreferences;

    // Config
    private static final boolean DEFAULT_PREFS_SHOW_GEOPING_NOTIFICATION = false;
    private boolean showNotifDefault = DEFAULT_PREFS_SHOW_GEOPING_NOTIFICATION;

    // Paint
    Paint mPaint = new Paint();

    // Bindings
    private EditText nameEditText;
    private EditText phoneEditText;
    private CheckBox showNotificationCheckBox;
    private TextView authorizeTypeTextView;

    private RadioGroup authorizeTypeRadioGroup;

    private RadioButton authorizeTypeAskRadioButton;
    private RadioButton authorizeTypeNeverRadioButton;
    private RadioButton authorizeTypeAlwaysRadioButton;


    //Validator
    private Form formValidator;
    private ExistPairingPhoneValidator  existValidator;

    // Image
    private PhotoEditorView photoImageView;

    private Button selectContactClickButton;

    // Listener
    private OnPairingSelectListener onPairingSelectListener;

    // Cache
    private PhotoThumbmailCache photoCache;

    // Instance
    // private String entityId;
    private Uri entityUri;

    // ===========================================================
    // Interface
    // ===========================================================

    public interface OnPairingSelectListener {

        void onPersonSelect(Uri id, String phone);

    }

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "****************** onCreateView");
        View v = inflater.inflate(R.layout.pairing_edit, container, false);
        // Menu on Fragment
        setHasOptionsMenu(true);

        // Cache
        photoCache = ((GeoPingApplication) getActivity().getApplicationContext()).getPhotoThumbmailCache();

        // Prefs
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        // Config
        showNotifDefault = sharedPreferences.getBoolean(AppConstants.PREFS_SHOW_GEOPING_NOTIFICATION, DEFAULT_PREFS_SHOW_GEOPING_NOTIFICATION);

        // binding
        photoImageView = (PhotoEditorView) v.findViewById(R.id.pairing_photo_imageView);
        nameEditText = (EditText) v.findViewById(R.id.pairing_name);
        phoneEditText = (EditText) v.findViewById(R.id.pairing_phone);
        showNotificationCheckBox = (CheckBox) v.findViewById(R.id.paring_show_notification);
        authorizeTypeTextView = (TextView) v.findViewById(R.id.pairing_authorize_type);

        authorizeTypeRadioGroup = (RadioGroup) v.findViewById(R.id.pairing_authorize_type_radioGroup);
        authorizeTypeAskRadioButton = (RadioButton) v.findViewById(R.id.pairing_authorize_type_radio_ask);
        authorizeTypeNeverRadioButton = (RadioButton) v.findViewById(R.id.pairing_authorize_type_radio_never);
        authorizeTypeAlwaysRadioButton = (RadioButton) v.findViewById(R.id.pairing_authorize_type_radio_always);

        selectContactClickButton = (Button) v.findViewById(R.id.select_contact_button);
        // Radio Auth Listener
        OnClickListener radioAuthListener = new OnClickListener() {

            @Override
            public void onClick(View v) {
                onRadioAuthorizeTypeButtonClicked(v);

            }
        };
        authorizeTypeAskRadioButton.setOnClickListener(radioAuthListener);
        authorizeTypeNeverRadioButton.setOnClickListener(radioAuthListener);
        authorizeTypeAlwaysRadioButton.setOnClickListener(radioAuthListener);

        // default value
        showNotificationCheckBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onShowNotificationClick(v);

            }
        });
        showNotificationCheckBox.setChecked(showNotifDefault);

        // default value
        selectContactClickButton.setOnClickListener(new OnClickListener() {

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
        Log.d(TAG, "onActivityCreated");
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
        String entityId = entityUri ==null ? null : entityUri.getLastPathSegment();
        existValidator = new ExistPairingPhoneValidator(getActivity(), entityId);
        ValidateTextView phoneTextField = new ValidateTextView(phoneEditText)//
                .addValidator(new NotEmptyValidator()) //
                .addValidator(existValidator)  ;
        formValidator.addValidates(phoneTextField);


        return formValidator;
    }

    // ===========================================================
    // Menu
    // ===========================================================

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_pairing_edit, menu);
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
    // Life Cycle
    // ===========================================================

    @Override
    public void onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    // ===========================================================
    // Preferences
    // ===========================================================

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(AppConstants.PREFS_SHOW_GEOPING_NOTIFICATION)) {
            showNotifDefault = sharedPreferences.getBoolean(AppConstants.PREFS_SHOW_GEOPING_NOTIFICATION, DEFAULT_PREFS_SHOW_GEOPING_NOTIFICATION);
        }
    }

    // ===========================================================
    // Accessor
    // ===========================================================

    public void setOnPersonSelectListener(OnPairingSelectListener onPersonSelectListener) {
        this.onPairingSelectListener = onPersonSelectListener;
    }

    private void loadEntity(Bundle agrs) {
        if (agrs != null && agrs.containsKey(Intents.EXTRA_PERSON_ID)) {
            Uri entityId = Uri.parse(agrs.getString(Intents.EXTRA_PERSON_ID));
            loadEntity(entityId);
        } else {
            // prepare for insert
            prepareInsert();
        }
    }

    private void loadEntity(Uri entityUri) { // String entityId
        Log.d(TAG, "loadEntity : " + entityUri);
        // this.entityUri =
        // Uri.withAppendedPath(PairingProvider.Constants.CONTENT_URI,
        // entityId);
        this.entityUri = entityUri;
        String entityId = entityUri ==null ? null : entityUri.getLastPathSegment();
        existValidator.setEntityId(entityId);
        Bundle bundle = new Bundle();
        bundle.putString(Intents.EXTRA_DATA_URI, entityUri.toString());
        getActivity().getSupportLoaderManager().initLoader(PAIRING_EDIT_LOADER, bundle, pairingLoaderCallback);
    }

    private void prepareInsert() {
        this.entityUri = null;
        existValidator.setEntityId(null);
        showNotificationCheckBox.setChecked(showNotifDefault);
        // Open Selection contact Diallog
        onSelectContactClick(null);
        // Defautl value
        authorizeTypeAlwaysRadioButton.setChecked(true);
    }

    public void onDeleteClick() {
        int deleteCount = getActivity().getContentResolver().delete(entityUri, null, null);
        Log.d(TAG, "Delete %s entity successuf");
        if (deleteCount > 0) {
            getActivity().setResult(Activity.RESULT_OK);
        }
        getActivity().finish();
    }

    public void onSaveClick() {
        String name = nameEditText.getText().toString();
        String phone = phoneEditText.getText().toString();
        // TODO Select authorizeType
        PairingAuthorizeTypeEnum authType = null;
        if (authorizeTypeAlwaysRadioButton.isChecked()) {
            authType = PairingAuthorizeTypeEnum.AUTHORIZE_ALWAYS;
        } else if (authorizeTypeNeverRadioButton.isChecked()) {
            authType = PairingAuthorizeTypeEnum.AUTHORIZE_NEVER;
        } else {
            authType = PairingAuthorizeTypeEnum.AUTHORIZE_REQUEST;
        }

        // Do Save
        Uri uri = doSavePairing(name, phone, authType);
        if (uri!=null) {
            getActivity().setResult(Activity.RESULT_OK);
            getActivity().finish();
        }
    }

    public void onCancelClick() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }

    /**
     * {link http://www.higherpass.com/Android/Tutorials/Working-With-Android-
     * Contacts/}
     * 
     * @param v
     */
    public void onSelectContactClick(View v) {
        // String phoneNumber = phoneEditText.getText().toString();
        // Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
        // Uri.encode(phoneNumber));
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        // run
        startActivityForResult(intent, PICK_CONTACT);
    }

    public void onPairingClick(View v) {
        String entityId = entityUri.getLastPathSegment();
        Intent intent = Intents.pairingRequest(getActivity(), phoneEditText.getText().toString(), entityId);
        getActivity().startService(intent);
    }

    public void onShowNotificationClick(View v) {
        if (entityUri != null) {
            boolean isCheck = showNotificationCheckBox.isChecked();
            ContentValues values = new ContentValues();
            values.put(PairingColumns.COL_SHOW_NOTIF, isCheck);
            int count = getActivity().getContentResolver().update(entityUri, values, null, null);
        }
    }

    // ===========================================================
    // Listener
    // ===========================================================

    // ===========================================================
    // Contact Picker
    // ===========================================================

    public void saveContactData(Uri contactData) {
        String selection = null;
        String[] selectionArgs = null;
        ContentResolver cr = getActivity().getContentResolver();
        Cursor c = cr.query(contactData, new String[] { //
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
                // Check If exist in db
                String checkExistId = checkExistEntityId(cr, phone);
                // Save The select person
                if (checkExistId == null) {
                    Uri uri = doSavePairing(name, phone, null);
                } else {
                    Log.i(TAG, "Found existing Entity [" + checkExistId + "] for Phone : " + phone);
                    Uri checkExistUri = Uri.withAppendedPath(PairingProvider.Constants.CONTENT_URI, checkExistId);
                    loadEntity(checkExistUri);
                }
                // showSelectedNumber(type, number);
            }
        } finally {
            c.close();
        }
    }

    private String checkExistEntityId(ContentResolver cr, String phone) {
        Uri checkExistUri = PairingProvider.Constants.getUriPhoneFilter(phone);
        String[] checkExistProjections = new String[] { PairingColumns.COL_ID };
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

    public void onRadioAuthorizeTypeButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        PairingAuthorizeTypeEnum authType = null;
        // Check which radio button was clicked
        switch (view.getId()) {
        case R.id.pairing_authorize_type_radio_ask:
            if (checked)
                authType = PairingAuthorizeTypeEnum.AUTHORIZE_REQUEST;
            showNotificationCheckBox.setVisibility(View.GONE);
            break;
        case R.id.pairing_authorize_type_radio_always:
            if (checked)
                authType = PairingAuthorizeTypeEnum.AUTHORIZE_ALWAYS;
            showNotificationCheckBox.setVisibility(View.VISIBLE);
            break;
        case R.id.pairing_authorize_type_radio_never:
            if (checked)
                authType = PairingAuthorizeTypeEnum.AUTHORIZE_NEVER;
            showNotificationCheckBox.setVisibility(View.VISIBLE);
            break;
        }
        if (authType != null && entityUri != null) {
            ContentValues values = authType.writeTo(null);
            getActivity().getContentResolver().update(entityUri, values, null, null);
        }
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


    private Uri doSavePairing(String nameDirty, String phoneDirty, PairingAuthorizeTypeEnum authorizeType) {
        String phone = cleanPhone(phoneDirty);
        String name = BindingHelper.trimToNull(nameDirty);
        setPairing(name, phone);
        // Validate
        if (!formValidator.validate()) {
            return null;
        }
        // Prepare db insert
        ContentValues values = new ContentValues();
        values.put(PairingColumns.COL_NAME, name);
        values.put(PairingColumns.COL_PHONE, phone);
        if (authorizeType != null) {
            authorizeType.writeTo(values);
        }
        // Content
        Uri uri;
        ContentResolver cr = getActivity().getContentResolver();
        if (entityUri == null) {
            uri = cr.insert(PairingProvider.Constants.CONTENT_URI, values);
            this.entityUri = uri;
            String entityId = entityUri ==null ? null : entityUri.getLastPathSegment();
            existValidator.setEntityId( entityId );
            getActivity().setResult(Activity.RESULT_OK);
        } else {
            uri = entityUri;
            int count =cr.update(uri, values, null, null);
            if (count != 1) {
                Log.e(TAG, String.format("Error, %s entities was updates for Expected One", count));
            }
        }
        // Notifify listener
        if (onPairingSelectListener != null) {
            onPairingSelectListener.onPersonSelect(entityUri, phone);
        }
        return uri;
    }

    private void setPairing(String name, String phone) {
        nameEditText.setText(name);
        phoneEditText.setText(phone);
        photoCache.loadPhoto(getActivity(), photoImageView, null, phone);
    }

    // ===========================================================
    // Activity Result handler
    // ===========================================================

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
        case (PairingEditFragment.PICK_CONTACT):
            if (resultCode == Activity.RESULT_OK) {
                Uri contactData = data.getData();
                saveContactData(contactData);
                // finish();
            }
        }
    }

    // ===========================================================
    // LoaderManager
    // ===========================================================

    private final LoaderManager.LoaderCallbacks<Cursor> pairingLoaderCallback = new LoaderManager.LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d(TAG, "onCreateLoader");
            String entityId = args.getString(Intents.EXTRA_DATA_URI);
            Uri entityUri = Uri.parse(entityId);
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
                PairingHelper helper = new PairingHelper().initWrapper(cursor);
                // Data
                String pairingPhone = helper.getPairingPhone(cursor);
                // Binding
                phoneEditText.setText(pairingPhone);
                       helper.setTextPairingName(nameEditText, cursor)//
                        .setCheckBoxPairingShowNotif(showNotificationCheckBox, cursor);
                // Pairing
                PairingAuthorizeTypeEnum authType = helper.getPairingAuthorizeTypeEnum(cursor);
                switch (authType) {
                case AUTHORIZE_REQUEST:
                    authorizeTypeAskRadioButton.setChecked(true);
                    break;
                case AUTHORIZE_NEVER:
                    authorizeTypeNeverRadioButton.setChecked(true);
                    break;
                case AUTHORIZE_ALWAYS:
                    authorizeTypeAlwaysRadioButton.setChecked(true);
                    break;

                default:
                    break;
                }
                // Notif
                if (PairingAuthorizeTypeEnum.AUTHORIZE_REQUEST.equals(authType)) {
                    showNotificationCheckBox.setVisibility(View.GONE);
                }
                // Notify listener
                if (onPairingSelectListener != null) {
                    Uri pairingUri = entityUri;
                    onPairingSelectListener.onPersonSelect(pairingUri, pairingPhone);
                }
                // Photo
                photoCache.loadPhoto(getActivity(), photoImageView,  null, pairingPhone);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            setPairing(null, null);
        }

    };



    // ===========================================================
    // Others
    // ===========================================================

}
