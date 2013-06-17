package eu.ttbox.geoping.ui.smslog;


import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;

import eu.ttbox.geoping.GeoPingApplication;
import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.model.SmsLog;
import eu.ttbox.geoping.domain.smslog.SmsLogDatabase;
import eu.ttbox.geoping.domain.smslog.SmsLogHelper;
import eu.ttbox.geoping.service.core.ContactHelper;
import eu.ttbox.geoping.ui.person.PhotoThumbmailCache;

public class SmsLogViewFragment  extends SherlockFragment {


    private static final String TAG = "SmsLogViewFragment";

    // Binding
    private ImageView photoImageView;
    private TextView  phoneTextView;
    private TextView  requestIdTextView;

    // Cache
    private PhotoThumbmailCache photoCache;

    // Instance
    private Uri entityUri;

    // ===========================================================
    // Constructors
    // ===========================================================

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.smslog_view, container, false);
        // Menu on Fragment
        setHasOptionsMenu(true);
        // Cache
        photoCache = ((GeoPingApplication) getActivity().getApplicationContext()).getPhotoThumbmailCache();
        // Bindings
        photoImageView = (ImageView) v.findViewById(R.id.smslog_photo_imageView);
        phoneTextView= (TextView) v.findViewById(R.id.smslog_phone);
        requestIdTextView = (TextView) v.findViewById(R.id.smslog_requestId);
//        this.nameEditText = (EditText) v.findViewById(R.id.geofenceEditName);
       return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated");
        // Load Data
        loadEntity(getActivity().getIntent());
    }
    // ===========================================================
    // Menu
    // ===========================================================

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    // ===========================================================
    // Load Data
    // ===========================================================

    private void loadEntity(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            loadEntity(uri);
        } else {
            Log.w(TAG, "Could not load entity without Uri : " + intent);
        }
    }

    public void loadEntity(Uri entityUri) {
        Log.d(TAG, "Load entity Uri : " + entityUri);
        this.entityUri = entityUri;
        ContentResolver cr = getActivity().getContentResolver();
        String selection = null;
        String[] selectionArgs = null;
        Cursor cursor = cr.query(entityUri, SmsLogDatabase.SmsLogColumns.ALL_COLS, selection, selectionArgs, null);
        try {
            if (cursor.moveToFirst()) {
                SmsLogHelper helper = new SmsLogHelper().initWrapper(cursor);
                SmsLog smsLog = helper.getEntity(cursor);
                loadEntity(smsLog);
            }
        } finally {
            cursor.close();
        }
    }

    public void loadEntity(SmsLog smsLog) {
        Log.d(TAG, "Load entity Datan: " + smsLog);
        phoneTextView.setText(smsLog.phone);
        requestIdTextView.setText(smsLog.requestId);
        // Photo
        loadPhoto(null, smsLog.phone);
    }



    // ===========================================================
    // Photo Loader
    // ===========================================================

    /**
     * Pour plus de details sur l'intégration dans les contacts consulter
     * <ul>
     * <li>item_photo_editor.xml</li>
     * <li>com.android.contacts.editor.PhotoEditorView</li>
     * <li>com.android.contacts.detail.PhotoSelectionHandler</li>
     * <li>com.android.contacts.editor.ContactEditorFragment.PhotoHandler</li>
     * </ul>
     *
     * @param contactId
     */
    private void loadPhoto(String contactId, final String phone) {
        Bitmap photo = null;
        boolean isContactId = !TextUtils.isEmpty(contactId);
        boolean isContactPhone = !TextUtils.isEmpty(phone);
        // Search in cache
        if (photo == null && isContactId) {
            photo = photoCache.get(contactId);
        }
        if (photo == null && isContactPhone) {
            photo = photoCache.get(phone);
        }
        // Set Photo
        if (photo != null) {
            photoImageView.setImageBitmap(photo);
        } else if (isContactId || isContactPhone) {
            // Cancel previous Async
            final PhotoLoaderAsyncTask oldTask = (PhotoLoaderAsyncTask) photoImageView.getTag();
            if (oldTask != null) {
                oldTask.cancel(false);
            }
            // Load photos
            PhotoLoaderAsyncTask newTask = new PhotoLoaderAsyncTask(photoImageView);
            photoImageView.setTag(newTask);
            newTask.execute(contactId, phone);
        }
        // photoImageView.setEditorListener(new EditorListener() {
        //
        // @Override
        // public void onRequest(int request) {
        // Toast.makeText(getActivity(), "Click to phone " + phone,
        // Toast.LENGTH_SHORT).show();
        // }
        //
        // });
    }

    public class PhotoLoaderAsyncTask extends AsyncTask<String, Void, Bitmap> {

        final ImageView holder;

        public PhotoLoaderAsyncTask(ImageView holder) {
            super();
            this.holder = holder;
        }

        @Override
        protected void onPreExecute() {
            holder.setTag(this);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            String contactIdSearch = params[0];
            String phoneSearch = null;
            if (params.length > 1) {
                phoneSearch = params[1];
            }
            Bitmap result = ContactHelper.openPhotoBitmap(getActivity(), photoCache, contactIdSearch, phoneSearch);
            Log.d(TAG, "PhotoLoaderAsyncTask load photo : " + (result != null));
            return result;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (holder.getTag() == this) {
                holder.setImageBitmap(result);
                holder.setTag(null);
                Log.d(TAG, "PhotoLoaderAsyncTask onPostExecute photo : " + (result != null));
            }
        }
    }

    // ===========================================================
    // Others
    // ===========================================================

}
