package eu.ttbox.geoping.ui.geofence;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.actionbarsherlock.app.SherlockFragment;


import java.net.URI;

import eu.ttbox.geoping.R;
import eu.ttbox.geoping.domain.model.CircleGeofence;
import eu.ttbox.geoping.domain.pairing.GeoFenceDatabase;
import eu.ttbox.geoping.domain.pairing.GeoFenceHelper;


public class GeofenceEditFragment extends SherlockFragment {


    // Binding
    private EditText nameEditText;

    // Instance Data
    CircleGeofence fence;

    // ===========================================================
    // Constructors
    // ===========================================================


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.geofence_edit, container, false);
        // Bindings
        this.nameEditText = (EditText) v.findViewById(R.id.geofenceEditName);

        return v;
    }


    // ===========================================================
    // Load Data
    // ===========================================================

    public void loadEntity(Uri entityUri) {
        ContentResolver cr = getActivity().getContentResolver();
        String selection= null;
        String[] selectionArgs = null;
        Cursor cursor =  cr.query(entityUri, GeoFenceDatabase.GeoFenceColumns.ALL_COLS, selection, selectionArgs, null);
        try {
            if (cursor.moveToFirst()) {
                GeoFenceHelper helper = new GeoFenceHelper().initWrapper(cursor);
                CircleGeofence  geofence = helper.getEntity(cursor);
                loadEntity(geofence);
            }
        } finally {
            cursor.close();
        }
    }

    public void loadEntity(CircleGeofence geofence) {
        //
        this.nameEditText.setText(geofence.name);
        // defince
        this.fence = geofence;

    }

}
